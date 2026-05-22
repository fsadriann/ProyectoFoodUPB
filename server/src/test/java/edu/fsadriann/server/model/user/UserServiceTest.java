package edu.fsadriann.server.model.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(null);
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Test
    void login_exitoso_devuelveRolCorrecto() throws RemoteException {
        Rol rol = service.login("operator@test.com", "1234");
        assertNotNull(rol);
        assertEquals(Rol.OPERADOR, rol);
    }

    @Test
    void login_contrasenaIncorrecta_devuelveNull() throws RemoteException {
        assertNull(service.login("operator@test.com", "incorrecta"));
    }

    @Test
    void login_usuarioInexistente_devuelveNull() throws RemoteException {
        assertNull(service.login("noexiste@test.com", "1001"));
    }

    @Test
    void login_correoNull_devuelveNull() throws RemoteException {
        assertNull(service.login(null, "1001"));
    }

    @Test
    void login_contrasenaNull_devuelveNull() throws RemoteException {
        assertNull(service.login("operator@test.com", null));
    }

    @Test
    void login_clientePremium_devuelveRolCliente() throws RemoteException {
        Rol rol = service.login("cliente@upb.com", "1234");
        assertNotNull(rol);
        assertEquals(Rol.CLIENTE, rol);
    }

    // ── Sesión ────────────────────────────────────────────────────────────────

    @Test
    void validarSesion_sesionInexistente_devuelveFalse() throws RemoteException {
        assertFalse(service.validarSesion("sesion-que-no-existe"));
    }

    @Test
    void validarSesion_null_devuelveFalse() throws RemoteException {
        assertFalse(service.validarSesion(null));
    }

    @Test
    void logout_idNull_noLanzaExcepcion() {
        assertDoesNotThrow(() -> service.logout(null));
    }

    // ── Búsqueda ─────────────────────────────────────────────────────────────

    @Test
    void buscarClientePorTelefono_existente() throws RemoteException {
        User u = service.buscarClientePorTelefono("3001234560");
        assertNotNull(u);
        assertEquals("operator@test.com", u.getId());
    }

    @Test
    void buscarClientePorTelefono_inexistente_devuelveNull() throws RemoteException {
        assertNull(service.buscarClientePorTelefono("0000000000"));
    }

    @Test
    void buscarClientePorTelefono_null_devuelveNull() throws RemoteException {
        assertNull(service.buscarClientePorTelefono(null));
    }

    // ── Usuarios ──────────────────────────────────────────────────────────────

    @Test
    void getTotalUsuarios_cargaDesdeJson() throws RemoteException {
        assertTrue(service.getTotalUsuarios() > 0);
    }

    @Test
    void listarUsuarios_noEsNull() throws RemoteException {
        assertNotNull(service.listarUsuarios());
    }

    // ── Cambio de contraseña ──────────────────────────────────────────────────

    @Test
    void cambiarContrasena_falla_usuarioInexistente() throws RemoteException {
        assertFalse(service.cambiarContrasena("NO-EXISTE", "pass", "nueva"));
    }

    @Test
    void cambiarContrasena_falla_contrasenaActualIncorrecta() throws RemoteException {
        assertFalse(service.cambiarContrasena("1001", "incorrecta", "nueva"));
    }

    @Test
    void cambiarContrasena_falla_nuevaVacia() throws RemoteException {
        assertFalse(service.cambiarContrasena("1001", "1001", ""));
    }

    @Test
    void cambiarContrasena_falla_cedulaNull() throws RemoteException {
        assertFalse(service.cambiarContrasena(null, "1001", "nueva"));
    }

    // ── Bitácora ──────────────────────────────────────────────────────────────

    @Test
    void getBitacora_noEsNull() {
        assertNotNull(service.getBitacora());
    }
}
