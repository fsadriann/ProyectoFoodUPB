package edu.fsadriann.client.controller.admin;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.client.model.ClientModel;
import edu.fsadriann.model.iterator.Iterator;
import edu.fsadriann.server.model.user.Rol;
import edu.fsadriann.server.model.user.User;
import edu.fsadriann.view.admin.AdminUserFormData;
import edu.fsadriann.view.admin.AdminView;

public class AdminController {

    private final ClientModel model;
    private final AdminView view;
    private boolean initialized;

    public AdminController(ClientModel model, AdminView view) {
        this.model = model;
        this.view = view;
    }

    public void init() {
        if (!initialized) {
            view.addOpenUserFormListener(() -> view.showUserForm(this::handleCreateUser));
            initialized = true;
        }
        // Siempre refrescar datos al iniciar (incluye re-logins)
        if (model.isConnected()) {
            refreshUsers();
            view.setTotalUsers(model.getTotalUsuarios());
        }
    }

    private void handleCreateUser(AdminUserFormData data) {
        Rol rol = parseRol(data.getRol());
        if (rol == null) {
            view.setMessage("Rol inválido.");
            return;
        }


        String telefono = "";
        User user = new User(
                data.getCorreo(),
                data.getNombre(),
                data.getApellido(),
                rol,
                data.getTelefono(),
                rol == Rol.CLIENTE,
                telefono,
                data.getDireccionCompleta(),
                null
        );

        boolean created = model.registrarUsuario(user, data.getContrasena());
        if (!created) {
            view.setMessage("No se pudo registrar el usuario.");
            return;
        }

        refreshUsers();
        view.setTotalUsers(model.getTotalUsuarios());
        view.setMessage("Usuario registrado correctamente.");
    }

    private void refreshUsers() {
        LinkedList<User> users = model.listarUsuarios();
        view.setUsers(users);
    }

    private Rol parseRol(String rol) {
        if (rol == null) {
            return null;
        }
        try {
            return Rol.valueOf(rol.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
