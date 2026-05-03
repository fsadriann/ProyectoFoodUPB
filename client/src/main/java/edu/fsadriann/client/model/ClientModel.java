package edu.fsadriann.client.model;

import java.rmi.Naming;

import edu.fsadriann.server.model.observer.Subject;
import edu.fsadriann.server.model.ticket.Customer;
import edu.fsadriann.server.model.ticket.Ticket;
import edu.fsadriann.server.model.ticket.TicketInterface;

public class ClientModel extends Subject {
    private String logger;
    private String uri;
    private TicketInterface ticketService;
    private boolean connected;

    public ClientModel(String ip, int port, String serviceName) {
        this.uri = "rmi://" + ip + ":" + port + "/" + serviceName;
        this.ticketService = null;
        this.connected = false;
    }

    public boolean connect() {
        try {
            this.ticketService = (TicketInterface) Naming.lookup(uri);
            this.connected = true;
            this.logger = ("Connecting to server at: " + uri);
            this.notifyObservers();
            return true;
        } catch (Exception e) {
            this.connected = false;
            this.logger = ("Failed to connect to server at: " + uri);
            this.notifyObservers();
            System.err.println(this.logger);
            return false;
        }
    }

    public TicketInterface getTicketService() {
        return ticketService;
    }

    public void register(String names) {
        if (!this.connected || this.ticketService == null) {
            this.logger = "Registration failed: server is not connected.";
            this.notifyObservers();
            return;
        }
        try {
            Ticket ticket = new Ticket("", new Customer("1", names));
            Ticket ticketRegistered = this.getTicketService().register(ticket);
            this.logger = ("Registered with ticket: " + ticketRegistered.getId() + " for customer: "
                    + ticketRegistered.getCustomerName());
            this.notifyObservers();
        } catch (Exception e) {
            this.logger = "Registration failed: " + e.getMessage();
            this.notifyObservers();
            System.err.println(this.logger);
        }
    }

    public String getLogger() {
        return logger;
    }

    public boolean isConnected() {
        return connected;
    }
}
