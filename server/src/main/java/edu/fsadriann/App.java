package edu.fsadriann;

import edu.fsadriann.server.controller.ServerController;
import edu.fsadriann.server.factory.ServerFactory;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        try {
            ServerController server = ServerFactory.create();
            server.init();
        } catch (Exception e) {
            System.err.println("Failed to start the server application: " + e.getMessage());
        }
    }
}