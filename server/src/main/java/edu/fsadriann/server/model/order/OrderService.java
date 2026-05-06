package edu.fsadriann.server.model.order;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.server.model.cuadrante.CuadranteService;
import edu.fsadriann.server.model.product.Product;
import edu.fsadriann.model.iterator.Iterator;

import java.rmi.RemoteException;

public class OrderService implements OrderInterface {

    private static final double IVA              = 0.19;
    private static final double COSTO_DOMI_STD   = 5_000.0;  // fallback si no hay cuadrante
    private static final double TARIFA_BASE_DOMI = 2_000.0;  // COP base
    private static final double TARIFA_POR_KM    = 800.0;    // COP por km

    static final String ORIGEN_BASE = "UPB";

    private final LinkedList<Order>  pedidos;
    private final CuadranteService   cuadranteService; // inyectado para calcular distancias

    // ── Constructor ───────────────────────────────────────────────────────────

    /**
     * @param cuadranteService servicio de cuadrantes ya inicializado en el servidor.
     *                         Se usa para calcular el costo de domicilio por distancia.
     */
    public OrderService(CuadranteService cuadranteService) {
        this.pedidos          = new LinkedList<>();
        this.cuadranteService = cuadranteService;
    }

    // ── Creación ──────────────────────────────────────────────────────────────

    @Override
    public Order crearPedido(String cedulaCliente, boolean isPremium) throws RemoteException {
        if (cedulaCliente == null || cedulaCliente.isBlank())
            throw new IllegalArgumentException("La cédula del cliente es requerida.");
        Order order = new Order(cedulaCliente, isPremium);
        pedidos.add(order);
        return order;
    }

    // ── Cocina ────────────────────────────────────────────────────────────────

    @Override
    public void enviarPedidoACocina(Order pedido) throws RemoteException {
        if (pedido == null) throw new IllegalArgumentException("Pedido nulo.");

        Order serverPedido = buscarPedido(pedido.getOrderId());
        if (serverPedido == null)
            throw new IllegalArgumentException("Pedido no encontrado: " + pedido.getOrderId());

        if (serverPedido.getEstado() != EstadoPedido.PENDIENTE)
            throw new IllegalStateException(
                    "El pedido debe estar PENDIENTE. Estado actual: " + serverPedido.getEstado());

        if (serverPedido.getCantProductos() == 0)
            throw new IllegalStateException("No se puede enviar a cocina un pedido sin productos.");

        serverPedido.setEstado(EstadoPedido.EN_PREPARACION);
        actualizar(serverPedido);
    }

    // ── Productos del carrito ─────────────────────────────────────────────────

    @Override
    public boolean agregarProducto(String pedidoId, Product product) throws RemoteException {
        Order pedido = buscarPedido(pedidoId);
        if (pedido == null) return false;
        if (pedido.getEstado() != EstadoPedido.PENDIENTE)
            throw new IllegalStateException("Solo se agregan productos en PENDIENTE.");
        pedido.agregarProducto(product);
        return actualizar(pedido);
    }

    @Override
    public boolean quitarProducto(String pedidoId, Product product) throws RemoteException {
        Order pedido = buscarPedido(pedidoId);
        if (pedido == null) return false;
        if (pedido.getEstado() != EstadoPedido.PENDIENTE)
            throw new IllegalStateException("Solo se quitan productos en PENDIENTE.");

        // Buscar la referencia real dentro del carrito del servidor
        Product enServidor = null;
        Iterator<Product> it = pedido.getCarrito().iterator();
        while (it.hasNext()) {
            Product p = it.next();
            if (p != null && product.getProductoId().equals(p.getProductoId())) {
                enServidor = p;
                break;
            }
        }
        if (enServidor == null) return false;

        boolean removed = pedido.quitarProducto(enServidor);
        if (removed) actualizar(pedido);
        return removed;
    }

    @Override
    public boolean cambiarCantidadProducto(String pedidoId, String productoId,
                                           int nuevaCantidad) throws RemoteException {
        Order pedido = buscarPedido(pedidoId);
        if (pedido == null) return false;
        if (pedido.getEstado() != EstadoPedido.PENDIENTE) return false;

        Iterator<Product> it = pedido.getCarrito().iterator();
        while (it.hasNext()) {
            Product p = it.next();
            if (p != null && productoId.equals(p.getProductoId())) {
                p.setCantidad(nuevaCantidad);
                actualizar(pedido);
                return true;
            }
        }
        return false;
    }

