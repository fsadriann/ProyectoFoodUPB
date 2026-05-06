package edu.fsadriann;

import edu.fsadriann.server.controller.ServerController;
import edu.fsadriann.server.factory.ServerFactory;

public class App {
    public static void main(String[] args) {
        try {

            ServerController controller = ServerFactory.create();
            controller.show();
        } catch (Exception e) {
            System.err.println("Failed to start the server application: " + e.getMessage());
        }
    }
}