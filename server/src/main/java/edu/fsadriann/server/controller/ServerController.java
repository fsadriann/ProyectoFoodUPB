package edu.fsadriann.server.controller;

import edu.fsadriann.server.model.ServerModel;
import edu.fsadriann.server.view.ServerView;

/**
 * Controlador principal del servidor Food UPB.
 * Conecta la vista con el modelo y gestiona las acciones de inicio y detención del servidor.
 */
public class ServerController {

    private ServerModel model;
    private ServerView  view;

    /**
     * Crea el controlador con el modelo y la vista del servidor.
     *
     * @param model modelo que gestiona los servicios RMI
     * @param view  vista del panel de control del servidor
     */
    public ServerController(ServerModel model, ServerView view) {
        this.model = model;
        this.view  = view;
    }

    /**
     * Inicializa la vista y registra las acciones de los botones de inicio y detención.
     */
    public void init() {
        view.initComponents(
                event -> {
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

    /**
     * Muestra la interfaz del servidor e inicializa los componentes.
     */
    public void show() {
        init();
    }
}
