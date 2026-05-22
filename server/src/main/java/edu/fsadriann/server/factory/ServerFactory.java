package edu.fsadriann.server.factory;

import edu.fsadriann.environment.Environment;
import edu.fsadriann.server.controller.ServerController;
import edu.fsadriann.server.model.ServerModel;
import edu.fsadriann.server.model.history.History;
import edu.fsadriann.server.view.ServerView;

/**
 * Fábrica que construye y conecta todos los componentes del servidor Food UPB.
 * Sigue el patrón Factory para centralizar la creación de la aplicación servidor.
 */
public class ServerFactory {

    private ServerFactory() {
    }

    /**
     * Crea e inicializa el controlador del servidor con todos sus dependencias.
     *
     * @return controlador del servidor listo para mostrar
     * @throws IllegalStateException si alguno de los componentes no pudo crearse
     */
    public static ServerController create() {

        Environment env = Environment.getInstance();
        if (env == null) {
            throw new IllegalStateException("Failed to create Environment");
        }

        History history = new History();
        if (history == null) {
            throw new IllegalStateException("Failed to create History");
        }

        ServerModel model = new ServerModel(env.getIp(), env.getPort(), env.getServiceName());
        if (model == null) {
            throw new IllegalStateException("Failed to create ServerModel");
        }

        ServerView view = new ServerView("Server Control Panel", history);
        if (view == null) {
            throw new IllegalStateException("Failed to create ServerView");
        }

        ServerController controller = new ServerController(model, view);
        if (controller == null) {
            throw new IllegalStateException("Failed to create ServerController");
        }

        return controller;
    }
}
