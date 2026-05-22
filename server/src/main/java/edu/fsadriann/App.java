package edu.fsadriann;

import edu.fsadriann.server.controller.ServerController;
import edu.fsadriann.server.factory.ServerFactory;

/**
 * Punto de entrada de la aplicación servidor de Food UPB.
 * Crea el controlador del servidor a través de la fábrica y lo inicia.
 */
public class App {

    /**
     * Método principal que lanza la aplicación servidor.
     *
     * @param args argumentos de línea de comandos (no se utilizan)
     */
    public static void main(String[] args) {
        try {
            ServerController controller = ServerFactory.create();
            controller.show();
        } catch (Exception e) {
            System.err.println("Failed to start the server application: " + e.getMessage());
        }
    }
}
