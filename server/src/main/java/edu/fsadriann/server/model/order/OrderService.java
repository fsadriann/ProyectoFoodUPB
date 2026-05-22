package edu.fsadriann.server.model.order;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.server.model.cuadrante.CuadranteService;
import edu.fsadriann.server.model.product.Product;
import edu.fsadriann.model.iterator.Iterator;

import java.rmi.RemoteException;

/**
 * Servicio que gestiona el ciclo de vida de los pedidos en Food UPB.
 * Calcula facturas con IVA y costo de domicilio según la distancia al cuadrante destino.
 */
public class OrderService implements OrderInterface {

    private static final double IVA              = 0.19;
    private static final double COSTO_DOMI_STD   = 5_000.0;
    private static final double TARIFA_BASE_DOMI = 2_000.0;
    private static final double TARIFA_POR_KM    = 800.0;

    static final String ORIGEN_BASE = "UPB";

    private final LinkedList<Order> pedidos;
    private final CuadranteService  cuadranteService;

    /**
     * Crea el servicio de pedidos con el servicio de cuadrantes para calcular domicilios.
     *
     * @param cuadranteService servicio de cuadrantes para obtener distancias reales
     */
    public OrderService(CuadranteService cuadranteService) {
        this.pedidos          = new LinkedList<>();
        this.cuadranteService = cuadranteService;
    }

    /**
     * Crea un nuevo pedido en estado PENDIENTE para el cliente indicado.
     *
     * @param cedulaCliente cédula del cliente
     * @param isPremium     {@code true} si el cliente tiene membresía premium
     * @return el pedido creado
     */
    @Override
    public Order crearPedido(String cedulaCliente, boolean isPremium) throws RemoteException {
        if (cedulaCliente == null || cedulaCliente.isBlank())
            throw new IllegalArgumentException("La cédula del cliente es requerida.");
        Order order = new Order(cedulaCliente, isPremium);
        pedidos.add(order);
        return order;
    }

    /**
     * Envía un pedido pendiente a cocina, cambiando su estado a EN_PREPARACION.
     * El pedido debe tener al menos un producto y estar en estado PENDIENTE.
     *
     * @param pedido pedido a enviar
     */
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

    /**
     * Agrega un producto al carrito del pedido (solo si está en estado PENDIENTE).
     *
     * @param pedidoId identificador del pedido
     * @param product  producto a agregar
     * @return {@code true} si se agregó exitosamente
     */
    @Override
    public boolean agregarProducto(String pedidoId, Product product) throws RemoteException {
        Order pedido = buscarPedido(pedidoId);
        if (pedido == null) return false;
        if (pedido.getEstado() != EstadoPedido.PENDIENTE)
            throw new IllegalStateException("Solo se agregan productos en PENDIENTE.");
        pedido.agregarProducto(product);
        return actualizar(pedido);
    }

