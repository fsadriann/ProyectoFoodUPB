package edu.fsadriann.server.model.order;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.server.model.product.Product;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface OrderInterface extends Remote {
    Order crearPedido(String cedulaCliente, boolean isPremium) throws RemoteException; // RF-02
    void enviarPedidoACocina(Order pedido) throws RemoteException; // RF-02
    boolean agregarProducto(String pedidoId, Product product) throws RemoteException;
    boolean quitarProducto(String pedidoId, Product product) throws RemoteException;
    Order buscarPedido(String pedidoId) throws RemoteException; // RF-04
    boolean modificarPedido(Order pedido) throws RemoteException; // RF-04
    boolean cancelarPedido(String pedidoId) throws RemoteException; // RF-04
    double calcularFactura(Order pedido) throws RemoteException; // RF-03
    edu.fsadriann.app.linkedlist.singly.singly.LinkedList<Order> getPedidosPorCliente(String cedula) throws RemoteException;
    boolean asignarCuadranteDestino(String orderId, String nombreCuadrante) throws RemoteException;
    edu.fsadriann.app.linkedlist.singly.singly.LinkedList<String> calcularRutaEntrega(String orderId) throws RemoteException;
    boolean marcarEnCamino(String orderId) throws RemoteException;
    boolean marcarEntregado(String orderId) throws RemoteException;
    boolean marcarListo(String orderId) throws RemoteException;
    LinkedList<Order> getPedidosEnPreparacion() throws RemoteException;
    LinkedList<Order> listarTodosPedidos() throws RemoteException;
}

