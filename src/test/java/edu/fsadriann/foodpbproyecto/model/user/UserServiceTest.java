package edu.fsadriann.foodpbproyecto.model.user;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService service;

    private static final String CORREO = "juan@test.com";
    private static final String CEDULA = "123456789";
    private static final String CONTRASENA = "pass1234";

    // ── Setup ────────────────────────────────────────────────────────────────

    @BeforeEach
    void setUp() throws RemoteException {
        service = new UserService();
        service.registrarCliente(crearUsuarioTest(CORREO, CEDULA));
        service.registrarCredencial(CEDULA, CONTRASENA);
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private User crearUsuarioTest(String id, String cedula) {
        return new User(id, "Juan", "Pérez", Rol.OPERADOR,
                cedula, false, 300123456, "Calle 1 #2-3", new LinkedList<>());
    }

    // ── RF-09: login ─────────────────────────────────────────────────────────

    @Test
    void login_exitoso_retornaSesionId() throws RemoteException {
        String sesionId = service.login(CORREO, CONTRASENA);
        assertNotNull(sesionId);
    }

    @Test
    void login_fallido_contrasenaIncorrecta() throws RemoteException {
        String sesionId = service.login(CORREO, "wrongpass");
        assertNull(sesionId);
    }

    @Test
    void login_fallido_correoInexistente() throws RemoteException {
        String sesionId = service.login("noexiste@test.com", CONTRASENA);
        assertNull(sesionId);
    }

    // ── RF-09: validarSesion ─────────────────────────────────────────────────

    @Test
    void validarSesion_true_despuesDeLogin() throws RemoteException {
        String sesionId = service.login(CORREO, CONTRASENA);
        assertTrue(service.validarSesion(sesionId));
    }

    @Test
    void validarSesion_false_sesionInexistente() throws RemoteException {
        assertFalse(service.validarSesion("sesion-que-no-existe"));
    }

    // ── RF-09: logout ────────────────────────────────────────────────────────

    @Test
    void logout_invalidaSesion() throws RemoteException {
        String sesionId = service.login(CORREO, CONTRASENA);
        service.logout(sesionId);
        assertFalse(service.validarSesion(sesionId));
    }

    // ── RF-09: cambiarContrasena ─────────────────────────────────────────────

    @Test
    void cambiarContrasena_exitoso() throws RemoteException {
        boolean result = service.cambiarContrasena(CEDULA, CONTRASENA, "nueva1234");
        assertTrue(result);
        assertNotNull(service.login(CORREO, "nueva1234"));
    }

    @Test
    void cambiarContrasena_falla_actualIncorrecta() throws RemoteException {
        assertFalse(service.cambiarContrasena(CEDULA, "incorrecta", "nueva1234"));
    }

    @Test
    void cambiarContrasena_falla_usuarioInexistente() throws RemoteException {
        assertFalse(service.cambiarContrasena("cedula-falsa", CONTRASENA, "nueva1234"));
    }

    @Test
    void login_contrasenaVieja_falla_trasCambio() throws RemoteException {
        service.cambiarContrasena(CEDULA, CONTRASENA, "nueva1234");
        assertNull(service.login(CORREO, CONTRASENA));
    }
}