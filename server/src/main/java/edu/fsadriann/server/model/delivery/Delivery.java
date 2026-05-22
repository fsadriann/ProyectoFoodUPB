package edu.fsadriann.server.model.delivery;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;

import java.io.Serializable;
import java.util.UUID;

/**
 * Representa el registro de una entrega a domicilio.
 * Almacena el repartidor asignado, la ruta calculada y el momento de asignación.
 */
public class Delivery implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Identificador único de esta asignación de entrega (UUID). */
    private final String deliveryId;

    /** Identificador del pedido que está siendo entregado. */
    private final String orderId;

    /** Identificador del repartidor asignado. */
    private final String repartidorId;

    /**
     * Ruta calculada por Dijkstra al momento de la asignación.
     * No cambia después de creado el registro de entrega.
     */
    private final LinkedList<String> ruta;

    /** Marca de tiempo de creación de la asignación (epoch ms). */
    private final long timestamp;

    /**
     * Crea un registro de entrega. Solo lo invoca {@link DeliveryService}.
     *
     * @param orderId      identificador del pedido asociado
     * @param repartidorId identificador del repartidor
     * @param ruta         ruta calculada desde UPB hasta el cuadrante de destino
     */
    public Delivery(String orderId, String repartidorId, LinkedList<String> ruta) {
        this.deliveryId   = UUID.randomUUID().toString();
        this.orderId      = orderId;
        this.repartidorId = repartidorId;
        this.ruta         = ruta;
        this.timestamp    = System.currentTimeMillis();
    }

    /** @return identificador único de esta entrega */
    public String getDeliveryId()           { return deliveryId; }

    /** @return identificador del pedido entregado */
    public String getOrderId()              { return orderId; }

    /** @return identificador del repartidor asignado */
    public String getRepartidorId()         { return repartidorId; }

    /** @return ruta de cuadrantes calculada al momento de la asignación */
    public LinkedList<String> getRuta()     { return ruta; }

    /** @return marca de tiempo de creación de la asignación (epoch ms) */
    public long getTimestamp()              { return timestamp; }

    @Override
    public String toString() {
        return "Delivery{" +
                "id='"         + deliveryId   + '\'' +
                ", order='"    + orderId      + '\'' +
                ", repartidor='" + repartidorId + '\'' +
                ", timestamp=" + timestamp          +
                '}';
    }
}
