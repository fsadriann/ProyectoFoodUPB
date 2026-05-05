package edu.fsadriann.client.controller.admin;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.client.model.ClientModel;
import edu.fsadriann.server.model.user.Rol;
import edu.fsadriann.server.model.user.User;
import edu.fsadriann.view.admin.AdminUserFormData;
import edu.fsadriann.view.admin.AdminView;

public class AdminController {

    private final ClientModel model;
    private final AdminView   view;
    private boolean initialized;

    public AdminController(ClientModel model, AdminView view) {
        this.model = model;
        this.view  = view;
    }

    public void init() {
        if (!initialized) {
            view.addOpenUserFormListener(() -> view.showUserForm(this::handleCreateUser));
            view.addEditUserListener(this::handleEditUser);
            view.addDeleteUserListener(this::handleDeleteUser);
            initialized = true;
        }
        if (model.isConnected()) {
            refreshUsers();
            view.setTotalUsers(model.getTotalUsuarios());
        }
    }

    // ── Crear ─────────────────────────────────────────────────────────────────

    private void handleCreateUser(AdminUserFormData data) {
        Rol rol = parseRol(data.getRol());
        if (rol == null) { view.setMessage("Rol inválido."); return; }

        User user = new User(
                data.getCorreo(),
                data.getNombre(),
                data.getApellido(),
                rol,
                data.getTelefono(),  // cedula = telefono
                rol == Rol.CLIENTE,
                data.getTelefono(),
                data.getDireccionCompleta(),
                null
        );

        boolean created = model.registrarUsuario(user, data.getContrasena());
        if (!created) { view.setMessage("No se pudo registrar el usuario."); return; }

        refreshUsers();
        view.setTotalUsers(model.getTotalUsuarios());
        view.setMessage("Usuario registrado correctamente.");
    }

    // ── Editar ────────────────────────────────────────────────────────────────

    private void handleEditUser() {
        String telefono = view.getSelectedUserTelefono();
        if (telefono == null) return;

        // Buscar el usuario actual para prellenar el formulario
        LinkedList<User> users = model.listarUsuarios();
        User selected = null;
        edu.fsadriann.model.iterator.Iterator<User> it = users.iterator();
        while (it.hasNext()) {
            User u = it.next();
            if (u != null && telefono.equals(u.getTelefono())) {
                selected = u;
                break;
            }
        }
        if (selected == null) { view.setMessage("No se encontró el usuario."); return; }

        final User userToEdit = selected;
        view.showEditUserForm(
                userToEdit.getNombres(),
                userToEdit.getApellidos(),
                userToEdit.getTelefono(),
                userToEdit.getId(),
                userToEdit.getRol() != null ? userToEdit.getRol().name() : "CLIENTE",
                userToEdit.getDireccion() != null ? userToEdit.getDireccion() : "",
                data -> {
                    Rol rol = parseRol(data.getRol());
                    if (rol == null) { view.setMessage("Rol inválido."); return; }

                    // Contraseña: si dejó vacío, no se cambia
                    if (!data.getContrasena().isBlank()) {
                        try {
                            model.listarUsuarios(); // solo para verificar conexión
                        } catch (Exception e) {
                            view.setMessage("Error de conexión.");
                            return;
                        }
                    }

                    User updated = new User(
                            data.getCorreo(),
                            data.getNombre(),
                            data.getApellido(),
                            rol,
                            userToEdit.getCedula(), // mantiene la cédula original
                            rol == Rol.CLIENTE,
                            data.getTelefono(),
                            data.getDireccionCompleta(),
                            null
                    );

                    boolean ok = model.actualizarCliente(updated);
                    if (!ok) { view.setMessage("No se pudo actualizar el usuario."); return; }

                    refreshUsers();
                    view.setMessage("Usuario actualizado correctamente.");
                }
        );
    }

    // ── Eliminar ──────────────────────────────────────────────────────────────

    private void handleDeleteUser() {
        String telefono = view.getSelectedUserTelefono();
        if (telefono == null) return;

        int confirm = javax.swing.JOptionPane.showConfirmDialog(
                view.getFrame(),
                "¿Eliminar el usuario con teléfono " + telefono + "?",
                "Confirmar eliminación",
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.WARNING_MESSAGE
        );
        if (confirm != javax.swing.JOptionPane.YES_OPTION) return;

        // Buscar cédula por teléfono
        LinkedList<User> users = model.listarUsuarios();
        String cedula = null;
        edu.fsadriann.model.iterator.Iterator<User> it = users.iterator();
        while (it.hasNext()) {
            User u = it.next();
            if (u != null && telefono.equals(u.getTelefono())) {
                cedula = u.getCedula();
                break;
            }
        }
        if (cedula == null) { view.setMessage("No se encontró el usuario."); return; }

        boolean ok = model.eliminarCliente(cedula);
        if (!ok) { view.setMessage("No se pudo eliminar el usuario."); return; }

        refreshUsers();
        view.setTotalUsers(model.getTotalUsuarios());
        view.setMessage("Usuario eliminado correctamente.");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void refreshUsers() {
        LinkedList<User> users = model.listarUsuarios();
        view.setUsers(users);
    }

    private Rol parseRol(String rol) {
        if (rol == null) return null;
        try {
            return Rol.valueOf(rol.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}