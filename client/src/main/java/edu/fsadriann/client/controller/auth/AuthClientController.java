package edu.fsadriann.client.controller.auth;

import edu.fsadriann.client.controller.operator.OperatorController;
import edu.fsadriann.client.model.ClientModel;
import edu.fsadriann.client.router.ViewRouter;
import edu.fsadriann.server.model.user.Rol;
import edu.fsadriann.view.auth.LoginView;

public class AuthClientController {
    private final ClientModel model;
    private final LoginView loginView;
    private final OperatorController operatorController;
    private final ViewRouter viewRouter;

    public AuthClientController(ClientModel model, LoginView loginView, OperatorController operatorController, ViewRouter viewRouter) {
        this.model = model;
        this.loginView = loginView;
        this.operatorController = operatorController;
        this.viewRouter = viewRouter;
    }

    public void init() {
        loginView.addLoginListener(this::handleLogin);
        operatorController.setLogoutAction(this::showLoginView);

        boolean connected = model.connect();
        loginView.setLoginEnabled(true);
        loginView.setMessage(connected
                ? "Conexión establecida. Ingresa tus credenciales."
                : "Sin conexión. Se intentará reconectar al iniciar sesión.");
        loginView.setVisible(true);
    }

    private void handleLogin() {
        if (!model.isConnected() && !model.connect()) {
            loginView.showError("No fue posible conectar con el servidor.");
            return;
        }

        Rol role = model.login(loginView.getUserText(), loginView.getPasswordText());
        if (role == null) {
            loginView.showError("Credenciales inválidas.");
            return;
        }

        loginView.setMessage("Acceso concedido.");
        loginView.setVisible(false);
        if (role == Rol.OPERADOR) {
            operatorController.init();
        }
        viewRouter.show(role);
    }

    private void showLoginView() {
        model.logout();
        viewRouter.showLogin();
        loginView.setVisible(true);
        loginView.setMessage("Sesión cerrada.");
    }
}
