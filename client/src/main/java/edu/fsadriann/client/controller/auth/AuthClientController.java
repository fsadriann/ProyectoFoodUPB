package edu.fsadriann.client.controller.auth;

import edu.fsadriann.client.controller.admin.AdminController;
import edu.fsadriann.client.controller.delivery.DeliveryController;
import edu.fsadriann.client.controller.kitchen.KitchenController;
import edu.fsadriann.client.controller.operator.OperatorController;
import edu.fsadriann.client.model.auth.AuthModel;
import edu.fsadriann.client.model.operator.OperatorModel;
import edu.fsadriann.client.model.kitchen.KitchenModel;
import edu.fsadriann.client.model.admin.AdminModel;
import edu.fsadriann.client.model.delivery.DeliveryModel;
import edu.fsadriann.client.router.ViewRouter;
import edu.fsadriann.server.model.user.Rol;
import edu.fsadriann.view.auth.LoginView;

public class AuthClientController {

    private final AuthModel          authModel;
    private final OperatorModel      operatorModel;
    private final KitchenModel       kitchenModel;
    private final AdminModel         adminModel;
    private final DeliveryModel      deliveryModel;
    private final LoginView          loginView;
    private final OperatorController operatorController;
    private final AdminController    adminController;
    private final KitchenController  kitchenController;
    private final DeliveryController deliveryController;
    private final ViewRouter         viewRouter;

    private boolean kitchenInitialized = false;

    public AuthClientController(AuthModel authModel,
                                OperatorModel operatorModel,
                                KitchenModel kitchenModel,
                                AdminModel adminModel,
                                DeliveryModel deliveryModel,
                                LoginView loginView,
                                OperatorController operatorController,
                                AdminController adminController,
                                KitchenController kitchenController,
                                DeliveryController deliveryController,
                                ViewRouter viewRouter) {
        this.authModel          = authModel;
        this.operatorModel      = operatorModel;
        this.kitchenModel       = kitchenModel;
        this.adminModel         = adminModel;
        this.deliveryModel      = deliveryModel;
        this.loginView          = loginView;
        this.operatorController = operatorController;
        this.adminController    = adminController;
        this.kitchenController  = kitchenController;
        this.deliveryController = deliveryController;
        this.viewRouter         = viewRouter;
    }

    public void init() {
        loginView.addLoginListener(this::handleLogin);
        operatorController.setLogoutAction(this::showLoginView);
        kitchenController.setLogoutAction(this::showLoginView);

        boolean connected = authModel.connect();
        if (connected) injectServices();

        loginView.setLoginEnabled(true);
        loginView.setMessage(connected
                ? "Conexion establecida. Ingresa tus credenciales."
                : "Sin conexion. Se intentara reconectar al iniciar sesion.");
        loginView.setVisible(true);
    }

    private void handleLogin() {
        if (!authModel.isConnected()) {
            boolean reconnected = authModel.connect();
            if (!reconnected) {
                loginView.showError("No fue posible conectar con el servidor.");
                return;
            }
            injectServices();
        }

        Rol role = authModel.login(loginView.getUserText(), loginView.getPasswordText());
        if (role == null) {
            loginView.showError("Credenciales invalidas.");
            return;
        }

        loginView.setMessage("Acceso concedido.");
        loginView.setVisible(false);

        if (role == Rol.OPERADOR) operatorController.init();
        if (role == Rol.ADMIN)    adminController.init();
        if (role == Rol.COCINA) {
            if (!kitchenInitialized) {
                kitchenController.init();
                kitchenInitialized = true;
            } else {
                kitchenController.show();
            }
        }
        if (role == Rol.ENTREGA) deliveryController.init();

        viewRouter.show(role);
    }

    private void showLoginView() {
        authModel.logout();
        operatorModel.logout();
        kitchenInitialized = false;
        viewRouter.showLogin();
        loginView.setVisible(true);
        loginView.setMessage("Sesion cerrada.");
    }

    private void injectServices() {
        operatorModel.injectServices(
                authModel.getUserService(),
                authModel.getProductService(),
                authModel.getOrderService(),
                authModel.getCuadranteService());

        kitchenModel.injectServices(
                authModel.getKitchenService(),
                authModel.getOrderService());

        adminModel.injectServices(
                authModel.getUserService(),
                authModel.getProductService(),
                authModel.getOrderService(),
                authModel.getAdminService(),
                authModel.getCuadranteService());

        deliveryModel.injectServices(
                authModel.getOrderService(),
                authModel.getDeliveryService(),
                authModel.getCuadranteService(),
                authModel.getAdminService());
    }
}