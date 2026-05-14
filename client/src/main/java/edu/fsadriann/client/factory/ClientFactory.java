package edu.fsadriann.client.factory;

import edu.fsadriann.client.controller.admin.AdminController;
import edu.fsadriann.client.controller.auth.AuthClientController;
import edu.fsadriann.client.controller.delivery.DeliveryController;
import edu.fsadriann.client.controller.kitchen.KitchenController;
import edu.fsadriann.client.controller.operator.OperatorController;
import edu.fsadriann.client.controller.order.OrderController;
import edu.fsadriann.client.controller.product.ProductController;
import edu.fsadriann.client.router.ViewRouter;
import edu.fsadriann.client.model.auth.AuthModel;
import edu.fsadriann.client.model.operator.OperatorModel;
import edu.fsadriann.client.model.kitchen.KitchenModel;
import edu.fsadriann.client.model.admin.AdminModel;
import edu.fsadriann.client.model.delivery.DeliveryModel;
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

        // ── Modelos ───────────────────────────────────────────────────────────
        AuthModel     authModel     = new AuthModel(env.getIp(), env.getPort(), env.getServiceName());
        OperatorModel operatorModel = new OperatorModel();
        KitchenModel  kitchenModel  = new KitchenModel();
        AdminModel    adminModel    = new AdminModel();
        DeliveryModel deliveryModel = new DeliveryModel();

        String userLabel = System.getProperty("user.email",
                System.getenv().getOrDefault("USER_EMAIL", "Usuario"));

        // ── Vistas ────────────────────────────────────────────────────────────
        LoginView    loginView    = new LoginView(userLabel);
        OperatorView operatorView = new OperatorView(userLabel);
        AdminView    adminView    = new AdminView(userLabel);
        KitchenView  kitchenView  = new KitchenView(userLabel);
        DeliveryView deliveryView = new DeliveryView(userLabel);

        ViewRouter router = new ViewRouter(loginView, operatorView, adminView, kitchenView, deliveryView);

        // ── Controladores ─────────────────────────────────────────────────────
        ProductController  productController  = new ProductController(operatorModel, operatorView);
        OrderController    orderController    = new OrderController(operatorModel, operatorView);
        OperatorController operatorController = new OperatorController(operatorModel, operatorView, productController, orderController);
        AdminController    adminController    = new AdminController(adminModel, adminView);
        KitchenController  kitchenController  = new KitchenController(kitchenModel, kitchenView);
        DeliveryController deliveryController = new DeliveryController(deliveryModel, deliveryView, router);

        // ── Listeners de logout ───────────────────────────────────────────────
        adminView.addLogoutListener(() -> {
            authModel.logout();
            router.showLogin();
        });
        kitchenView.addLogoutListener(() -> {
            authModel.logout();
            kitchenController.hide();
            router.showLogin();
        });

        // ── AuthController ────────────────────────────────────────────────────
        return new AuthClientController(
                authModel, operatorModel, kitchenModel, adminModel, deliveryModel,
                loginView,
                operatorController, adminController,
                kitchenController,  deliveryController,
                router);
    }
}