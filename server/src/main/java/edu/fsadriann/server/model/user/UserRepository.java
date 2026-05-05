package edu.fsadriann.server.model.user;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    private static final String USERS_FILE = "data/users.json";
    private static final String CREDS_FILE = "data/credentials.json";

    // Excluye favProductos para evitar problemas con la LinkedList del JAR
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    // Gson sin restricciones para credenciales (son POJOs simples)
    private final Gson gsonSimple = new GsonBuilder().setPrettyPrinting().create();

    // ── Usuarios ─────────────────────────────────────────────────────────────

    public List<User> findAllUsers() {
        return readList(USERS_FILE, new TypeToken<List<User>>(){}.getType(), gson);
    }

    public void saveAllUsers(List<User> users) {
        writeList(USERS_FILE, users, gson);
    }

    public void saveUser(User user) {
        List<User> lista = findAllUsers();
        lista.removeIf(u -> u.getCedula().equals(user.getCedula()));
        lista.add(user);
        saveAllUsers(lista);
    }

    public void deleteUser(String cedula) {
        List<User> lista = findAllUsers();
        lista.removeIf(u -> u.getCedula().equals(cedula));
        saveAllUsers(lista);
    }

    // ── Credenciales ─────────────────────────────────────────────────────────

    public List<CredentialEntry> findAllCredentials() {
        return readList(CREDS_FILE, new TypeToken<List<CredentialEntry>>(){}.getType(), gsonSimple);
    }

    public void saveCredential(String cedula, String contrasena) {
        List<CredentialEntry> lista = findAllCredentials();
        lista.removeIf(c -> c.cedula.equals(cedula));
        lista.add(new CredentialEntry(cedula, contrasena));
        writeList(CREDS_FILE, lista, gsonSimple);
    }

    public String getContrasena(String cedula) {
        return findAllCredentials().stream()
                .filter(c -> c.cedula.equals(cedula))
                .map(c -> c.contrasena)
                .findFirst()
                .orElse(null);
    }

    public void deleteCredential(String cedula) {
        List<CredentialEntry> lista = findAllCredentials();
        lista.removeIf(c -> c.cedula.equals(cedula));
        writeList(CREDS_FILE, lista, gsonSimple);
    }

    // ── CredentialEntry (clase interna pública para Gson) ─────────────────────

    public static class CredentialEntry {
        public String cedula;
        public String contrasena;

        public CredentialEntry(String cedula, String contrasena) {
            this.cedula = cedula;
            this.contrasena = contrasena;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private <T> List<T> readList(String fileName, Type type, Gson g) {
        try (Reader reader = new FileReader(getFile(fileName))) {
            List<T> lista = g.fromJson(reader, type);
            return lista != null ? lista : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Error leyendo " + fileName + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private <T> void writeList(String fileName, List<T> items, Gson g) {
        try (Writer writer = new FileWriter(getFile(fileName))) {
            g.toJson(items, writer);
        } catch (IOException e) {
            System.err.println("Error guardando " + fileName + ": " + e.getMessage());
        }
    }

    private File getFile(String fileName) {
        java.net.URL url = getClass().getClassLoader().getResource(fileName);
        if (url != null) {
            try {
                return new File(url.toURI());
            } catch (URISyntaxException e) {
                return new File(url.getFile());
            }
        }
        File file = new File("server/src/main/resources/" + fileName);
        file.getParentFile().mkdirs();
        return file;
    }
}