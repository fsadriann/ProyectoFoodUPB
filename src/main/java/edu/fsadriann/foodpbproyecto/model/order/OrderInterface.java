package edu.fsadriann.foodpbproyecto.model.order;

import java.rmi.RemoteException;

public interface OrderInterface {
    Order crearPedido(String cedulaCliente, boolean isPremium) throws RemoteException;  // RF-02
    void enviarPedidoACocina(Order pedido) throws RemoteException;                      // RF-02
    Order buscarPedido(String pedidoId) throws RemoteException;                         // RF-04
    boolean modificarPedido(Order pedido) throws RemoteException;                       // RF-04
    boolean cancelarPedido(String pedidoId) throws RemoteException;                     // RF-04
    double calcularFactura(Order pedido) throws RemoteException;
}
