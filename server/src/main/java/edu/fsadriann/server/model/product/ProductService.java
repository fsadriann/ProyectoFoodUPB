package edu.fsadriann.server.model.product;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.model.iterator.Iterator;

import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

public class ProductService implements ProductInterface {

    private final LinkedList<Product> catalogo;
    private final ProductRepository repository = new ProductRepository();

    public ProductService() {
        this.catalogo = new LinkedList<>();
        cargarCatalogoInicial();
    }

    private void cargarCatalogoInicial() {
        List<Product> productos = repository.findAll();
        for (Product p : productos) {
            catalogo.add(p);
        }
    }

    @Override
    public boolean agregarProducto(Product product) throws RemoteException {
        if (product == null || product.getProductoId() == null) return false;
        if (buscarProducto(product.getProductoId()) != null) return false;
        catalogo.add(product);
        repository.save(product);
        return true;
    }

    @Override
    public boolean editarProducto(Product product) throws RemoteException {
        if (product == null || product.getProductoId() == null) return false;
        if (buscarProducto(product.getProductoId()) == null) return false;
        catalogo.remove(p -> product.getProductoId().equals(p.getProductoId()));
        catalogo.add(product);
        repository.save(product);
        return true;
    }

    @Override
    public boolean desactivarProducto(String productoId) throws RemoteException {
        if (productoId == null) return false;
        Product p = buscarProducto(productoId);
        if (p == null) return false;
        p.setDisponible(false);
        repository.save(p);
        return true;
    }

    @Override
    public boolean activarProducto(String productoId) throws RemoteException {
        if (productoId == null) return false;
        Product p = buscarProducto(productoId);
        if (p == null) return false;
        p.setDisponible(true);
        repository.save(p);
        return true;
    }

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

    @Override
    public LinkedList<Product> listarProductos() throws RemoteException {
        return catalogo;
    }

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