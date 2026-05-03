package edu.fsadriann.server.model.order;

/**
 * Estados del ciclo de vida de un pedido.
 *
 * <p>Transiciones válidas:
 * <pre>
 *  PENDIENTE → EN_PREPARACION → LISTO → EN_CAMINO → ENTREGADO
 *  PENDIENTE → CANCELADO
 *  EN_PREPARACION → CANCELADO
 * </pre>
 *
 * @author fsadriann
 */
public enum EstadoPedido {

    PENDIENTE,
    EN_PREPARACION,
    LISTO,
    EN_CAMINO,
    ENTREGADO,
    CANCELADO
}
