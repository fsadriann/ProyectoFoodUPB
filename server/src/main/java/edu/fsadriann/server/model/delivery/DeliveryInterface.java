package edu.fsadriann.server.model.delivery;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Contrato remoto para la gestión de entregas a domicilio.
 */
public interface DeliveryInterface extends Remote {

    /**
     * Asigna un pedido listo a un repartidor e inicia el proceso de entrega.
     *
     * @param orderId       identificador del pedido a asignar
     * @param repartidorId  identificador del repartidor
     * @return registro de la entrega creada, o {@code null} si no fue posible
     */
    Delivery asignarPedidoARepartidor(String orderId,
                                       String repartidorId) throws RemoteException;

    /**
     * Marca el inicio del trayecto de entrega de un pedido.
     *
     * @param orderId identificador del pedido
     * @return {@code true} si el cambio de estado fue exitoso
     */
    boolean iniciarEntrega(String orderId) throws RemoteException;

    /**
     * Registra la entrega como completada.
     *
     * @param orderId identificador del pedido
     * @return {@code true} si el cambio de estado fue exitoso
     */
    boolean completarEntrega(String orderId) throws RemoteException;

    /**
     * Busca el registro de entrega asociado a un pedido.
     *
     * @param orderId identificador del pedido
     * @return registro de entrega, o {@code null} si no está asignado
     */
    Delivery buscarEntregaPorPedido(String orderId) throws RemoteException;

    /**
     * Indica si un pedido ya tiene un repartidor asignado.
     *
     * @param orderId identificador del pedido
     * @return {@code true} si ya está asignado
     */
    boolean yaEstaAsignado(String orderId) throws RemoteException;
}
