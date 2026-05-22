package edu.fsadriann.server.model.ticket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.*;

class TicketServiceTest {

    private TicketService service;

    @BeforeEach
    void setUp() throws RemoteException {
        service = new TicketService();
    }

    private Ticket ticketEntrada(String nombre) {
        return new Ticket("", new Customer("1", nombre));
    }

    // ── register ──────────────────────────────────────────────────────────────

    @Test
    void register_devuelveTicketNoNulo() throws RemoteException {
        Ticket resultado = service.register(ticketEntrada("Juan"));
        assertNotNull(resultado);
    }

    @Test
    void register_primerTicket_tieneIdUno() throws RemoteException {
        Ticket resultado = service.register(ticketEntrada("Ana"));
        assertEquals("1", resultado.getId());
    }

    @Test
    void register_preservaNombreCliente() throws RemoteException {
        Ticket resultado = service.register(ticketEntrada("Pedro"));
        assertEquals("Pedro", resultado.getCustomerName());
    }

    @Test
    void register_idsSecuenciales() throws RemoteException {
        Ticket t1 = service.register(ticketEntrada("Cliente1"));
        Ticket t2 = service.register(ticketEntrada("Cliente2"));
        assertEquals("1", t1.getId());
        assertEquals("2", t2.getId());
    }

    // ── validate ──────────────────────────────────────────────────────────────

    @Test
    void validate_ticketRegistrado_devuelveTrue() throws RemoteException {
        Ticket registrado = service.register(ticketEntrada("Maria"));
        assertTrue(service.validate(registrado));
    }

    @Test
    void validate_ticketNoRegistrado_devuelveFalse() throws RemoteException {
        Ticket noRegistrado = new Ticket("99", new Customer("1", "Fantasma"));
        assertFalse(service.validate(noRegistrado));
    }

    @Test
    void validate_multiplesRegistros_validaCorrectamente() throws RemoteException {
        Ticket t1 = service.register(ticketEntrada("A"));
        Ticket t2 = service.register(ticketEntrada("B"));
        assertTrue(service.validate(t1));
        assertTrue(service.validate(t2));
    }
}
