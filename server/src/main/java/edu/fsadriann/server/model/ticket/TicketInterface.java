package edu.fsadriann.server.model.ticket;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Contrato remoto para el registro y validación de tiquetes de atención.
 */
public interface TicketInterface extends Remote {

    /**
     * Registra un nuevo tiquete y le asigna un número de turno.
     *
     * @param ticket tiquete con los datos del cliente
     * @return el tiquete registrado con su número asignado
     */
    Ticket register(Ticket ticket) throws RemoteException;

    /**
     * Verifica si un tiquete fue registrado en el sistema.
     *
     * @param ticket tiquete a validar
     * @return {@code true} si el tiquete existe
     */
    boolean validate(Ticket ticket) throws RemoteException;
}
