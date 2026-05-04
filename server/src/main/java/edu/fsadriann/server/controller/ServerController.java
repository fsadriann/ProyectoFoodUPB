package edu.fsadriann.server.controller;

import edu.fsadriann.server.model.ServerModel;
import edu.fsadriann.server.view.ServerView;

import java.lang.reflect.Method;

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
                },
                this::openClientLogin
        );
    }

    public void show() {
        init();
    }

    private void openClientLogin() {
        try {
            Class<?> clientApp = Class.forName("edu.fsadriann.App");
            Method main = clientApp.getMethod("main", String[].class);
            main.invoke(null, (Object) new String[0]);
            view.setMessage("Login abierto.");
        } catch (Exception e) {
            view.setMessage("No fue posible abrir Login: " + e.getMessage());
        }
    }
}