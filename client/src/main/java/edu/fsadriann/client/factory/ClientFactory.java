package edu.fsadriann.client.factory;

import edu.fsadriann.client.controller.admin.AdminController;
import edu.fsadriann.client.controller.auth.AuthClientController;
import edu.fsadriann.client.controller.operator.OperatorController;
import edu.fsadriann.client.controller.order.OrderController;
import edu.fsadriann.client.controller.product.ProductController;
import edu.fsadriann.client.router.ViewRouter;
import edu.fsadriann.client.model.ClientModel;
import edu.fsadriann.view.auth.LoginView;
import edu.fsadriann.view.admin.AdminView;
import edu.fsadriann.view.delivery.DeliveryView;
import edu.fsadriann.view.kitchen.KitchenView;
import edu.fsadriann.view.operator.OperatorView;
import edu.fsadriann.environment.Environment;

public class ClientFactory {

    private ClientFactory() {
    }

    public static AuthClientController create() {

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

        String operadorEmail = System.getProperty("user.email", System.getenv().getOrDefault("USER_EMAIL", "Operador"));
        LoginView loginView = new LoginView(operadorEmail);
        OperatorView view = new OperatorView(operadorEmail);
        AdminView adminView = new AdminView(operadorEmail);
        KitchenView kitchenView = new KitchenView(operadorEmail);
        DeliveryView deliveryView = new DeliveryView(operadorEmail);

        ProductController productController = new ProductController(model, view);
        OrderController orderController = new OrderController(model, view);
        OperatorController operatorController = new OperatorController(model, view, productController, orderController);
        AdminController adminController = new AdminController(model, adminView);
        adminController.init();

        ViewRouter router = new ViewRouter(loginView, view, adminView, kitchenView, deliveryView);

        adminView.addLogoutListener(() -> {
            model.logout();
            router.showLogin();
        });
        kitchenView.addLogoutListener(() -> {
            model.logout();
            router.showLogin();
        });
        deliveryView.addLogoutListener(() -> {
            model.logout();
            router.showLogin();
        });

        return new AuthClientController(model, loginView, operatorController, router);
    }
}
