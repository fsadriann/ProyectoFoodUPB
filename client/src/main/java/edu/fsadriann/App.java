package edu.fsadriann;

import edu.fsadriann.client.controller.ClientController;
import edu.fsadriann.client.factory.ClientFactory;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        try {
            ClientController client = ClientFactory.create();
            client.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
