package edu.fsadriann.server.model.order;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Contrato remoto para la gestión de la cola de cocina y el estado de los fogones.
 */
public interface KitchenInterface extends Remote {

    /**
     * Agrega un pedido a la cola de cocina según su prioridad (premium o estándar).
     *
     * @param pedido pedido a encolar
     */
    void encolarPedido(Order pedido) throws RemoteException;

    /**
     * Extrae y asigna el siguiente pedido de mayor prioridad a un fogón disponible.
     *
     * @return el pedido asignado, o {@code null} si no hay fogón libre
     */
    Order procesarSiguientePedido() throws RemoteException;

    /**
     * Marca un pedido en preparación como listo y libera el fogón que ocupaba.
     *
     * @param pedidoId identificador del pedido
     * @return {@code true} si se marcó exitosamente
     */
    boolean marcarPedidoListo(String pedidoId) throws RemoteException;

    /**
     * Retorna el estado actual de los cuatro fogones de cocina.
     *
     * @return texto con el estado de cada fogón
     */
    String obtenerEstadoFogones() throws RemoteException;

    /**
     * @return cantidad de pedidos en espera en la cola
     */
    int tamanoCola() throws RemoteException;

    /**
     * @return {@code true} si no hay pedidos en la cola
     */
    boolean colaVacia() throws RemoteException;

    /**
     * Procesa todos los pedidos de la cola que puedan asignarse a un fogón libre.
     *
     * @return lista de pedidos que fueron asignados a fogones
     */
    LinkedList<Order> procesarPedidosDisponibles() throws RemoteException;
}
