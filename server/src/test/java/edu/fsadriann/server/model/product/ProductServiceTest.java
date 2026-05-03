package edu.fsadriann.server.model.product;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.model.iterator.Iterator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.*;

class ProductServiceTest {

    private ProductService service;

    @BeforeEach
    void setUp() {
        service = new ProductService();
    }

    private Product crearProductoTest(String id, String nombre) {
        return new Product(id, nombre, "ENTRADA", 9000, false);
    }

    private int contarElementos(LinkedList<Product> lista) {
        int count = 0;
        Iterator<Product> it = lista.iterator();
        while (it.hasNext()) { it.next(); count++; }
        return count;
    }

    private boolean contieneNombre(LinkedList<Product> lista, String nombre) {
        Iterator<Product> it = lista.iterator();
        while (it.hasNext()) {
            Product p = it.next();
            if (p != null && nombre.equalsIgnoreCase(p.getNombre())) return true;
        }
        return false;
    }

    @Test
    void registrarProducto_exitoso() throws RemoteException {
        Product nuevo = crearProductoTest("prod-test-01", "Empanada");
        assertTrue(service.agregarProducto(nuevo));
        assertNotNull(service.buscarProducto("prod-test-01"));
    }

    @Test
    void registrarProducto_falla_duplicado() throws RemoteException {
        Product p = crearProductoTest("prod-dup-01", "Arepa");
        service.agregarProducto(p);
        assertFalse(service.agregarProducto(p));
    }

    @Test
    void buscarProductoPorNombre_existente() throws RemoteException {
        LinkedList<Product> resultado = service.buscarProductoPorNombre("Hamburguesa");
        assertTrue(contarElementos(resultado) > 0);
        assertTrue(contieneNombre(resultado, "Hamburguesa"));
    }

    @Test
    void buscarProductoPorNombre_inexistente() throws RemoteException {
        LinkedList<Product> resultado = service.buscarProductoPorNombre("ProductoQueNoExiste");
        assertEquals(0, contarElementos(resultado));
    }

    @Test
    void listarProductos_noVacio() throws RemoteException {
        LinkedList<Product> lista = service.listarProductos();
        assertTrue(contarElementos(lista) > 0);
    }

    @Test
    void listarProductos_contieneCatalogoInicial() throws RemoteException {
        LinkedList<Product> lista = service.listarProductos();
        assertTrue(contieneNombre(lista, "Bandeja Paisa"));
        assertTrue(contieneNombre(lista, "Hamburguesa"));
        assertTrue(contieneNombre(lista, "Pizza"));
        assertTrue(contieneNombre(lista, "Gaseosa"));
        assertTrue(contieneNombre(lista, "Papas Fritas"));
    }
    @Test
    void actualizarProducto_exitoso() throws RemoteException {
        Product original = crearProductoTest("prod-upd-01", "Jugo Natural");
        service.agregarProducto(original);

        Product actualizado = new Product("prod-upd-01", "Jugo Natural Premium",
                "BEBIDA", 8500, false);
        assertTrue(service.editarProducto(actualizado));

        Product encontrado = service.buscarProducto("prod-upd-01");
        assertNotNull(encontrado);
        assertEquals("Jugo Natural Premium", encontrado.getNombre());
        assertEquals(8500, encontrado.getPrecio());
    }

    @Test
    void actualizarProducto_falla_inexistente() throws RemoteException {
        Product fantasma = crearProductoTest("id-no-existe", "Fantasma");
        assertFalse(service.editarProducto(fantasma));
    }

    @Test
    void eliminarProducto_exitoso() throws RemoteException {
        Product p = crearProductoTest("prod-del-01", "Sopa del Día");
        service.agregarProducto(p);

        assertTrue(service.desactivarProducto("prod-del-01"));

        Product encontrado = service.buscarProducto("prod-del-01");
        assertNotNull(encontrado);
        assertFalse(encontrado.isDisponible());
    }

    @Test
    void eliminarProducto_falla_inexistente() throws RemoteException {
        assertFalse(service.desactivarProducto("id-que-no-existe"));
    }

    @Test
    void listarDisponibles_filtraCorrectamente() throws RemoteException {
        // Agregar un producto y desactivarlo
        Product activo    = crearProductoTest("prod-act-01", "Tinto");
        Product inactivo  = crearProductoTest("prod-ina-01", "Perico");
        service.agregarProducto(activo);
        service.agregarProducto(inactivo);
        service.desactivarProducto("prod-ina-01");

        LinkedList<Product> disponibles = service.listarProductosDisponibles();

        // El activo debe aparecer
        assertTrue(contieneNombre(disponibles, "Tinto"));

        // El desactivado no debe aparecer
        assertFalse(contieneNombre(disponibles, "Perico"));

        // Ningún elemento de la lista debe estar desactivado
        Iterator<Product> it = disponibles.iterator();
        while (it.hasNext()) {
            Product p = it.next();
            assertTrue(p.isDisponible(), "Producto inactivo filtrado incorrectamente: " + p.getNombre());
        }
    }
}