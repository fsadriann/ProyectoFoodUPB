package edu.fsadriann.server.model.product;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    private static final String FILE_NAME = "data/products.json";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private File getFile() {
        java.net.URL url = getClass().getClassLoader().getResource(FILE_NAME);
        if (url != null) {
            try {
                return new File(url.toURI()); // ← toURI() decodifica los %20
            } catch (java.net.URISyntaxException e) {
                return new File(url.getFile());
            }
        }
        // Fallback
        File file = new File("server/src/main/resources/" + FILE_NAME);
        file.getParentFile().mkdirs();
        return file;
    }

    public List<Product> findAll() {
        try (Reader reader = new FileReader(getFile())) {
            Type listType = new TypeToken<List<Product>>(){}.getType();
            List<Product> lista = gson.fromJson(reader, listType);
            return lista != null ? lista : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Error leyendo products.json: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void saveAll(List<Product> productos) {
        try (Writer writer = new FileWriter(getFile())) {
            gson.toJson(productos, writer);
        } catch (IOException e) {
            System.err.println("Error guardando products.json: " + e.getMessage());
        }
    }

    public void save(Product product) {
        List<Product> lista = findAll();
        lista.removeIf(p -> p.getProductoId().equals(product.getProductoId()));
        lista.add(product);
        saveAll(lista);
    }

    public void deleteById(String productoId) {
        List<Product> lista = findAll();
        lista.removeIf(p -> p.getProductoId().equals(productoId));
        saveAll(lista);
    }
}