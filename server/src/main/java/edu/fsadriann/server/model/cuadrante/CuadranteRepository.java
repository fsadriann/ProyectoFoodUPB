package edu.fsadriann.server.model.cuadrante;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.model.iterator.Iterator;

import java.io.*;
import java.net.URISyntaxException;

/**
 * Repositorio para la persistencia de cuadrantes y conexiones del mapa en archivos JSON.
 */
public class CuadranteRepository {

    private static final String CUADS_FILE = "data/cuadrantes.json";
    private static final String CONNS_FILE = "data/conexiones.json";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Retorna todos los cuadrantes guardados en el archivo JSON.
     *
     * @return lista de cuadrantes
     */
    public LinkedList<Cuadrante> findAllCuadrantes() {
        return readList(CUADS_FILE, Cuadrante.class);
    }

    /**
     * Guarda la lista completa de cuadrantes en el archivo JSON.
     *
     * @param cuadrantes lista de cuadrantes a guardar
     */
    public void saveAllCuadrantes(LinkedList<Cuadrante> cuadrantes) {
        writeList(CUADS_FILE, cuadrantes);
    }

    /**
     * Guarda o actualiza un cuadrante en el archivo JSON.
     *
     * @param c cuadrante a guardar
     */
    public void saveCuadrante(Cuadrante c) {
        LinkedList<Cuadrante> lista = findAllCuadrantes();
        lista.remove(x -> x.getNombre().equals(c.getNombre()));
        lista.add(c);
        saveAllCuadrantes(lista);
    }

    /**
     * Elimina un cuadrante del archivo JSON por su nombre.
     *
     * @param nombre nombre del cuadrante a eliminar
     */
    public void deleteCuadrante(String nombre) {
        LinkedList<Cuadrante> lista = findAllCuadrantes();
        lista.remove(c -> c.getNombre().equals(nombre));
        saveAllCuadrantes(lista);
    }

    /**
     * Retorna todas las conexiones entre cuadrantes guardadas en el archivo JSON.
     *
     * @return lista de conexiones
     */
    public LinkedList<ConexionEntry> findAllConexiones() {
        return readList(CONNS_FILE, ConexionEntry.class);
    }

    /**
     * Guarda o actualiza una conexión entre dos cuadrantes.
     *
     * @param origen    nombre del cuadrante de origen
     * @param destino   nombre del cuadrante de destino
     * @param distancia distancia en km entre ambos cuadrantes
     */
    public void saveConexion(String origen, String destino, double distancia) {
        LinkedList<ConexionEntry> lista = findAllConexiones();
        lista.remove(c -> c.origen.equals(origen) && c.destino.equals(destino));
        lista.add(new ConexionEntry(origen, destino, distancia));
        writeList(CONNS_FILE, lista);
    }

    /**
     * Guarda la lista completa de conexiones en el archivo JSON, reemplazando el contenido anterior.
     *
     * @param conexiones lista de conexiones a guardar
     */
    public void saveAllConexiones(LinkedList<ConexionEntry> conexiones) {
        writeList(CONNS_FILE, conexiones);
    }

    /**
     * Elimina todas las conexiones que involucren al cuadrante indicado.
     *
     * @param nombre nombre del cuadrante cuyos enlaces se eliminarán
     */
    public void deleteConexionesDe(String nombre) {
        LinkedList<ConexionEntry> lista = findAllConexiones();
        lista.remove(c -> c.origen.equals(nombre) || c.destino.equals(nombre));
        writeList(CONNS_FILE, lista);
    }

    /**
     * Representa una conexión (arista) entre dos cuadrantes con su distancia.
     * Es serializable para poder ser transmitida por RMI.
     */
    public static class ConexionEntry implements java.io.Serializable {

        private static final long serialVersionUID = 1L;

        /** Nombre del cuadrante de origen. */
        public String origen;

        /** Nombre del cuadrante de destino. */
        public String destino;

        /** Distancia en km entre los dos cuadrantes. */
        public double distancia;

        /**
         * Crea una entrada de conexión entre dos cuadrantes.
         *
         * @param origen    nombre del cuadrante de origen
         * @param destino   nombre del cuadrante de destino
         * @param distancia distancia en km
         */
        public ConexionEntry(String origen, String destino, double distancia) {
            this.origen    = origen;
            this.destino   = destino;
            this.distancia = distancia;
        }
    }

    private <T> LinkedList<T> readList(String fileName, Class<T> elementClass) {
        try (Reader reader = new FileReader(getFile(fileName))) {
            JsonArray array = gson.fromJson(reader, JsonArray.class);
            LinkedList<T> lista = new LinkedList<>();
            if (array != null) {
                for (int i = 0; i < array.size(); i++) {
                    lista.add(gson.fromJson(array.get(i), elementClass));
                }
            }
            return lista;
        } catch (IOException e) {
            System.err.println("Error leyendo " + fileName + ": " + e.getMessage());
            return new LinkedList<>();
        }
    }

    private <T> void writeList(String fileName, LinkedList<T> items) {
        JsonArray array = new JsonArray();
        Iterator<T> it = items.iterator();
        while (it.hasNext()) {
            T item = it.next();
            if (item != null) array.add(gson.toJsonTree(item));
        }
        try (Writer writer = new FileWriter(getFile(fileName))) {
            gson.toJson(array, writer);
        } catch (IOException e) {
            System.err.println("Error guardando " + fileName + ": " + e.getMessage());
        }
    }

    private File getFile(String fileName) {
        java.net.URL url = getClass().getClassLoader().getResource(fileName);
        if (url != null) {
            try { return new File(url.toURI()); }
            catch (URISyntaxException e) { return new File(url.getFile()); }
        }
        File file = new File("server/src/main/resources/" + fileName);
        file.getParentFile().mkdirs();
        return file;
    }
}
