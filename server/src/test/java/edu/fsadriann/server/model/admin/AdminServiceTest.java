package edu.fsadriann.server.model.admin;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.model.iterator.Iterator;
import edu.fsadriann.server.model.order.Order;
import edu.fsadriann.server.model.user.Rol;
import edu.fsadriann.server.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.*;

class AdminServiceTest {

    private AdminService service;

    @BeforeEach
    void setUp() {
        service = new AdminService();
    }

    private User crearUsuario(String id, String cedula, String tel) {
        return new User(id, "Nombre", "Apellido", Rol.OPERADOR, cedula, false, tel, "Calle 1", null);
    }

    private int contar(LinkedList<String> lista) {
        int count = 0;
        Iterator<String> it = lista.iterator();
        while (it.hasNext()) {
            if (it.next() != null) count++;
        }
        return count;
    }

    @Test
    void crearOperador_exitoso() throws RemoteException {
        User op = crearUsuario("op1@test.com", "OP-001", "3001110001");
        assertTrue(service.crearOperador(op));
    }

    @Test
    void crearOperador_falla_duplicado() throws RemoteException {
        User op = crearUsuario("op2@test.com", "OP-002", "3001110002");
        service.crearOperador(op);
        assertFalse(service.crearOperador(op));
    }

    @Test
    void crearOperador_falla_null() throws RemoteException {
        assertFalse(service.crearOperador(null));
    }

    @Test
    void crearOperador_asignaRolOperador_siNoTieneRol() throws RemoteException {
        User sinRol = new User("op3@test.com", "Sin", "Rol", null, "OP-003", false, "3001110003", "Dir", null);
        assertTrue(service.crearOperador(sinRol));
    }

    @Test
    void editarOperador_exitoso() throws RemoteException {
        User op = crearUsuario("op4@test.com", "OP-004", "3001110004");
        service.crearOperador(op);
        User editado = new User("op4@test.com", "Nuevo", "Nombre", Rol.COCINA, "OP-004", false, "3001110004", "Dir2", null);
        assertTrue(service.editarOperador(editado));
    }

    @Test
    void editarOperador_falla_inexistente() throws RemoteException {
        User fantasma = crearUsuario("x@test.com", "NO-EXISTE", "3009990000");
        assertFalse(service.editarOperador(fantasma));
    }

    @Test
    void editarOperador_falla_null() throws RemoteException {
        assertFalse(service.editarOperador(null));
    }

    @Test
    void eliminarOperador_exitoso() throws RemoteException {
        User op = crearUsuario("op5@test.com", "OP-005", "3001110005");
        service.crearOperador(op);
        assertTrue(service.eliminarOperador("OP-005"));
    }

    @Test
    void eliminarOperador_falla_inexistente() throws RemoteException {
        assertFalse(service.eliminarOperador("NO-EXISTE"));
    }

    @Test
    void eliminarOperador_falla_null() throws RemoteException {
        assertFalse(service.eliminarOperador(null));
    }

    @Test
    void asignarRol_exitoso() throws RemoteException {
        User op = crearUsuario("op6@test.com", "OP-006", "3001110006");
        service.crearOperador(op);
        assertTrue(service.asignarRol("OP-006", Rol.COCINA));
    }

    @Test
    void asignarRol_falla_inexistente() throws RemoteException {
        assertFalse(service.asignarRol("NO-EXISTE", Rol.COCINA));
    }

    @Test
    void asignarRol_falla_cedulaNull() throws RemoteException {
        assertFalse(service.asignarRol(null, Rol.COCINA));
    }

    @Test
    void asignarRol_falla_rolNull() throws RemoteException {
        assertFalse(service.asignarRol("OP-006", null));
    }

    @Test
    void generarReporte_sinFiltros_noEsNull() throws RemoteException {
        LinkedList<Order> resultado = service.generarReporte(null, null, null);
        assertNotNull(resultado);
    }

    @Test
    void generarReporte_conFiltroEstado_noEsNull() throws RemoteException {
        LinkedList<Order> resultado = service.generarReporte(null, "PENDIENTE", null);
        assertNotNull(resultado);
    }

    @Test
    void verBitacoraAuditoria_registraEventosDespuesDeCrear() throws RemoteException {
        User op = crearUsuario("op7@test.com", "OP-007", "3001110007");
        service.crearOperador(op);
        LinkedList<String> bitacora = service.verBitacoraAuditoria();
        assertNotNull(bitacora);
        assertTrue(contar(bitacora) >= 1);
    }

    @Test
    void verBitacoraAuditoria_acumulaEventos() throws RemoteException {
        service.crearOperador(crearUsuario("op8@test.com", "OP-008", "3001110008"));
        service.crearOperador(crearUsuario("op9@test.com", "OP-009", "3001110009"));
        service.eliminarOperador("OP-008");
        LinkedList<String> bitacora = service.verBitacoraAuditoria();
        assertTrue(contar(bitacora) >= 3);
    }
}
