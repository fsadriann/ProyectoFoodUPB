package edu.fsadriann.server.model.user;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.model.iterator.Iterator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserRepositoryTest {

    @Test
    void debeCargarUsuariosDesdeJson() {
        UserRepository repo = new UserRepository();
        LinkedList<User> usuarios = repo.findAllUsers();

        assertFalse(usuarios.isEmpty(), "La lista no debe estar vacía");
        System.out.println("Usuarios cargados: " + usuarios.size());
        Iterator<User> it = usuarios.iterator();
        while (it.hasNext()) System.out.println(" - " + it.next());
    }

    @Test
    void debeCargarCredencialesDesdeJson() {
        UserRepository repo = new UserRepository();
        LinkedList<UserRepository.CredentialEntry> creds = repo.findAllCredentials();

        assertFalse(creds.isEmpty(), "Las credenciales no deben estar vacías");
        System.out.println("Credenciales cargadas: " + creds.size());
        Iterator<UserRepository.CredentialEntry> it = creds.iterator();
        while (it.hasNext()) {
            UserRepository.CredentialEntry c = it.next();
            if (c != null) System.out.println(" - cedula: " + c.cedula);
        }
    }
}
