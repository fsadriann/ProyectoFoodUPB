package edu.fsadriann.server.model;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import edu.fsadriann.server.model.ticket.TicketInterface;
import edu.fsadriann.server.model.ticket.TicketService;

public class ServerModel {

    private String ip;
    private int port;
    private String serviceName;
    private String uri;
    private Registry registry;
    private boolean running = false;

    public ServerModel(String ip, int port, String serviceName) {
        this.ip = ip;
        this.port = port;
        this.serviceName = serviceName;
        this.uri = "//" + ip + ":" + port + "/" + serviceName;
    }

    public boolean deploy() {
        try {
            System.setProperty("java.rmi.server.hostname", ip);
            TicketInterface service = new TicketService();
            registry = LocateRegistry.createRegistry(port);
            Naming.rebind(uri, service);
            running = true;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean stop() {
        try {
            Naming.unbind(uri);
            UnicastRemoteObject.unexportObject(registry, true);
            running = false;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isRunning() {
        return running;
    }
}