package edu.fsadriann.server.model.cuadrante;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class CuadranteRepository {

    private static final String CUADS_FILE = "data/cuadrantes.json";
    private static final String CONNS_FILE = "data/conexiones.json";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // ── Cuadrantes ────────────────────────────────────────────────────────────

    public List<Cuadrante> findAllCuadrantes() {
        return readList(CUADS_FILE, new TypeToken<List<Cuadrante>>(){}.getType());
    }

    public void saveAllCuadrantes(List<Cuadrante> cuadrantes) {
        writeList(CUADS_FILE, cuadrantes);
    }

    public void saveCuadrante(Cuadrante c) {
        List<Cuadrante> lista = findAllCuadrantes();
        lista.removeIf(x -> x.getNombre().equals(c.getNombre()));
        lista.add(c);
        saveAllCuadrantes(lista);
    }

    // ── Conexiones ────────────────────────────────────────────────────────────

    public List<ConexionEntry> findAllConexiones() {
        return readList(CONNS_FILE, new TypeToken<List<ConexionEntry>>(){}.getType());
    }

    public void saveConexion(String origen, String destino, double distancia) {
        List<ConexionEntry> lista = findAllConexiones();
        lista.removeIf(c -> c.origen.equals(origen) && c.destino.equals(destino));
        lista.add(new ConexionEntry(origen, destino, distancia));
        writeList(CONNS_FILE, lista);
    }

    // ── ConexionEntry — Serializable para transmisión por RMI ─────────────────

    public static class ConexionEntry implements java.io.Serializable {

        private static final long serialVersionUID = 1L;

        public String origen;
        public String destino;
        public double distancia;

        public ConexionEntry(String origen, String destino, double distancia) {
            this.origen    = origen;
            this.destino   = destino;
            this.distancia = distancia;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private <T> List<T> readList(String fileName, Type type) {
        try (Reader reader = new FileReader(getFile(fileName))) {
            List<T> lista = gson.fromJson(reader, type);
            return lista != null ? lista : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Error leyendo " + fileName + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private <T> void writeList(String fileName, List<T> items) {
        try (Writer writer = new FileWriter(getFile(fileName))) {
            gson.toJson(items, writer);
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

    public void deleteCuadrante(String nombre) {
        List<Cuadrante> lista = findAllCuadrantes();
        lista.removeIf(c -> c.getNombre().equals(nombre));
        saveAllCuadrantes(lista);
    }

    public void deleteConexionesDe(String nombre) {
        List<ConexionEntry> lista = findAllConexiones();
        lista.removeIf(c -> c.origen.equals(nombre) || c.destino.equals(nombre));
        writeList(CONNS_FILE, lista);
    }
}