package edu.fsadriann.server.model.order;

import edu.fsadriann.server.model.product.Product;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Contrato del servicio de pedidos.
 *
 * <ul>
 * <li>RF-02 – Crear y enviar a cocina</li>
 * <li>RF-03 – Facturación</li>
 * <li>RF-04 – Buscar, modificar, cancelar</li>
 * <li>RF-13 – Asignación de cuadrante y cálculo de ruta de entrega</li>
 * </ul>
 *
 * @author fsadriann
 */
public interface OrderInterface extends Remote {
    Order crearPedido(String cedulaCliente, boolean isPremium) throws RemoteException; // RF-02

    void enviarPedidoACocina(Order pedido) throws RemoteException; // RF-02

    boolean agregarProducto(String pedidoId, Product product) throws RemoteException;

    boolean quitarProducto(String pedidoId, Product product) throws RemoteException;

    Order buscarPedido(String pedidoId) throws RemoteException; // RF-04

    boolean modificarPedido(Order pedido) throws RemoteException; // RF-04

    boolean cancelarPedido(String pedidoId) throws RemoteException; // RF-04

    double calcularFactura(Order pedido) throws RemoteException; // RF-03

    edu.fsadriann.app.linkedlist.singly.singly.LinkedList<Order> getPedidosPorCliente(String cedula) throws RemoteException;

    // ── RF-13 — Rutas de entrega ──────────────────────────────────────────────

    /**
     * Asigna el cuadrante de destino a un pedido existente.
     *
     * @param orderId         UUID del pedido
     * @param nombreCuadrante nombre del cuadrante destino (debe existir en
     *                        {@code cs})
     * @param cs              servicio de cuadrantes para validar existencia
     * @return {@code true} si la asignación fue exitosa; {@code false} si el pedido
     *         no existe, el cuadrante no existe, o el pedido está CANCELADO
     * @throws IllegalArgumentException si {@code nombreCuadrante} es nulo o vacío
     */

    boolean asignarCuadranteDestino(String orderId, String nombreCuadrante) throws RemoteException;

    /**
     * Calcula la ruta óptima de entrega desde la base
     * ({@value OrderService#ORIGEN_BASE})
     * hasta el cuadrante de destino del pedido, usando Dijkstra.
     *
     * @param orderId UUID del pedido con cuadranteDestino ya asignado
     * @return lista ordenada de nombres de cuadrantes en la ruta óptima,
     *         o {@code null} si el pedido no existe, está cancelado,
     *         no tiene destino asignado, o no hay ruta disponible
     */
    edu.fsadriann.app.linkedlist.singly.singly.LinkedList<String> calcularRutaEntrega(String orderId) throws RemoteException;
    // ── RF-13-C / RF-13-D — Transiciones de estado para entrega ──────────────

    /**
     * Transición estricta {@link EstadoPedido#LISTO} →
     * {@link EstadoPedido#EN_CAMINO}.
     * Invocado por {@code DeliveryService.iniciarEntrega()} cuando el repartidor
     * comienza el trayecto.
     *
     * @param orderId UUID del pedido
     * @return {@code true} si la transición fue exitosa
     * @throws IllegalStateException si el estado actual no es LISTO
     */
    boolean marcarEnCamino(String orderId) throws RemoteException;

    /**
     * Transición estricta {@link EstadoPedido#EN_CAMINO} →
     * {@link EstadoPedido#ENTREGADO}.
     * Invocado por {@code DeliveryService.completarEntrega()} al confirmar la
     * entrega.
     *
     * @param orderId UUID del pedido
     * @return {@code true} si la transición fue exitosa
     * @throws IllegalStateException si el estado actual no es EN_CAMINO
     */
    boolean marcarEntregado(String orderId) throws RemoteException;

    /**
     * Transición estricta {@link EstadoPedido#EN_PREPARACION} →
     * {@link EstadoPedido#LISTO}.
     * Invocado por {@code KitchenService.marcarPedidoListo()} en modo coordinado
     * para
     * formalizar el cambio de estado en {@code OrderService} (fuente de verdad
     * central).
     *
     * <p>
     * En la implementación en memoria refuerza el estado ya aplicado por
     * {@code KitchenService}
     * sobre el objeto compartido. Con MongoDB/RMI garantizará la actualización
     * persistente.
     *
     * @param orderId UUID del pedido
     * @return {@code true} si la transición fue exitosa; {@code false} si el pedido
     *         no existe
     * @throws IllegalStateException si el estado actual no es EN_PREPARACION
     */
    boolean marcarListo(String orderId) throws RemoteException;
}
