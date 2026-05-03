package edu.fsadriann.server.model.product;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import java.rmi.RemoteException;

public interface ProductInterface {
    boolean agregarProducto(Product product) throws RemoteException;
    boolean editarProducto(Product product) throws RemoteException;
    boolean desactivarProducto(String productoId) throws RemoteException;
    Product buscarProducto(String productoId) throws RemoteException;
    LinkedList<Product> listarProductos() throws RemoteException;
    LinkedList<Product> buscarProductoPorNombre(String nombre) throws RemoteException;
}
