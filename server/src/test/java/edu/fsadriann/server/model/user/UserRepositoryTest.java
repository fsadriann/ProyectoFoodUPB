package edu.fsadriann.server.model.user;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class UserRepositoryTest {

    @Test
    void debeCargarUsuariosDesdeJson() {
        UserRepository repo = new UserRepository();
        List<User> usuarios = repo.findAllUsers();

        assertFalse(usuarios.isEmpty(), "La lista no debe estar vacía");
        System.out.println("Usuarios cargados: " + usuarios.size());
        usuarios.forEach(u -> System.out.println(" - " + u));
    }

    @Test
    void debeCargarCredencialesDesdeJson() {
        UserRepository repo = new UserRepository();
        List<UserRepository.CredentialEntry> creds = repo.findAllCredentials();

        assertFalse(creds.isEmpty(), "Las credenciales no deben estar vacías");
        System.out.println("Credenciales cargadas: " + creds.size());
        creds.forEach(c -> System.out.println(" - cedula: " + c.cedula));
    }
}