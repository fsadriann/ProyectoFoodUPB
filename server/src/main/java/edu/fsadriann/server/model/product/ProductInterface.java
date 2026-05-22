package edu.fsadriann.server.model.product;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Contrato remoto para la gestión del catálogo de productos de Food UPB.
 */
public interface ProductInterface extends Remote {

    /**
     * Registra un nuevo producto en el catálogo.
     *
     * @param product producto a agregar
     * @return {@code true} si se registró exitosamente
     */
    boolean agregarProducto(Product product) throws RemoteException;

    /**
     * Actualiza los datos de un producto existente.
     *
     * @param product producto con datos actualizados
     * @return {@code true} si se editó exitosamente
     */
    boolean editarProducto(Product product) throws RemoteException;

    /**
     * Desactiva un producto para que no aparezca en el menú.
     *
     * @param productoId identificador del producto
     * @return {@code true} si se desactivó exitosamente
     */
    boolean desactivarProducto(String productoId) throws RemoteException;

    /**
     * Reactiva un producto previamente desactivado.
     *
     * @param productoId identificador del producto
     * @return {@code true} si se activó exitosamente
     */
    boolean activarProducto(String productoId) throws RemoteException;

    /**
     * Busca un producto por su identificador.
     *
     * @param productoId identificador del producto
     * @return el producto encontrado, o {@code null} si no existe
     */
    Product buscarProducto(String productoId) throws RemoteException;

    /**
     * Retorna todos los productos del catálogo.
     *
     * @return lista completa de productos
     */
    LinkedList<Product> listarProductos() throws RemoteException;

    /**
     * Busca productos cuyo nombre contenga el texto indicado.
     *
     * @param nombre texto a buscar (no distingue mayúsculas)
     * @return lista de productos que coinciden
     */
    LinkedList<Product> buscarProductoPorNombre(String nombre) throws RemoteException;
}
