package edu.fsadriann.server.model.ticket;

import java.io.Serializable;

/**
 * Representa al cliente asociado a un tiquete de atención.
 */
public class Customer implements Serializable {

    private static final long serialVersionUID = 2L;

    private String id;
    private String names;

    /**
     * Crea un cliente con su identificador y nombre.
     *
     * @param id    identificador único del cliente
     * @param names nombre del cliente
     */
    public Customer(String id, String names) {
        this.id    = id;
        this.names = names;
    }

    /** @return identificador del cliente */
    public String getId()    { return id; }

    /** @return nombre del cliente */
    public String getNames() { return names; }

    /** @param names nuevo nombre del cliente */
    public void setNames(String names) { this.names = names; }
}