    // ── Factura ───────────────────────────────────────────────────────────────

    /**
     * Calcula la factura completa del pedido.
     * El costo de domicilio se calcula usando la distancia real al cuadrante destino:
     *   costo = TARIFA_BASE + (distanciaKm × TARIFA_POR_KM)
     * Si el cliente es premium, el domicilio es gratis.
     * Si el cuadrante destino no está asignado, se usa el costo estándar como fallback.
     */
    @Override
    public double calcularFactura(Order pedido) throws RemoteException {
        if (pedido == null) throw new IllegalArgumentException("Pedido nulo.");

        Order serverPedido = buscarPedido(pedido.getOrderId());
        if (serverPedido == null)
            throw new IllegalArgumentException("Pedido no encontrado: " + pedido.getOrderId());

        // Subtotal
        double subtotal = 0.0;
        Iterator<Product> it = serverPedido.getCarrito().iterator();
        while (it.hasNext()) {
            Product p = it.next();
            if (p != null && p.isDisponible())
                subtotal += (double) p.getPrecio() * p.getCantidad();
        }

        double iva   = subtotal * IVA;
        double domi  = calcularCostoDomicilio(serverPedido);
        double total = subtotal + iva + domi;

        serverPedido.setSubtotal(subtotal);
        serverPedido.setImpuesto(iva);
        serverPedido.setCostoDomi(domi);
        serverPedido.setTotal(total);

        actualizar(serverPedido);
        return total;
    }

    /**
     * Calcula el costo de domicilio según el cuadrante destino del pedido.
     *
     * <ul>
     *   <li>Premium → siempre gratis.</li>
     *   <li>Sin cuadrante asignado → tarifa estándar ({@value #COSTO_DOMI_STD} COP).</li>
     *   <li>Con cuadrante → tarifa base + distancia × tarifa por km.</li>
     * </ul>
     */
    private double calcularCostoDomicilio(Order pedido) {
        if (pedido.isPremium()) return 0.0;

        String destino = pedido.getCuadranteDestino();
        if (destino == null || destino.isBlank()) return COSTO_DOMI_STD;

        try {
            double distKm = cuadranteService.calcularDistancia(ORIGEN_BASE, destino);
            if (distKm <= 0) return COSTO_DOMI_STD;
            return TARIFA_BASE_DOMI + (distKm * TARIFA_POR_KM);
        } catch (Exception e) {
            return COSTO_DOMI_STD; // fallback ante cualquier error del grafo
        }
    }

    // ── Cuadrante destino ─────────────────────────────────────────────────────

    @Override
    public boolean asignarCuadranteDestino(String orderId, String nombreCuadrante) {
        if (nombreCuadrante == null || nombreCuadrante.isBlank())
            throw new IllegalArgumentException("El nombre del cuadrante no puede ser nulo ni vacío.");
        if (orderId == null) return false;

        try {
            Order pedido = buscarPedido(orderId);
            if (pedido == null) return false;
            if (pedido.getEstado() == EstadoPedido.CANCELADO) return false;

            pedido.setCuadranteDestino(nombreCuadrante);
            return actualizar(pedido);
        } catch (RemoteException e) {
            return false;
        }
    }

    // ── Ruta de entrega ───────────────────────────────────────────────────────

    /**
     * Calcula la ruta óptima desde UPB hasta el cuadrante destino del pedido
     * usando Dijkstra a través del {@link CuadranteService}.
     * Si el cuadrante no está asignado, devuelve null.
     */
    @Override
    public LinkedList<String> calcularRutaEntrega(String orderId) {
        if (orderId == null) return null;
        try {
            Order pedido = buscarPedido(orderId);
            if (pedido == null) return null;
            if (pedido.getEstado() == EstadoPedido.CANCELADO) return null;

            String destino = pedido.getCuadranteDestino();
            if (destino == null || destino.isBlank()) return null;

            // Usar Dijkstra real en lugar de lista de dos nodos
            return cuadranteService.calcularRutaMasCorta(ORIGEN_BASE, destino);
        } catch (Exception e) {
            return null;
        }
    }

