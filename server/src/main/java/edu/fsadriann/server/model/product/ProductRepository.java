package edu.fsadriann.server.model.product;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.model.iterator.Iterator;

import java.io.*;

/**
 * Repositorio para la persistencia del catálogo de productos en un archivo JSON.
 */
public class ProductRepository {

    private static final String FILE_NAME = "data/products.json";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private File getFile() {
        java.net.URL url = getClass().getClassLoader().getResource(FILE_NAME);
        if (url != null) {
            try {
                return new File(url.toURI());
            } catch (java.net.URISyntaxException e) {
                return new File(url.getFile());
            }
        }
        File file = new File("server/src/main/resources/" + FILE_NAME);
        file.getParentFile().mkdirs();
        return file;
    }

    /**
     * Lee y retorna todos los productos del archivo JSON.
     *
     * @return lista de productos, o lista vacía si el archivo no existe
     */
    public LinkedList<Product> findAll() {
        try (Reader reader = new FileReader(getFile())) {
            JsonArray array = gson.fromJson(reader, JsonArray.class);
            LinkedList<Product> lista = new LinkedList<>();
            if (array != null) {
                for (int i = 0; i < array.size(); i++) {
                    lista.add(gson.fromJson(array.get(i), Product.class));
                }
            }
            return lista;
        } catch (IOException e) {
            System.err.println("Error leyendo products.json: " + e.getMessage());
            return new LinkedList<>();
        }
    }

    /**
     * Guarda la lista completa de productos en el archivo JSON.
     *
     * @param productos lista de productos a guardar
     */
    public void saveAll(LinkedList<Product> productos) {
        JsonArray array = new JsonArray();
        Iterator<Product> it = productos.iterator();
        while (it.hasNext()) {
            Product p = it.next();
            if (p != null) array.add(gson.toJsonTree(p));
        }
        try (Writer writer = new FileWriter(getFile())) {
            gson.toJson(array, writer);
        } catch (IOException e) {
            System.err.println("Error guardando products.json: " + e.getMessage());
        }
    }

    /**
     * Guarda o actualiza un producto en el archivo JSON.
     *
     * @param product producto a guardar
     */
    public void save(Product product) {
        LinkedList<Product> lista = findAll();
        lista.remove(p -> p.getProductoId().equals(product.getProductoId()));
        lista.add(product);
        saveAll(lista);
    }

    /**
     * Elimina un producto del archivo JSON por su identificador.
     *
     * @param productoId identificador del producto a eliminar
     */
    public void deleteById(String productoId) {
        LinkedList<Product> lista = findAll();
        lista.remove(p -> p.getProductoId().equals(productoId));
        saveAll(lista);
    }
}
