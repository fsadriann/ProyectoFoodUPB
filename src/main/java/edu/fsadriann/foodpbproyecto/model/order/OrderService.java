package edu.fsadriann.foodpbproyecto.model.order;

import edu.fsadriann.app.linkedlist.doubly.doubly.LinkedList;
import edu.fsadriann.foodpbproyecto.model.product.Product;
import edu.fsadriann.model.iterator.Iterator;

import java.rmi.RemoteException;

/**
 * Servicio de pedidos: implementa {@link OrderInterface} con lógica real.
 *
 * <p>Almacena pedidos en una {@code DoublyLinkedList} del JAR.
 * Usa el {@code Iterator} tipado del JAR para evitar problemas de cast con genéricos.
 *
 * <ul>
 *   <li>RF-02 – Crear pedido y enviar a cocina</li>
 *   <li>RF-03 – Calcular factura (subtotal + IVA 19% + domicilio)</li>
 *   <li>RF-04 – Buscar, modificar, cancelar, agregar/quitar productos</li>
 * </ul>
 *
 * @author fsadriann
 */
public class OrderService implements OrderInterface {

    private static final double IVA            = 0.19;
    private static final double COSTO_DOMI_STD = 5_000.0; // COP estándar

    /** Almacén principal de pedidos. DoublyLinkedList del JAR. */
    private final LinkedList<Order> pedidos;

    public OrderService() {
        this.pedidos = new LinkedList<>();
    }

    // ── RF-02: crearPedido ───────────────────────────────────────────────────

    /**
     * Crea un pedido nuevo en estado {@link EstadoPedido#PENDIENTE}.
     *
     * @param cedulaCliente cédula del cliente propietario
     * @param isPremium     {@code true} si el cliente tiene membresía premium
     * @return pedido creado con UUID asignado
     */
    @Override
    public Order crearPedido(String cedulaCliente, boolean isPremium) throws RemoteException {
        if (cedulaCliente == null || cedulaCliente.isBlank()) {
            throw new IllegalArgumentException("La cédula del cliente es requerida.");
        }
        Order order = new Order(cedulaCliente, isPremium);
        pedidos.add(order);
        return order;
    }

    /**
     * Envía pedido a cocina cambiando estado a {@link EstadoPedido#EN_PREPARACION}.
     * Requiere estado PENDIENTE y al menos un producto.
     *
     * @param pedido pedido a enviar
     */
    @Override
    public void enviarPedidoACocina(Order pedido) throws RemoteException {
        if (pedido == null) throw new IllegalArgumentException("Pedido nulo.");
        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException(
                "El pedido debe estar PENDIENTE. Estado actual: " + pedido.getEstado());
        }
        if (pedido.getCantProductos() == 0) {
            throw new IllegalStateException(
                "No se puede enviar a cocina un pedido sin productos.");
        }
        pedido.setEstado(EstadoPedido.EN_PREPARACION);
        actualizar(pedido);
    }

    // ── RF-03: calcularFactura ───────────────────────────────────────────────

    /**
     * Calcula y aplica la factura completa del pedido.
     *
     * <pre>
     * subtotal  = Σ (precio × cantidad) por producto disponible
     * IVA       = subtotal × 19%
     * domicilio = isPremium ? 0 : 5.000 COP
     * total     = subtotal + IVA + domicilio
     * </pre>
     *
     * @param pedido pedido a facturar
     * @return total en COP
     */
    @Override
    public double calcularFactura(Order pedido) throws RemoteException {
        if (pedido == null) throw new IllegalArgumentException("Pedido nulo.");

        double subtotal = 0.0;
        Iterator<Product> it = pedido.getCarrito().iterator();
        while (it.hasNext()) {
            Product p = it.next();
            if (p != null && p.isDisponible()) {
                subtotal += (double) p.getPrecio() * p.getCantidad();
            }
        }

        double iva   = subtotal * IVA;
        double domi  = pedido.isPremium() ? 0.0 : COSTO_DOMI_STD;
        double total = subtotal + iva + domi;

        pedido.setSubtotal(subtotal);
        pedido.setImpuesto(iva);
        pedido.setCostoDomi(domi);
        pedido.setTotal(total);

        actualizar(pedido);
        return total;
    }

    // ── RF-04: buscar / modificar / cancelar ────────────────────────────────

    /**
     * Busca un pedido por su UUID.
     *
     * @param pedidoId UUID del pedido
     * @return pedido encontrado o {@code null}
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
     * Persiste cambios de un pedido. Solo permitido en estado PENDIENTE.
     *
     * @param pedido pedido modificado
     * @return {@code true} si fue actualizado
     */
    @Override
    public boolean modificarPedido(Order pedido) throws RemoteException {
        if (pedido == null) return false;
        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException(
                "Solo modificable en PENDIENTE. Estado: " + pedido.getEstado());
        }
        return actualizar(pedido);
    }

    /**
     * Cancela un pedido. Permitido desde PENDIENTE o EN_PREPARACION.
     *
     * @param pedidoId UUID del pedido a cancelar
     * @return {@code true} si fue cancelado
     */
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

    // ── RF-04: helpers de carrito ────────────────────────────────────────────

    /**
     * Agrega un producto al carrito de un pedido en PENDIENTE.
     *
     * @param pedidoId UUID del pedido
     * @param product  producto a agregar
     * @return {@code true} si fue agregado
     */
    public boolean agregarProducto(String pedidoId, Product product) throws RemoteException {
        Order pedido = buscarPedido(pedidoId);
        if (pedido == null) return false;
        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("Solo se agregan productos en PENDIENTE.");
        }
        pedido.agregarProducto(product);
        return actualizar(pedido);
    }

    /**
     * Quita un producto del carrito de un pedido en PENDIENTE.
     *
     * @param pedidoId UUID del pedido
     * @param product  producto a quitar
     * @return {@code true} si fue encontrado y quitado
     */
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

    /**
     * Retorna todos los pedidos de un cliente. RF-01 (historial).
     *
     * @param cedula cédula del cliente
     * @return lista de pedidos del cliente usando DoublyLinkedList del JAR
     */
    public LinkedList<Order> getPedidosPorCliente(String cedula) {
        LinkedList<Order> resultado = new LinkedList<>();
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

    // ── Helper privado ───────────────────────────────────────────────────────

    /**
     * Reemplaza el pedido en la lista por su versión actualizada.
     * Usa {@code remove(Predicate)} del JAR.
     */
    private boolean actualizar(Order order) {
        boolean removed = pedidos.remove(o -> order.getOrderId().equals(o.getOrderId()));
        if (removed) pedidos.add(order);
        return removed;
    }
}
