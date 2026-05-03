package edu.fsadriann.client.controller;

import edu.fsadriann.client.model.ClientModel;
import edu.fsadriann.view.ClientView;

public class ClientController {
    private ClientModel model;
    private ClientView view;

    public ClientController(ClientModel model, ClientView view) {
        this.model = model;
        this.view = view;
    }

    public void init() {
        boolean connected = model.connect();
        view.initComponents(text -> {
            if (!model.isConnected()) {
                view.setMessage("No hay conexion con el servidor.");
                return null;
            }
            model.register(text);
            return null;
        });
        view.setRegisterEnabled(connected);
        view.setVisible(true);
        if (connected) {
            view.setMessage("Connected to server.");
        } else {
            view.setMessage("Failed to connect to server.");
        }
    }
}
