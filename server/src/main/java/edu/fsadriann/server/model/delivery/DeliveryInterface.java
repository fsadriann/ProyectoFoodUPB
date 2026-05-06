package edu.fsadriann.server.model.delivery;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DeliveryInterface extends Remote {
    Delivery asignarPedidoARepartidor(String orderId, String repartidorId) throws RemoteException;
    boolean  iniciarEntrega(String orderId)                                 throws RemoteException;
    boolean  completarEntrega(String orderId)                               throws RemoteException;
    Delivery buscarEntregaPorPedido(String orderId)                         throws RemoteException;
    boolean  yaEstaAsignado(String orderId)                                 throws RemoteException;
}