package edu.fsadriann.server.model.ticket;

import java.io.Serializable;

/**
 * Representa un tiquete de atención generado para un cliente.
 * Contiene el número de turno y los datos del cliente asociado.
 */
public class Ticket implements Serializable {

    private static final long serialVersionUID = 1L;

    private String   id;
    private Customer customer;

    /**
     * Crea un tiquete con su número y el cliente al que corresponde.
     *
     * @param id       número de turno del tiquete
     * @param customer cliente asociado al tiquete
     */
    public Ticket(String id, Customer customer) {
        this.id       = id;
        this.customer = customer;
    }

    /** @return número de turno del tiquete */
    public String getId()           { return id; }

    /** @return nombre del cliente del tiquete */
    public String getCustomerName() { return customer.getNames(); }
}
