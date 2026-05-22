package edu.fsadriann.server.model.cuadrante;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CuadranteServiceTest {

    private CuadranteService service;

    @BeforeEach
    void setUp() {
        service = new CuadranteService();
    }

    private String nombre() {
        return "T-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private Cuadrante cuadrante(String nombre) {
        return new Cuadrante(nombre, "Descripción test", 2.0);
    }

    // ── agregarCuadrante ──────────────────────────────────────────────────────

    @Test
    void agregarCuadrante_exitoso() throws RemoteException {
        assertTrue(service.agregarCuadrante(cuadrante(nombre())));
    }

    @Test
    void agregarCuadrante_falla_null() {
        assertThrows(IllegalArgumentException.class,
                () -> service.agregarCuadrante(null));
    }

    @Test
    void agregarCuadrante_falla_duplicado() throws RemoteException {
        String n = nombre();
        service.agregarCuadrante(cuadrante(n));
        assertFalse(service.agregarCuadrante(cuadrante(n)));
    }

    @Test
    void agregarCuadrante_incrementaNumeroCuadrantes() throws RemoteException {
        int antes = service.numeroCuadrantes();
        service.agregarCuadrante(cuadrante(nombre()));
        assertEquals(antes + 1, service.numeroCuadrantes());
    }

    // ── eliminarCuadrante ─────────────────────────────────────────────────────

    @Test
    void eliminarCuadrante_falla_UPB() throws RemoteException {
        assertFalse(service.eliminarCuadrante("UPB"));
    }

    @Test
    void eliminarCuadrante_falla_null() throws RemoteException {
        assertFalse(service.eliminarCuadrante(null));
    }

    @Test
    void eliminarCuadrante_falla_inexistente() throws RemoteException {
        assertFalse(service.eliminarCuadrante("NoExisteCuadrante999"));
    }

    @Test
    void eliminarCuadrante_exitoso() throws RemoteException {
        String n = nombre();
        service.agregarCuadrante(cuadrante(n));
        assertTrue(service.eliminarCuadrante(n));
    }

    // ── buscarCuadrante ───────────────────────────────────────────────────────

    @Test
    void buscarCuadrante_inexistente_devuelveNull() throws RemoteException {
        assertNull(service.buscarCuadrante("CuadranteNoExiste999"));
    }

    @Test
    void buscarCuadrante_nulo_devuelveNull() throws RemoteException {
        assertNull(service.buscarCuadrante(null));
    }

    // ── conectarCuadrantes ────────────────────────────────────────────────────

    @Test
    void conectarCuadrantes_falla_distanciaZero() throws RemoteException {
        String a = nombre();
        String b = nombre();
        service.agregarCuadrante(cuadrante(a));
        service.agregarCuadrante(cuadrante(b));
        assertThrows(IllegalArgumentException.class,
                () -> service.conectarCuadrantes(a, b, 0));
    }

    @Test
    void conectarCuadrantes_exitoso_incrementaConexiones() throws RemoteException {
        String a = nombre();
        String b = nombre();
        service.agregarCuadrante(cuadrante(a));
        service.agregarCuadrante(cuadrante(b));
        int antes = service.numeroConexiones();
        assertTrue(service.conectarCuadrantes(a, b, 1.5));
        assertEquals(antes + 1, service.numeroConexiones());
    }

    // ── existeRuta ────────────────────────────────────────────────────────────

    @Test
    void existeRuta_nodosInexistentes_devuelveFalse() throws RemoteException {
        assertFalse(service.existeRuta("NoExiste1", "NoExiste2"));
    }

    @Test
    void existeRuta_mismoNodo_devuelveTrue() throws RemoteException {
        String n = nombre();
        service.agregarCuadrante(cuadrante(n));
        assertTrue(service.existeRuta(n, n));
    }

    // ── vecinosDirectos ───────────────────────────────────────────────────────

    @Test
    void vecinosDirectos_nodoInexistente_devuelveNull() throws RemoteException {
        assertNull(service.vecinosDirectos("NodoNoExiste"));
    }

    @Test
    void vecinosDirectos_nodoConectado_devuelveVecinos() throws RemoteException {
        String a = nombre();
        String b = nombre();
        service.agregarCuadrante(cuadrante(a));
        service.agregarCuadrante(cuadrante(b));
        service.conectarCuadrantes(a, b, 1.0);
        LinkedList<String> vecinos = service.vecinosDirectos(a);
        assertNotNull(vecinos);
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    @Test
    void numeroCuadrantes_esNonNegativo() throws RemoteException {
        assertTrue(service.numeroCuadrantes() >= 0);
    }

    @Test
    void listarCuadrantes_noEsNull() throws RemoteException {
        assertNotNull(service.listarCuadrantes());
    }

    @Test
    void verMatrizAdyacencia_noEsNull() throws RemoteException {
        assertNotNull(service.verMatrizAdyacencia());
    }
}
