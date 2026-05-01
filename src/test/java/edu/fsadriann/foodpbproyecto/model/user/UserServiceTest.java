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

    // ── A-01 — Regresión: campos antes ignorados en constructor ──────────────
    // Estos tests HABRÍAN FALLADO antes del fix. Verifican que User almacena
    // correctamente TODOS los parámetros del constructor.

    @Test
    @DisplayName("A-01 | User guarda isPremium correctamente (true)")
    void user_isPremium_guardaValorCorrecto() {
        User premium = new User("id-p", "María", "García", Rol.OPERADOR,
                "2001", true, 311000001, "Calle 1", null);
        assertTrue(premium.isPremium(),
                "isPremium debe ser true, antes siempre era false (bug A-01)");
    }

    @Test
    @DisplayName("A-01 | User guarda isPremium false correctamente")
    void user_isNotPremium_guardaValorCorrecto() {
        User std = new User("id-s", "Carlos", "López", Rol.OPERADOR,
                "2002", false, 311000002, "Calle 2", null);
        assertFalse(std.isPremium());
    }

    @Test
    @DisplayName("A-01 | User guarda nombres correctamente")
    void user_nombres_guardaValorCorrecto() {
        User u = new User("id-1", "Ana", "Martínez", Rol.OPERADOR,
                "2003", false, 311000003, "Dir", null);
        assertEquals("Ana", u.getNombres());
    }

    @Test
    @DisplayName("A-01 | User guarda apellidos correctamente")
    void user_apellidos_guardaValorCorrecto() {
        User u = new User("id-1", "Ana", "Martínez", Rol.OPERADOR,
                "2003", false, 311000003, "Dir", null);
        assertEquals("Martínez", u.getApellidos());
    }

    @Test
    @DisplayName("A-01 | User.getNombreCompleto() retorna nombres y apellidos")
    void user_getNombreCompleto_retornaNombreCompleto() {
        User u = new User("id-1", "Ana", "Martínez", Rol.OPERADOR,
                "2003", false, 311000003, "Dir", null);
        assertEquals("Ana Martínez", u.getNombreCompleto().trim());
    }

    @Test
    @DisplayName("A-01 | User guarda id correctamente")
    void user_id_guardaValorCorrecto() {
        User u = new User("uuid-test-001", "Pedro", "Ruiz", Rol.OPERADOR,
                "2004", false, 311000004, "Dir", null);
        assertEquals("uuid-test-001", u.getId());
    }

    @Test
    @DisplayName("A-01 | User guarda rol correctamente")
    void user_rol_guardaValorCorrecto() {
        User u = new User("id-1", "Luisa", "Díaz", Rol.OPERADOR,
                "2005", false, 311000005, "Dir", null);
        assertEquals(Rol.OPERADOR, u.getRol());
    }

    @Test
    @DisplayName("A-01 | favProductos null → lista vacía (no NullPointerException)")
    void user_favProductosNulo_inicializaListaVacia() {
        User u = new User("id-1", "Test", "Test", Rol.OPERADOR,
                "2006", false, 311000006, "Dir", null);
        assertNotNull(u.getFavProductos(), "favProductos no debe ser null");
    }

    @Test
    @DisplayName("A-01 | isPremium cambia correctamente con setter")
    void user_setPremium_actualiza() {
        User u = new User("id-1", "Test", "Test", Rol.OPERADOR,
                "2007", false, 311000007, "Dir", null);
        assertFalse(u.isPremium());
        u.setPremium(true);
        assertTrue(u.isPremium());
    }
}
