package edu.fsadriann.server.model.order;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.server.model.product.Product;
import edu.fsadriann.model.iterator.Iterator;

import java.rmi.RemoteException;

public class OrderService implements OrderInterface {

    private static final double IVA            = 0.19;
    private static final double COSTO_DOMI_STD = 5_000.0; // COP estándar

    static final String ORIGEN_BASE = "UPB";

    private final LinkedList<Order> pedidos;

    public OrderService() {
        this.pedidos = new LinkedList<>();
    }

    @Override
    public Order crearPedido(String cedulaCliente, boolean isPremium) throws RemoteException {
        if (cedulaCliente == null || cedulaCliente.isBlank()) {
            throw new IllegalArgumentException("La cédula del cliente es requerida.");
        }
        Order order = new Order(cedulaCliente, isPremium);
        pedidos.add(order);
        return order;
    }

    // ── FIX: buscar el pedido en el servidor por ID, no usar el objeto
    //         deserializado del cliente (que llega sin productos en el carrito).
    @Override
    public void enviarPedidoACocina(Order pedido) throws RemoteException {
        if (pedido == null) throw new IllegalArgumentException("Pedido nulo.");

        // Usamos la copia que vive en el servidor, donde los productos SÍ están
        Order serverPedido = buscarPedido(pedido.getOrderId());
        if (serverPedido == null)
            throw new IllegalArgumentException("Pedido no encontrado en servidor: " + pedido.getOrderId());

        if (serverPedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException(
                    "El pedido debe estar PENDIENTE. Estado actual: " + serverPedido.getEstado());
        }
        if (serverPedido.getCantProductos() == 0) {
            throw new IllegalStateException(
                    "No se puede enviar a cocina un pedido sin productos.");
        }
        serverPedido.setEstado(EstadoPedido.EN_PREPARACION);
        actualizar(serverPedido);
    }

    @Override
    public boolean agregarProducto(String pedidoId, Product product) throws RemoteException {
        Order pedido = buscarPedido(pedidoId);
        if (pedido == null) return false;
        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("Solo se agregan productos en PENDIENTE.");
        }
        pedido.agregarProducto(product);
        return actualizar(pedido);
    }

    // ── FIX: igual que enviarPedidoACocina — leer carrito del servidor,
    //         no del objeto que llegó por RMI (puede estar vacío).
    @Override
    public double calcularFactura(Order pedido) throws RemoteException {
        if (pedido == null) throw new IllegalArgumentException("Pedido nulo.");

        Order serverPedido = buscarPedido(pedido.getOrderId());
        if (serverPedido == null)
            throw new IllegalArgumentException("Pedido no encontrado en servidor: " + pedido.getOrderId());

        double subtotal = 0.0;
        Iterator<Product> it = serverPedido.getCarrito().iterator();
        while (it.hasNext()) {
            Product p = it.next();
            if (p != null && p.isDisponible()) {
                subtotal += (double) p.getPrecio() * p.getCantidad();
            }
        }

        double iva   = subtotal * IVA;
        double domi  = serverPedido.isPremium() ? 0.0 : COSTO_DOMI_STD;
        double total = subtotal + iva + domi;

        serverPedido.setSubtotal(subtotal);
        serverPedido.setImpuesto(iva);
        serverPedido.setCostoDomi(domi);
        serverPedido.setTotal(total);

        actualizar(serverPedido);
        return total;
    }

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
    public boolean modificarPedido(Order pedido) throws RemoteException {
        if (pedido == null) return false;
        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException(
                    "Solo modificable en PENDIENTE. Estado: " + pedido.getEstado());
        }
        return actualizar(pedido);
    }

    @Override
    public boolean cancelarPedido(String pedidoId) throws RemoteException {
        Order pedido = buscarPedido(pedidoId);
        if (pedido == null) return false;

        EstadoPedido estado = pedido.getEstado();
        if (estado == EstadoPedido.LISTO     ||
                estado == EstadoPedido.EN_CAMINO ||
                estado == EstadoPedido.ENTREGADO) {
            throw new IllegalStateException(
                    "No cancelable en estado: " + estado);
        }
        pedido.setEstado(EstadoPedido.CANCELADO);
        return actualizar(pedido);
    }

    public boolean quitarProducto(String pedidoId, Product product) throws RemoteException {
        Order pedido = buscarPedido(pedidoId);
        if (pedido == null) return false;
        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("Solo se quitan productos en PENDIENTE.");
        }
        boolean removed = pedido.quitarProducto(product);
        if (removed) actualizar(pedido);
        return removed;
    }

    @Override
    public edu.fsadriann.app.linkedlist.singly.singly.LinkedList<Order> getPedidosPorCliente(String cedula) throws RemoteException {
        edu.fsadriann.app.linkedlist.singly.singly.LinkedList<Order> resultado = new edu.fsadriann.app.linkedlist.singly.singly.LinkedList<>();
        if (cedula == null) return resultado;
        Iterator<Order> it = pedidos.iterator();
        while (it.hasNext()) {
            Order o = it.next();
            if (o != null && cedula.equals(o.getCedulaCliente())) {
                resultado.add(o);
            }
        }
        return resultado;
    }

    @Override
    public boolean asignarCuadranteDestino(String orderId, String nombreCuadrante) {
        if (nombreCuadrante == null || nombreCuadrante.isBlank())
            throw new IllegalArgumentException(
                    "El nombre del cuadrante no puede ser nulo ni vacío.");
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

    @Override
    public edu.fsadriann.app.linkedlist.singly.singly.LinkedList<String> calcularRutaEntrega(String orderId) {
        if (orderId == null) return null;
        try {
            Order pedido = buscarPedido(orderId);
            if (pedido == null) return null;
            if (pedido.getEstado() == EstadoPedido.CANCELADO) return null;
            if (pedido.getCuadranteDestino() == null) return null;
            edu.fsadriann.app.linkedlist.singly.singly.LinkedList<String> ruta = new edu.fsadriann.app.linkedlist.singly.singly.LinkedList<>();
            ruta.add(ORIGEN_BASE);
            ruta.add(pedido.getCuadranteDestino());
            return ruta;
        } catch (Exception e) {
            return null;
        }
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
                        "Solo se puede completar entrega desde EN_CAMINO. Estado actual: "
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

    @Override
    public edu.fsadriann.app.linkedlist.singly.singly.LinkedList<Order> getPedidosEnPreparacion() throws RemoteException {
        edu.fsadriann.app.linkedlist.singly.singly.LinkedList<Order> resultado =
                new edu.fsadriann.app.linkedlist.singly.singly.LinkedList<>();
        Iterator<Order> it = pedidos.iterator();
        while (it.hasNext()) {
            Order o = it.next();
            if (o != null && o.getEstado() == EstadoPedido.EN_PREPARACION) {
                resultado.add(o);
            }
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
    // ── Helper privado ─────────────────────────────────────────────────────────────

    private boolean actualizar(Order order) {
        boolean removed = pedidos.remove(o -> order.getOrderId().equals(o.getOrderId()));
        if (removed) pedidos.add(order);
        return removed;
    }


}