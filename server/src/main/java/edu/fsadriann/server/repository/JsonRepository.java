package edu.fsadriann.server.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.model.iterator.Iterator;

import java.io.*;
import java.net.URL;

/**
 * Repositorio genérico que lee y escribe listas de objetos en archivos JSON.
 * Las subclases deben indicar el nombre del archivo y la clase del elemento.
 *
 * @param <T> tipo de entidad que gestiona el repositorio
 */
public abstract class JsonRepository<T> {

    private final String   fileName;
    private final Class<T> elementClass;

    /** Instancia de Gson con formato legible para los archivos JSON. */
    protected final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Inicializa el repositorio con el nombre del archivo y la clase del elemento.
     *
     * @param fileName     nombre del archivo JSON dentro de {@code resources/data/}
     * @param elementClass clase del objeto almacenado en la lista
     */
    protected JsonRepository(String fileName, Class<T> elementClass) {
        this.fileName     = fileName;
        this.elementClass = elementClass;
    }

    private File getFile() {
        URL url = getClass().getClassLoader().getResource("data/" + fileName);
        if (url != null) return new File(url.getFile());
        File file = new File("server/src/main/resources/data/" + fileName);
        file.getParentFile().mkdirs();
        return file;
    }

    /**
     * Lee y retorna todos los registros del archivo JSON.
     *
     * @return lista de entidades, o lista vacía si el archivo no existe
     */
    public LinkedList<T> findAll() {
        try (Reader reader = new FileReader(getFile())) {
            JsonArray array = gson.fromJson(reader, JsonArray.class);
            LinkedList<T> lista = new LinkedList<>();
            if (array != null) {
                for (int i = 0; i < array.size(); i++) {
                    lista.add(gson.fromJson(array.get(i), elementClass));
                }
            }
            return lista;
        } catch (IOException e) {
            return new LinkedList<>();
        }
    }

    /**
     * Guarda la lista completa de entidades en el archivo JSON, reemplazando el contenido anterior.
     *
     * @param items lista de entidades a guardar
     */
    public void saveAll(LinkedList<T> items) {
        JsonArray array = new JsonArray();
        Iterator<T> it = items.iterator();
        while (it.hasNext()) {
            T item = it.next();
            if (item != null) array.add(gson.toJsonTree(item));
        }
        try (Writer writer = new FileWriter(getFile())) {
            gson.toJson(array, writer);
        } catch (IOException e) {
            System.err.println("Error guardando " + fileName + ": " + e.getMessage());
        }
    }
}
