package edu.fsadriann.server.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public abstract class JsonRepository<T> {

    private final String fileName;
    private final Type listType;
    protected final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    protected JsonRepository(String fileName, Type listType) {
        this.fileName = fileName;
        this.listType = listType;
    }

    // Busca el archivo en resources/data/
    private File getFile() {
        URL url = getClass().getClassLoader().getResource("data/" + fileName);
        if (url != null) return new File(url.getFile());
        // Si no existe aún, lo crea en target/classes/data/
        File file = new File("server/src/main/resources/data/" + fileName);
        file.getParentFile().mkdirs();
        return file;
    }

    public List<T> findAll() {
        try (Reader reader = new FileReader(getFile())) {
            List<T> lista = gson.fromJson(reader, listType);
            return lista != null ? lista : new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public void saveAll(List<T> items) {
        try (Writer writer = new FileWriter(getFile())) {
            gson.toJson(items, writer);
        } catch (IOException e) {
            System.err.println("Error guardando " + fileName + ": " + e.getMessage());
        }
    }
}