package edu.fsadriann.foodpbproyecto.model.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de UserService para RF-01 (buscar por teléfono) y RF-11 (registrar cliente).
 *
 * <p>NOTA: User.telefono es de tipo {@code int} (máx 2.147.483.647).
 * Se usan números de 9 dígitos. Deuda técnica: migrar a {@code String}.
 *
 * @author fsadriann
 */
@DisplayName("UserService – RF-01 y RF-11")
class UserServiceTest {

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService();
    }

    /** Crea un User de prueba. Teléfono máx 9 dígitos por límite de int. */
    private User usuario(String cedula, int telefono) {
        return new User("id-test", "Juan", "Pérez", Rol.OPERADOR,
                cedula, false, telefono, "Calle 10 #5-20", null);
    }

    // ── RF-11: registrarCliente ───────────────────────────────────────────────

    @Test
    @DisplayName("RF-11 | Registrar cliente nuevo → éxito")
    void registrar_nuevoCliente_retornaUsuario() throws RemoteException {
        User registrado = service.registrarCliente(usuario("1001", 312345678));
        assertNotNull(registrado);
        assertEquals("1001", registrado.getCedula());
    }

    @Test
    @DisplayName("RF-11 | Teléfono duplicado → IllegalArgumentException")
    void registrar_telefonoDuplicado_lanzaExcepcion() throws RemoteException {
        service.registrarCliente(usuario("1001", 312345678));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.registrarCliente(usuario("1002", 312345678)));
        assertTrue(ex.getMessage().contains("Teléfono ya registrado"));
    }

    @Test
    @DisplayName("RF-11 | Cédula duplicada → IllegalArgumentException")
    void registrar_cedulaDuplicada_lanzaExcepcion() throws RemoteException {
        service.registrarCliente(usuario("1001", 312345678));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.registrarCliente(usuario("1001", 312999999)));
        assertTrue(ex.getMessage().contains("Cédula ya registrada"));
    }

    @Test
    @DisplayName("RF-11 | Usuario nulo → IllegalArgumentException")
    void registrar_usuarioNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> service.registrarCliente(null));
    }

    @Test
    @DisplayName("RF-11 | Múltiples clientes → todos persisten")
    void registrar_multiplesClientes_todosGuardados() throws RemoteException {
        service.registrarCliente(usuario("1001", 311111111));
        service.registrarCliente(usuario("1002", 322222222));
        service.registrarCliente(usuario("1003", 333333333));
        assertNotNull(service.buscarClientePorTelefono("311111111"));
        assertNotNull(service.buscarClientePorTelefono("322222222"));
        assertNotNull(service.buscarClientePorTelefono("333333333"));
    }

    // ── RF-01: buscarClientePorTelefono ───────────────────────────────────────

    @Test
    @DisplayName("RF-01 | Teléfono existente → retorna cliente correcto")
    void buscar_telefonoExistente_retornaCliente() throws RemoteException {
        service.registrarCliente(usuario("1001", 312345678));
        User encontrado = service.buscarClientePorTelefono("312345678");
        assertNotNull(encontrado);
        assertEquals("1001", encontrado.getCedula());
    }

    @Test
    @DisplayName("RF-01 | Teléfono inexistente → null")
    void buscar_telefonoInexistente_retornaNull() throws RemoteException {
        assertNull(service.buscarClientePorTelefono("999999999"));
    }

    @Test
    @DisplayName("RF-01 | Teléfono nulo → null sin excepción")
    void buscar_telefonoNulo_retornaNull() throws RemoteException {
        assertNull(service.buscarClientePorTelefono(null));
    }

    @Test
    @DisplayName("RF-01 | Lista vacía → null")
    void buscar_listaVacia_retornaNull() throws RemoteException {
        assertNull(service.buscarClientePorTelefono("312345678"));
    }

    // ── Actualizar / Eliminar (RF-10 / Admin) ────────────────────────────────

    @Test
    @DisplayName("RF-10 | Actualizar cliente existente → éxito")
    void actualizar_clienteExistente_retornaTrue() throws RemoteException {
        service.registrarCliente(usuario("1001", 312345678));
        assertTrue(service.actualizarCliente(usuario("1001", 312999999)));
        assertEquals(312999999,
                service.buscarClientePorTelefono("312999999").getTelefono());
    }

    @Test
    @DisplayName("RF-10 | Actualizar cliente inexistente → false")
    void actualizar_clienteInexistente_retornaFalse() throws RemoteException {
        assertFalse(service.actualizarCliente(usuario("9999", 312999999)));
    }

    @Test
    @DisplayName("Admin | Eliminar cliente existente → ya no se encuentra")
    void eliminar_clienteExistente_retornaTrue() throws RemoteException {
        service.registrarCliente(usuario("1001", 312345678));
        assertTrue(service.eliminarCliente("1001"));
        assertNull(service.buscarClientePorTelefono("312345678"));
    }

    @Test
    @DisplayName("Admin | Eliminar cliente inexistente → false")
    void eliminar_clienteInexistente_retornaFalse() throws RemoteException {
        assertFalse(service.eliminarCliente("9999"));
    }
}
