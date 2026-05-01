package edu.fsadriann.foodpbproyecto.model.delivery;

import edu.fsadriann.foodpbproyecto.model.cuadrante.CuadranteInterface;
import edu.fsadriann.foodpbproyecto.model.order.OrderService;

/**
 * Contrato del servicio de entregas. RF-13.
 *
 * <p><b>Flujo de operación:</b>
 * <ol>
 *   <li>Pedido llega a {@link edu.fsadriann.foodpbproyecto.model.order.EstadoPedido#LISTO}
 *       desde {@code KitchenService}.</li>
 *   <li>{@link #asignarPedidoARepartidor} valida, calcula ruta y crea el registro
 *       {@link Delivery}. El pedido sigue en LISTO.</li>
 *   <li>{@link #iniciarEntrega} mueve el pedido a EN_CAMINO.</li>
 *   <li>{@link #completarEntrega} mueve el pedido a ENTREGADO.</li>
 * </ol>
 *
 * <p><b>Sin checked exceptions:</b> los errores del JAR y de {@code OrderService}
 * se absorben internamente. Retorna {@code null/false} en vez de propagar excepciones.
 * {@link IllegalArgumentException} se lanza para entradas inválidas (nulos, vacíos).
 *
 * @author fsadriann
 */
public interface DeliveryInterface {

    // ── Asignación ────────────────────────────────────────────────────────────

    /**
     * Asigna un repartidor a un pedido LISTO y calcula su ruta de entrega.
     *
     * <p>Precondiciones:
     * <ul>
     *   <li>El pedido debe existir en {@code os}.</li>
     *   <li>El pedido debe estar en {@link edu.fsadriann.foodpbproyecto.model.order.EstadoPedido#LISTO}.</li>
     *   <li>El pedido no debe estar CANCELADO.</li>
     *   <li>El pedido debe tener {@code cuadranteDestino} asignado.</li>
     *   <li>Debe existir ruta en el grafo entre la base y el destino.</li>
     *   <li>El pedido no debe estar ya asignado (no duplicar).</li>
     * </ul>
     *
     * @param orderId       UUID del pedido a asignar
     * @param repartidorId  identificador del repartidor
     * @param cs            servicio de cuadrantes (para calcular ruta)
     * @param os            servicio de pedidos (para validar estado)
     * @return {@link Delivery} creado, o {@code null} si alguna precondición falla
     * @throws IllegalArgumentException si {@code repartidorId} es nulo o vacío
     */
    Delivery asignarPedidoARepartidor(String orderId, String repartidorId,
                                      CuadranteInterface cs, OrderService os);

    // ── Ciclo de vida ─────────────────────────────────────────────────────────

    /**
     * Inicia la entrega: {@link edu.fsadriann.foodpbproyecto.model.order.EstadoPedido#LISTO}
     * → {@link edu.fsadriann.foodpbproyecto.model.order.EstadoPedido#EN_CAMINO}.
     *
     * <p>Requiere que el pedido esté asignado previamente con
     * {@link #asignarPedidoARepartidor}.
     *
     * @param orderId UUID del pedido
     * @param os      servicio de pedidos
     * @return {@code true} si la transición fue exitosa
     * @throws IllegalStateException si el pedido no está en LISTO
     */
    boolean iniciarEntrega(String orderId, OrderService os);

    /**
     * Completa la entrega: {@link edu.fsadriann.foodpbproyecto.model.order.EstadoPedido#EN_CAMINO}
     * → {@link edu.fsadriann.foodpbproyecto.model.order.EstadoPedido#ENTREGADO}.
     *
     * <p>Requiere que el pedido esté en EN_CAMINO (es decir, que
     * {@link #iniciarEntrega} haya sido llamado antes).
     *
     * @param orderId UUID del pedido
     * @param os      servicio de pedidos
     * @return {@code true} si la transición fue exitosa
     * @throws IllegalStateException si el pedido no está en EN_CAMINO
     */
    boolean completarEntrega(String orderId, OrderService os);

    // ── Consultas ─────────────────────────────────────────────────────────────

    /**
     * Busca el registro de entrega asociado a un pedido.
     *
     * @param orderId UUID del pedido
     * @return {@link Delivery} asociado, o {@code null} si no existe asignación
     */
    Delivery buscarEntregaPorPedido(String orderId);

    /**
     * Verifica si un pedido ya tiene un repartidor asignado.
     *
     * @param orderId UUID del pedido
     * @return {@code true} si ya existe una asignación para este pedido
     */
    boolean yaEstaAsignado(String orderId);
}
