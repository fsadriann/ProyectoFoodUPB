package edu.fsadriann.server.model.product;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.model.iterator.Iterator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ProductRepositoryTest {

    @Test
    void debeCargarProductosDesdeJson() {
        ProductRepository repo = new ProductRepository();
        LinkedList<Product> productos = repo.findAll();

        assertFalse(productos.isEmpty(), "La lista no debe estar vacía");
        System.out.println("Productos cargados: " + productos.size());
        Iterator<Product> it = productos.iterator();
        while (it.hasNext()) System.out.println(" - " + it.next());
    }

    @Test
    void debeGuardarYLeerUnProducto() {
        ProductRepository repo = new ProductRepository();

        Product nuevo = new Product("test-99", "Producto Test", "BEBIDA", 1000, false);
        repo.save(nuevo);

        LinkedList<Product> productos = repo.findAll();
        boolean encontrado = false;
        Iterator<Product> it = productos.iterator();
        while (it.hasNext()) {
            Product p = it.next();
            if (p != null && "test-99".equals(p.getProductoId())) {
                encontrado = true;
                break;
            }
        }

        assertTrue(encontrado, "El producto guardado debe aparecer en el JSON");

        repo.deleteById("test-99");
    }
}
