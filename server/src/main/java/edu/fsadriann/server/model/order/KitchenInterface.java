package edu.fsadriann.server.model.order;

/**
 * Contrato del servicio de cocina con gestión real de estaciones. RF-05.
 *
 * <p><b>Modelo de operación:</b>
 * <ol>
 *   <li>Los pedidos se encolan en una {@code PriorityQueue} (premium=0, estándar=1).</li>
 *   <li>{@link #procesarSiguientePedido()} intenta asignar el pedido de mayor prioridad
 *       al primer fogón compatible libre.</li>
 *   <li>Si el fogón compatible está ocupado, el pedido <em>espera en cola</em>.</li>
 *   <li>{@link #marcarPedidoListo(String)} libera el fogón y dispara automáticamente
 *       el procesamiento del siguiente pedido en cola.</li>
 * </ol>
 *
 * <p><b>Reglas de asignación:</b>
 * <ul>
 *   <li>Pedido con algún producto complejo → {@link FogonCocina#FOGON_GRANDE}</li>
 *   <li>Pedido todo simple → primer {@code FOGON_NORMAL} libre (1 → 2 → 3)</li>
 * </ul>
 *
 * @author fsadriann
 */
public interface KitchenInterface {

    /**
     * Encola un pedido según prioridad (premium=0, estándar=1).
     *
     * @param pedido pedido a encolar
     * @throws IllegalArgumentException si el pedido es nulo
     * @throws IllegalStateException    si el estado es CANCELADO o ENTREGADO
     */
    void encolarPedido(Order pedido);

    /**
     * Intenta asignar el pedido de mayor prioridad a un fogón compatible libre.
     *
     * <ul>
     *   <li>Complejo + {@link FogonCocina#FOGON_GRANDE} libre → asigna y retorna el pedido.</li>
     *   <li>Simple + algún {@code FOGON_NORMAL} libre → asigna y retorna el pedido.</li>
     *   <li>Fogón compatible ocupado → no extrae de la cola, retorna {@code null}.</li>
     * </ul>
     *
     * @return pedido asignado (ahora en {@code EN_PREPARACION}), o {@code null} si no pudo asignar
     */
    Order procesarSiguientePedido();

    /**
     * Marca un pedido como listo, libera su fogón y procesa el siguiente en cola.
     *
     * <p>Transición válida: {@code EN_PREPARACION → LISTO}
     *
     * @param pedidoId UUID del pedido
     * @return {@code true} si el pedido fue marcado correctamente
     * @throws IllegalArgumentException si pedidoId es nulo
     * @throws IllegalStateException    si el pedido no está en ningún fogón
     *                                  o no está en {@code EN_PREPARACION}
     */
    boolean marcarPedidoListo(String pedidoId);

    /**
     * Retorna una descripción del estado de ocupación de cada fogón.
     * Útil para depuración y visualización en UI.
     *
     * @return texto con estado de los 4 fogones
     */
    String obtenerEstadoFogones();

    /** @return número de pedidos en cola (sin contar los ya asignados a fogones) */
    int tamanoCola();

    /** @return {@code true} si no hay pedidos esperando en la cola */
    boolean colaVacia();
}
