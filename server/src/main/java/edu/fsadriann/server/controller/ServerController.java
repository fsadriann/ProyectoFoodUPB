package edu.fsadriann.server.controller;

import edu.fsadriann.server.model.ServerModel;
import edu.fsadriann.server.view.ServerView;

public class ServerController {

    private ServerModel model;
    private ServerView view;

    public ServerController(ServerModel model, ServerView view) {
        this.model = model;
        this.view = view;
    }

    public void init() {
        view.initComponents(
                event -> {
                    // Botón Iniciar
                    if (!model.isRunning()) {
                        if (model.deploy()) {
                            view.startStatus("Servidor iniciado correctamente.");
                        } else {
                            view.setMessage("Error al iniciar el servidor.");
                        }
                    }
                    return null;
                },
                event -> {
                    // Botón Detener
                    if (model.isRunning()) {
                        if (model.stop()) {
                            view.stopStatus("Servidor detenido.");
                        } else {
                            view.setMessage("Error al detener el servidor.");
                        }
                    }
                    return null;
                }
        );
    }
}