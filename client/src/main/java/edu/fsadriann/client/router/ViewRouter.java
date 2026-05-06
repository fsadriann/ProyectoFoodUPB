package edu.fsadriann.client.router;

import edu.fsadriann.server.model.user.Rol;
import edu.fsadriann.view.auth.LoginView;
import edu.fsadriann.view.admin.AdminView;
import edu.fsadriann.view.delivery.DeliveryView;
import edu.fsadriann.view.kitchen.KitchenView;
import edu.fsadriann.view.operator.OperatorView;

import javax.swing.JFrame;

public class ViewRouter {
    private final LoginView loginView;
    private final OperatorView operatorView;
    private final AdminView adminView;
    private final KitchenView kitchenView;
    private final DeliveryView deliveryView;


    public ViewRouter(LoginView loginView,
                      OperatorView operatorView,
                      AdminView adminView,
                      KitchenView kitchenView,
                      DeliveryView deliveryView) {
        this.loginView = loginView;
        this.operatorView = operatorView;
        this.adminView = adminView;
        this.kitchenView = kitchenView;
        this.deliveryView = deliveryView;
    }

    public void show(Rol role) {
        hideAll();
        if (role == null) {
            showLogin();
            return;
        }

        switch (role) {
            case ADMIN:
                adminView.showView();
                break;
            case OPERADOR:
                operatorView.setVisible(true);
                break;
            case CLIENTE:
                operatorView.setVisible(true);
                break;
            case COCINA:
                kitchenView.showView();
                break;
            case ENTREGA:
                deliveryView.showView();
                break;
            case SERVER:
                adminView.showView();
                break;
            default:
                showLogin();
                break;
        }
    }

    public void showLogin() {
        hideAll();
        loginView.setVisible(true);
        loginView.toFront();
    }

    private void hideAll() {
        hideFrame(loginView);
        hideFrame(operatorView);
        hideFrame(adminView.getFrame());
        hideFrame(kitchenView.getFrame());
        hideFrame(deliveryView.getFrame());
    }

    private void hideFrame(JFrame frame) {
        if (frame != null) {
            frame.setVisible(false);
        }
    }
}