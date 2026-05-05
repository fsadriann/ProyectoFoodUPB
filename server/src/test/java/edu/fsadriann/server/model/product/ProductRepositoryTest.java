package edu.fsadriann.server.model.product;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ProductRepositoryTest {

    @Test
    void debeCargarProductosDesdeJson() {
        ProductRepository repo = new ProductRepository();
        List<Product> productos = repo.findAll();

        assertFalse(productos.isEmpty(), "La lista no debe estar vacía");
        System.out.println("Productos cargados: " + productos.size());
        productos.forEach(p -> System.out.println(" - " + p));
    }

    @Test
    void debeGuardarYLeerUnProducto() {
        ProductRepository repo = new ProductRepository();

        Product nuevo = new Product("test-99", "Producto Test", "BEBIDA", 1000, false);
        repo.save(nuevo);

        List<Product> productos = repo.findAll();
        boolean encontrado = productos.stream()
                .anyMatch(p -> "test-99".equals(p.getProductoId()));

        assertTrue(encontrado, "El producto guardado debe aparecer en el JSON");

        // limpia después del test
        repo.deleteById("test-99");
    }
}