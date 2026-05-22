package edu.fsadriann.server.model.ticket;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Servicio remoto para el registro y validación de tiquetes de atención.
 * Asigna turnos secuenciales a los clientes que solicitan atención.
 */
public class TicketService extends UnicastRemoteObject implements TicketInterface {

    private int      index   = 0;
    private Ticket[] tickets = new Ticket[100];

    /**
     * Crea el servicio de tiquetes y lo registra como objeto remoto RMI.
     */
    public TicketService() throws RemoteException {
        super();
    }

    /**
     * Registra un nuevo tiquete y asigna un número de turno correlativo.
     *
     * @param ticket tiquete con los datos del cliente
     * @return el tiquete registrado con su número de turno asignado
     */
    @Override
    public Ticket register(Ticket ticket) throws RemoteException {
        Ticket newTicket = new Ticket(
                String.valueOf(index + 1),
                new Customer("1", ticket.getCustomerName()));
        tickets[index] = newTicket;
        index++;
        return newTicket;
    }

    /**
     * Verifica si un tiquete fue registrado previamente en el sistema.
     *
     * @param ticket tiquete a validar
     * @return {@code true} si el tiquete existe en el registro
     */
    @Override
    public boolean validate(Ticket ticket) throws RemoteException {
        for (int i = 0; i < index; i++) {
            if (tickets[i].equals(ticket)) {
                return true;
            }
        }
        return false;
    }
}
