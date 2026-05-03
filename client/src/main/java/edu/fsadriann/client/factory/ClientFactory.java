package edu.fsadriann.client.factory;

import edu.fsadriann.client.controller.ClientController;
import edu.fsadriann.client.model.ClientModel;
import edu.fsadriann.view.ClientView;
import edu.fsadriann.environment.Environment;

public class ClientFactory {

    private ClientFactory() {
    }

    public static ClientController create() {

        Environment env = null;

        try {
            env = Environment.getInstance();
        } catch (Exception e) {
            System.err.println("Failed to initialize Environment: " + e.getMessage());
        }

        if (env == null) {
            throw new IllegalStateException("Environment is not initialized");
        }

        ClientModel model = new ClientModel(env.getIp(), env.getPort(), env.getServiceName());
        if (model == null) {
            throw new IllegalStateException("Failed to create ClientModel");
        }

        String operadorEmail = System.getProperty("user.email", System.getenv().getOrDefault("USER_EMAIL", "Operador"));
        ClientView view = new ClientView(operadorEmail);
        if (view == null) {
            throw new IllegalStateException("Failed to create ClientView");
        }

        ClientController controller = new ClientController(model, view);
        if (controller == null) {
            throw new IllegalStateException("Failed to create ClientController");
        }
        return controller;
    }
}
