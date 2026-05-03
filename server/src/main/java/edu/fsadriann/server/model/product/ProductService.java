package edu.fsadriann.server.model.product;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.model.iterator.Iterator;

import java.rmi.RemoteException;
import java.util.UUID;

/**
 * Servicio del catálogo de productos: implementa {@link ProductInterface}.
 *
 * <p>Almacena productos en una {@code SinglyLinkedList} del JAR.
 * El catálogo se inicializa con productos base de Food UPB en el constructor.
 *
 * <ul>
 *   <li>RF-02 – Listar / buscar productos</li>
 *   <li>RF-03 – Registrar / editar / desactivar productos</li>
 * </ul>
 *
 * @author fsadriann
 */
public class ProductService implements ProductInterface {

    private final LinkedList<Product> catalogo;

    public ProductService() {
        this.catalogo = new LinkedList<>();
        cargarCatalogoInicial();
    }

    private void cargarCatalogoInicial() {
        catalogo.add(crearProducto("Bandeja Paisa",  "PLATO_PRINCIPAL", 28000, true,
                "Frijoles, chicharrón, carne molida, chorizo, morcilla, arepa y arroz."));
        catalogo.add(crearProducto("Hamburguesa",    "PLATO_PRINCIPAL", 18000, false,
                "Carne de res, lechuga, tomate, queso y salsa especial."));
        catalogo.add(crearProducto("Pizza",          "PLATO_PRINCIPAL", 22000, true,
                "Pizza personal con salsa de tomate, queso mozzarella e ingredientes a elección."));
        catalogo.add(crearProducto("Gaseosa",        "BEBIDA",          5000,  false,
                "Gaseosa 350 ml a elección."));
        catalogo.add(crearProducto("Papas Fritas",   "ENTRADA",         8000,  false,
                "Porción de papas fritas crujientes con salsa a elección."));
        catalogo.add(crearProducto("Arroz con Pollo","PLATO_PRINCIPAL", 20000, false,
                "Arroz con pollo desmechado, verduras salteadas y ensalada."));
        catalogo.add(crearProducto("Limonada",       "BEBIDA",          6000,  false,
                "Limonada natural o de coco 400 ml."));
        catalogo.add(crearProducto("Brownie",        "POSTRE",          7000,  false,
                "Brownie de chocolate con helado de vainilla."));
    }

    private Product crearProducto(String nombre, String categoria,
                                  int precio, boolean isComplejo, String descripcion) {
        Product p = new Product(UUID.randomUUID().toString(), nombre, categoria, precio, isComplejo);
        p.setDescripcion(descripcion);
        return p;
    }

    @Override
    public boolean agregarProducto(Product product) throws RemoteException {
        if (product == null || product.getProductoId() == null) return false;
        if (buscarProducto(product.getProductoId()) != null) return false;
        catalogo.add(product);
        return true;
    }

    @Override
    public boolean editarProducto(Product product) throws RemoteException {
        if (product == null || product.getProductoId() == null) return false;
        boolean removed = catalogo.remove(p -> product.getProductoId().equals(p.getProductoId()));
        if (removed) catalogo.add(product);
        return removed;
    }

    @Override
    public boolean desactivarProducto(String productoId) throws RemoteException {
        if (productoId == null) return false;
        Product p = buscarProducto(productoId);
        if (p == null) return false;
        p.setDisponible(false);
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