    // ── Estados del pedido ────────────────────────────────────────────────────

    @Override
    public boolean modificarPedido(Order pedido) throws RemoteException {
        if (pedido == null) return false;
        if (pedido.getEstado() != EstadoPedido.PENDIENTE)
            throw new IllegalStateException(
                    "Solo modificable en PENDIENTE. Estado: " + pedido.getEstado());
        return actualizar(pedido);
    }

    @Override
    public boolean cancelarPedido(String pedidoId) throws RemoteException {
        Order pedido = buscarPedido(pedidoId);
        if (pedido == null) return false;

        EstadoPedido estado = pedido.getEstado();
        if (estado == EstadoPedido.LISTO     ||
                estado == EstadoPedido.EN_CAMINO ||
                estado == EstadoPedido.ENTREGADO)
            throw new IllegalStateException("No cancelable en estado: " + estado);

        pedido.setEstado(EstadoPedido.CANCELADO);
        return actualizar(pedido);
    }

    @Override
    public boolean marcarEnCamino(String orderId) {
        if (orderId == null) return false;
        try {
            Order pedido = buscarPedido(orderId);
            if (pedido == null) return false;
            if (pedido.getEstado() != EstadoPedido.LISTO)
                throw new IllegalStateException(
                        "Solo se puede iniciar entrega desde LISTO. Estado actual: "
                                + pedido.getEstado());
            pedido.setEstado(EstadoPedido.EN_CAMINO);
            return actualizar(pedido);
        } catch (RemoteException e) {
            return false;
        }
    }

    @Override
    public boolean marcarEntregado(String orderId) {
        if (orderId == null) return false;
        try {
            Order pedido = buscarPedido(orderId);
            if (pedido == null) return false;
            if (pedido.getEstado() != EstadoPedido.EN_CAMINO)
                throw new IllegalStateException(
                        "Solo se puede completar desde EN_CAMINO. Estado actual: "
                                + pedido.getEstado());
            pedido.setEstado(EstadoPedido.ENTREGADO);
            return actualizar(pedido);
        } catch (RemoteException e) {
            return false;
        }
    }

    @Override
    public boolean marcarListo(String orderId) {
        if (orderId == null) return false;
        try {
            Order pedido = buscarPedido(orderId);
            if (pedido == null) return false;
            if (pedido.getEstado() != EstadoPedido.EN_PREPARACION)
                throw new IllegalStateException(
                        "Solo se puede marcar LISTO desde EN_PREPARACION. Estado actual: "
                                + pedido.getEstado());
            pedido.setEstado(EstadoPedido.LISTO);
            return actualizar(pedido);
        } catch (RemoteException e) {
            return false;
        }
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    @Override
    public Order buscarPedido(String pedidoId) throws RemoteException {
        if (pedidoId == null) return null;
        Iterator<Order> it = pedidos.iterator();
        while (it.hasNext()) {
            Order o = it.next();
            if (o != null && pedidoId.equals(o.getOrderId())) return o;
        }
        return null;
    }

    @Override
    public LinkedList<Order> getPedidosPorCliente(String cedula) throws RemoteException {
        LinkedList<Order> resultado = new LinkedList<>();
        if (cedula == null) return resultado;
        Iterator<Order> it = pedidos.iterator();
        while (it.hasNext()) {
            Order o = it.next();
            if (o != null && cedula.equals(o.getCedulaCliente())) resultado.add(o);
        }
        return resultado;
    }

    @Override
    public LinkedList<Order> getPedidosEnPreparacion() throws RemoteException {
        LinkedList<Order> resultado = new LinkedList<>();
        Iterator<Order> it = pedidos.iterator();
        while (it.hasNext()) {
            Order o = it.next();
            if (o != null && o.getEstado() == EstadoPedido.EN_PREPARACION) resultado.add(o);
        }
        return resultado;
    }

    @Override
    public LinkedList<Order> listarTodosPedidos() throws RemoteException {
        return pedidos;
    }

    public LinkedList<Order> listarTodosLosPedidos() {
        return pedidos;
    }

    // ── Helper privado ────────────────────────────────────────────────────────

    private boolean actualizar(Order order) {
        boolean removed = pedidos.remove(o -> order.getOrderId().equals(o.getOrderId()));
        if (removed) pedidos.add(order);
        return removed;
    }
}