    /**
     * Quita un producto del carrito del pedido (solo si está en estado PENDIENTE).
     *
     * @param pedidoId identificador del pedido
     * @param product  producto a quitar
     * @return {@code true} si se quitó exitosamente
     */
    @Override
    public boolean quitarProducto(String pedidoId, Product product) throws RemoteException {
        Order pedido = buscarPedido(pedidoId);
        if (pedido == null) return false;
        if (pedido.getEstado() != EstadoPedido.PENDIENTE)
            throw new IllegalStateException("Solo se quitan productos en PENDIENTE.");

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

    /**
     * Cambia la cantidad de un producto dentro del carrito (solo en estado PENDIENTE).
     *
     * @param pedidoId     identificador del pedido
     * @param productoId   identificador del producto
     * @param nuevaCantidad nueva cantidad deseada
     * @return {@code true} si se actualizó exitosamente
     */
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

    /**
     * Calcula la factura del pedido: subtotal + IVA (19%) + costo de domicilio.
     * Si el cliente es premium, el domicilio es gratis.
     * Si hay cuadrante asignado, el costo se calcula por distancia real desde UPB.
     *
     * @param pedido pedido a facturar
     * @return valor total en pesos colombianos
     */
    @Override
    public double calcularFactura(Order pedido) throws RemoteException {
        if (pedido == null) throw new IllegalArgumentException("Pedido nulo.");

        Order serverPedido = buscarPedido(pedido.getOrderId());
        if (serverPedido == null)
            throw new IllegalArgumentException("Pedido no encontrado: " + pedido.getOrderId());

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

    private double calcularCostoDomicilio(Order pedido) {
        if (pedido.isPremium()) return 0.0;

        String destino = pedido.getCuadranteDestino();
        if (destino == null || destino.isBlank()) return COSTO_DOMI_STD;

        try {
            double distKm = cuadranteService.calcularDistancia(ORIGEN_BASE, destino);
            if (distKm <= 0) return COSTO_DOMI_STD;
            return TARIFA_BASE_DOMI + (distKm * TARIFA_POR_KM);
        } catch (Exception e) {
            return COSTO_DOMI_STD;
        }
    }

    /**
     * Asigna el cuadrante de destino a un pedido para el cálculo de la ruta de entrega.
     *
     * @param orderId         identificador del pedido
     * @param nombreCuadrante nombre del cuadrante de destino
     * @return {@code true} si se asignó exitosamente
     */
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

    /**
     * Calcula la ruta más corta desde UPB hasta el cuadrante destino del pedido.
     *
     * @param orderId identificador del pedido
     * @return lista de cuadrantes en la ruta, o {@code null} si no hay cuadrante asignado
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

            return cuadranteService.calcularRutaMasCorta(ORIGEN_BASE, destino);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Actualiza los datos de un pedido en estado PENDIENTE.
     *
     * @param pedido pedido con datos actualizados
     * @return {@code true} si se modificó exitosamente
     */
    @Override
    public boolean modificarPedido(Order pedido) throws RemoteException {
        if (pedido == null) return false;
        if (pedido.getEstado() != EstadoPedido.PENDIENTE)
            throw new IllegalStateException(
                    "Solo modificable en PENDIENTE. Estado: " + pedido.getEstado());
        return actualizar(pedido);
    }

    /**
     * Cancela un pedido que aún no haya sido despachado.
     *
     * @param pedidoId identificador del pedido
     * @return {@code true} si se canceló exitosamente
     */
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

    /**
     * Cambia el estado del pedido a EN_CAMINO (pedido en trayecto de entrega).
     *
     * @param orderId identificador del pedido
     * @return {@code true} si el cambio fue exitoso
     */
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

    /**
     * Cambia el estado del pedido a ENTREGADO.
     *
     * @param orderId identificador del pedido
     * @return {@code true} si el cambio fue exitoso
     */
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

    /**
     * Cambia el estado del pedido a LISTO (pedido preparado en cocina).
     *
     * @param orderId identificador del pedido
     * @return {@code true} si el cambio fue exitoso
     */
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

    /**
     * Busca un pedido por su identificador único.
     *
     * @param pedidoId identificador del pedido
     * @return el pedido encontrado, o {@code null} si no existe
     */
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

    /**
     * Retorna todos los pedidos de un cliente.
     *
     * @param cedula cédula del cliente
     * @return lista de pedidos del cliente
     */
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

    /**
     * Retorna los pedidos actualmente en preparación en cocina.
     *
     * @return lista de pedidos con estado EN_PREPARACION
     */
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

    /** @return lista completa de todos los pedidos */
    @Override
    public LinkedList<Order> listarTodosPedidos() throws RemoteException {
        return pedidos;
    }

    /** @return lista completa de todos los pedidos (acceso sin RemoteException) */
    public LinkedList<Order> listarTodosLosPedidos() {
        return pedidos;
    }

    private boolean actualizar(Order order) {
        boolean removed = pedidos.remove(o -> order.getOrderId().equals(o.getOrderId()));
        if (removed) pedidos.add(order);
        return removed;
    }
}
