package edu.fsadriann.server.model.product;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.model.iterator.Iterator;

import java.rmi.RemoteException;

/**
 * Servicio que gestiona el catálogo de productos de Food UPB.
 * Carga los productos desde el archivo JSON al iniciar y persiste los cambios automáticamente.
 */
public class ProductService implements ProductInterface {

    private final LinkedList<Product> catalogo;
    private final ProductRepository   repository = new ProductRepository();

    /**
     * Crea el servicio y carga el catálogo inicial desde el archivo JSON.
     */
    public ProductService() {
        this.catalogo = new LinkedList<>();
        cargarCatalogoInicial();
    }

    private void cargarCatalogoInicial() {
        LinkedList<Product> productos = repository.findAll();
        Iterator<Product> it = productos.iterator();
        while (it.hasNext()) {
            Product p = it.next();
            if (p != null) catalogo.add(p);
        }
    }

    /**
     * Registra un nuevo producto en el catálogo.
     *
     * @param product producto a agregar
     * @return {@code true} si se registró exitosamente; {@code false} si ya existe
     */
    @Override
    public boolean agregarProducto(Product product) throws RemoteException {
        if (product == null || product.getProductoId() == null) return false;
        if (buscarProducto(product.getProductoId()) != null) return false;
        catalogo.add(product);
        repository.save(product);
        return true;
    }

    /**
     * Actualiza los datos de un producto existente en el catálogo.
     *
     * @param product producto con datos actualizados
     * @return {@code true} si se editó exitosamente; {@code false} si no existe
     */
    @Override
    public boolean editarProducto(Product product) throws RemoteException {
        if (product == null || product.getProductoId() == null) return false;
        if (buscarProducto(product.getProductoId()) == null) return false;
        catalogo.remove(p -> product.getProductoId().equals(p.getProductoId()));
        catalogo.add(product);
        repository.save(product);
        return true;
    }

    /**
     * Desactiva un producto para que no aparezca en el menú activo.
     *
     * @param productoId identificador del producto
     * @return {@code true} si se desactivó exitosamente
     */
    @Override
    public boolean desactivarProducto(String productoId) throws RemoteException {
        if (productoId == null) return false;
        Product p = buscarProducto(productoId);
        if (p == null) return false;
        p.setDisponible(false);
        repository.save(p);
        return true;
    }

    /**
     * Reactiva un producto previamente desactivado.
     *
     * @param productoId identificador del producto
     * @return {@code true} si se activó exitosamente
     */
    @Override
    public boolean activarProducto(String productoId) throws RemoteException {
        if (productoId == null) return false;
        Product p = buscarProducto(productoId);
        if (p == null) return false;
        p.setDisponible(true);
        repository.save(p);
        return true;
    }

    /**
     * Busca un producto por su identificador único.
     *
     * @param productoId identificador del producto
     * @return el producto encontrado, o {@code null} si no existe
     */
    @Override
    public Product buscarProducto(String productoId) throws RemoteException {
        if (productoId == null) return null;
        Iterator<Product> it = catalogo.iterator();
        while (it.hasNext()) {
            Product p = it.next();
            if (p != null && productoId.equals(p.getProductoId())) return p;
        }
        return null;
    }

    /**
     * Busca productos cuyo nombre contenga el texto indicado (sin distinguir mayúsculas).
     *
     * @param nombre texto a buscar en el nombre del producto
     * @return lista de productos que coinciden con la búsqueda
     */
    @Override
    public LinkedList<Product> buscarProductoPorNombre(String nombre) throws RemoteException {
        LinkedList<Product> resultado = new LinkedList<>();
        if (nombre == null || nombre.isBlank()) return resultado;
        String query = nombre.trim().toLowerCase();
        Iterator<Product> it = catalogo.iterator();
        while (it.hasNext()) {
            Product p = it.next();
            if (p != null && p.getNombre().toLowerCase().contains(query)) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    /**
     * Retorna todos los productos del catálogo (activos e inactivos).
     *
     * @return lista completa de productos
     */
    @Override
    public LinkedList<Product> listarProductos() throws RemoteException {
        return catalogo;
    }

    /**
     * Retorna únicamente los productos que están disponibles en el menú.
     *
     * @return lista de productos con {@code disponible = true}
     */
    public LinkedList<Product> listarProductosDisponibles() throws RemoteException {
        LinkedList<Product> disponibles = new LinkedList<>();
        Iterator<Product> it = catalogo.iterator();
        while (it.hasNext()) {
            Product p = it.next();
            if (p != null && p.isDisponible()) disponibles.add(p);
        }
        return disponibles;
    }
}
