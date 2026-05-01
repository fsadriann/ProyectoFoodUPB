package edu.fsadriann.foodpbproyecto.model.delivery;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;

import java.util.UUID;

/**
 * Registro de asignación de entrega en el sistema Food UPB. RF-13.
 *
 * <p><b>Responsabilidad:</b> conectar un {@code orderId} con un {@code repartidorId},
 * almacenar la ruta calculada en el momento de la asignación (snapshot inmutable) y
 * registrar la marca de tiempo.
 *
 * <p><b>Estado de la entrega:</b> se controla mediante {@link edu.fsadriann.foodpbproyecto.model.order.EstadoPedido}
 * en el {@code Order} asociado. {@code Delivery} no duplica estado para evitar inconsistencias.
 *
 * <ul>
 *   <li>La ruta se captura en {@link DeliveryService#asignarPedidoARepartidor} y no cambia
 *       después — es un snapshot útil para auditoría y futura visualización en Swing.</li>
 *   <li>{@code repartidorId} es un {@code String} en esta fase. Cuando se implemente el
 *       dominio {@code Repartidor}, solo cambia el tipo del campo.</li>
 * </ul>
 *
 * @author fsadriann
 */
public class Delivery {

    /** Identificador único de la asignación (UUID). */
    private final String deliveryId;

    /** UUID del pedido asignado. Clave de búsqueda. */
    private final String orderId;

    /**
     * Identificador del repartidor asignado.
     * Fase actual: String libre. Fase futura: ID del modelo {@code Repartidor}.
     */
    private final String repartidorId;

    /**
     * Ruta calculada por Dijkstra al momento de asignación.
     * Snapshot inmutable — no cambia después de creado el {@code Delivery}.
     */
    private final LinkedList<String> ruta;

    /** Época en milisegundos del momento de asignación. */
    private final long timestamp;

    // ── Constructor ───────────────────────────────────────────────────────────

    /**
     * Crea un registro de entrega. Solo lo invoca {@link DeliveryService}.
     *
     * @param orderId       UUID del pedido asociado
     * @param repartidorId  identificador del repartidor
     * @param ruta          ruta calculada por Dijkstra desde base hasta cuadranteDestino
     */
    public Delivery(String orderId, String repartidorId, LinkedList<String> ruta) {
        this.deliveryId    = UUID.randomUUID().toString();
        this.orderId       = orderId;
        this.repartidorId  = repartidorId;
        this.ruta          = ruta;
        this.timestamp     = System.currentTimeMillis();
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    /** @return identificador único de esta asignación de entrega */
    public String getDeliveryId()     { return deliveryId; }

    /** @return UUID del pedido que está siendo entregado */
    public String getOrderId()        { return orderId; }

    /** @return identificador del repartidor asignado */
    public String getRepartidorId()   { return repartidorId; }

    /** @return ruta de cuadrantes calculada al momento de la asignación */
    public LinkedList<String> getRuta() { return ruta; }

    /** @return marca de tiempo de creación de la asignación (epoch ms) */
    public long getTimestamp()        { return timestamp; }

    @Override
    public String toString() {
        return "Delivery{" +
                "id='" + deliveryId + '\'' +
                ", order='" + orderId + '\'' +
                ", repartidor='" + repartidorId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
