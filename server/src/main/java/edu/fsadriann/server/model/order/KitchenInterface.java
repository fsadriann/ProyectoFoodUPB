package edu.fsadriann.server.model.order;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface KitchenInterface extends Remote {

    void encolarPedido(Order pedido) throws RemoteException;

    Order procesarSiguientePedido() throws RemoteException;

    boolean marcarPedidoListo(String pedidoId) throws RemoteException;

    String obtenerEstadoFogones() throws RemoteException;

    int tamanoCola() throws RemoteException;

    boolean colaVacia() throws RemoteException;

    java.util.List<Order> procesarPedidosDisponibles() throws RemoteException;

}
