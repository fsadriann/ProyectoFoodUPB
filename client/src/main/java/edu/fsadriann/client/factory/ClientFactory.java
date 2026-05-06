package edu.fsadriann.client.factory;

import edu.fsadriann.client.controller.admin.AdminController;
import edu.fsadriann.client.controller.auth.AuthClientController;
import edu.fsadriann.client.controller.delivery.DeliveryController;
import edu.fsadriann.client.controller.kitchen.KitchenController;
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

    private ClientFactory() {}

    public static AuthClientController create() {

        Environment env = null;
        try {
            env = Environment.getInstance();
        } catch (Exception e) {
            System.err.println("Failed to initialize Environment: " + e.getMessage());
        }
        if (env == null) throw new IllegalStateException("Environment is not initialized");

        ClientModel model = new ClientModel(env.getIp(), env.getPort(), env.getServiceName());

        String userLabel = System.getProperty("user.email",
                System.getenv().getOrDefault("USER_EMAIL", "Usuario"));

        // ── Vistas ────────────────────────────────────────────────────────────
        LoginView    loginView    = new LoginView(userLabel);
        OperatorView operatorView = new OperatorView(userLabel);
        AdminView    adminView    = new AdminView(userLabel);
        KitchenView  kitchenView  = new KitchenView(userLabel);
        DeliveryView deliveryView = new DeliveryView(userLabel);

        // ── Controladores ─────────────────────────────────────────────────────
        ProductController  productController  = new ProductController(model, operatorView);
        OrderController    orderController    = new OrderController(model, operatorView);
        OperatorController operatorController = new OperatorController(model, operatorView, productController, orderController);
        AdminController    adminController    = new AdminController(model, adminView);
        KitchenController  kitchenController  = new KitchenController(model, kitchenView);
        DeliveryController deliveryController = new DeliveryController(model, deliveryView);

        // ── Router ────────────────────────────────────────────────────────────
        ViewRouter router = new ViewRouter(loginView, operatorView, adminView, kitchenView, deliveryView);

        // ── Listeners de logout (los que no están en sus propios controladores)
        adminView.addLogoutListener(() -> {
            model.logout();
            router.showLogin();
        });
        kitchenView.addLogoutListener(() -> {
            model.logout();
            kitchenController.hide();
            router.showLogin();
        });
        // El logout de deliveryView ya está manejado dentro de DeliveryController

        // ── AuthController ────────────────────────────────────────────────────
        return new AuthClientController(
                model, loginView,
                operatorController, adminController,
                kitchenController,  deliveryController,
                router);
    }
}