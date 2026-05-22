package edu.fsadriann.server.model.user;

import com.google.gson.*;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.model.iterator.Iterator;

import java.io.*;
import java.net.URISyntaxException;

/**
 * Repositorio para la persistencia de usuarios y credenciales en archivos JSON.
 * Los usuarios se guardan sin la lista de favoritos para evitar conflictos con la estructura de datos del JAR.
 */
public class UserRepository {

    private static final String USERS_FILE = "data/users.json";
    private static final String CREDS_FILE = "data/credentials.json";

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    private final Gson gsonSimple = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Retorna todos los usuarios del archivo JSON.
     *
     * @return lista de usuarios
     */
    public LinkedList<User> findAllUsers() {
        return readList(USERS_FILE, User.class, gson);
    }

    /**
     * Guarda la lista completa de usuarios en el archivo JSON.
     *
     * @param users lista de usuarios a guardar
     */
    public void saveAllUsers(LinkedList<User> users) {
        writeList(USERS_FILE, users, gson);
    }

    /**
     * Guarda o actualiza un usuario en el archivo JSON.
     *
     * @param user usuario a guardar
     */
    public void saveUser(User user) {
        LinkedList<User> lista = findAllUsers();
        lista.remove(u -> u.getCedula().equals(user.getCedula()));
        lista.add(user);
        saveAllUsers(lista);
    }

    /**
     * Elimina un usuario del archivo JSON por su cédula.
     *
     * @param cedula cédula del usuario a eliminar
     */
    public void deleteUser(String cedula) {
        LinkedList<User> lista = findAllUsers();
        lista.remove(u -> u.getCedula().equals(cedula));
        saveAllUsers(lista);
    }

    /**
     * Retorna todas las credenciales almacenadas.
     *
     * @return lista de entradas de credenciales
     */
    public LinkedList<CredentialEntry> findAllCredentials() {
        return readList(CREDS_FILE, CredentialEntry.class, gsonSimple);
    }

    /**
     * Guarda o actualiza la contraseña de un usuario.
     *
     * @param cedula     cédula del usuario
     * @param contrasena contraseña a guardar
     */
    public void saveCredential(String cedula, String contrasena) {
        LinkedList<CredentialEntry> lista = findAllCredentials();
        lista.remove(c -> c.cedula.equals(cedula));
        lista.add(new CredentialEntry(cedula, contrasena));
        writeList(CREDS_FILE, lista, gsonSimple);
    }

    /**
     * Obtiene la contraseña almacenada de un usuario.
     *
     * @param cedula cédula del usuario
     * @return contraseña almacenada, o {@code null} si no existe
     */
    public String getContrasena(String cedula) {
        Iterator<CredentialEntry> it = findAllCredentials().iterator();
        while (it.hasNext()) {
            CredentialEntry c = it.next();
            if (c != null && c.cedula.equals(cedula)) return c.contrasena;
        }
        return null;
    }

    /**
     * Elimina las credenciales de un usuario.
     *
     * @param cedula cédula del usuario
     */
    public void deleteCredential(String cedula) {
        LinkedList<CredentialEntry> lista = findAllCredentials();
        lista.remove(c -> c.cedula.equals(cedula));
        writeList(CREDS_FILE, lista, gsonSimple);
    }

    /**
     * Representa las credenciales de acceso de un usuario (cédula + contraseña).
     */
    public static class CredentialEntry {

        /** Cédula del usuario. */
        public String cedula;

        /** Contraseña de acceso. */
        public String contrasena;

        /**
         * Crea una entrada de credenciales.
         *
         * @param cedula     cédula del usuario
         * @param contrasena contraseña de acceso
         */
        public CredentialEntry(String cedula, String contrasena) {
            this.cedula     = cedula;
            this.contrasena = contrasena;
        }
    }

    private <T> LinkedList<T> readList(String fileName, Class<T> elementClass, Gson g) {
        try (Reader reader = new FileReader(getFile(fileName))) {
            JsonArray array = g.fromJson(reader, JsonArray.class);
            LinkedList<T> lista = new LinkedList<>();
            if (array != null) {
                for (int i = 0; i < array.size(); i++) {
                    lista.add(g.fromJson(array.get(i), elementClass));
                }
            }
            return lista;
        } catch (IOException e) {
            System.err.println("Error leyendo " + fileName + ": " + e.getMessage());
            return new LinkedList<>();
        }
    }

    private <T> void writeList(String fileName, LinkedList<T> items, Gson g) {
        JsonArray array = new JsonArray();
        Iterator<T> it = items.iterator();
        while (it.hasNext()) {
            T item = it.next();
            if (item != null) array.add(g.toJsonTree(item));
        }
        try (Writer writer = new FileWriter(getFile(fileName))) {
            g.toJson(array, writer);
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
