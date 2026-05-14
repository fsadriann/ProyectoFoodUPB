package edu.fsadriann.client.controller.admin;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.client.model.admin.AdminModel;
import edu.fsadriann.server.model.cuadrante.Cuadrante;
import edu.fsadriann.server.model.order.Order;
import edu.fsadriann.server.model.user.Rol;
import edu.fsadriann.server.model.user.User;
import edu.fsadriann.view.admin.AdminUserFormData;
import edu.fsadriann.view.admin.AdminView;
import edu.fsadriann.server.model.product.Product;

public class AdminController {

    private static final double UMBRAL_VECINO_KM = 0.4;

    private final AdminModel model;
    private final AdminView   view;
    private boolean initialized;

    public AdminController(AdminModel model, AdminView view) {
        this.model = model;
        this.view  = view;
    }

    public void init() {
        if (!initialized) {
            view.addRefreshReportsListener(this::refreshReportes);
            view.addRefreshAuditListener(this::refreshBitacora);

            // Cuadrantes
            view.addOpenCuadFormListener(() -> view.showCuadForm(null, this::handleCreateCuadrante));
            view.addEditCuadListener(this::handleEditCuadrante);
            view.addDeleteCuadListener(this::handleDeleteCuadrante);

            // Usuarios
            view.addOpenUserFormListener(() -> view.showUserForm(getCuadrantesNombres(), this::handleCreateUser));
            view.addEditUserListener(this::handleEditUser);
            view.addDeleteUserListener(this::handleDeleteUser);

            // Productos
            view.addOpenProductFormListener(() -> view.showProductForm(null, this::handleCreateProduct));
            view.addEditProductListener(this::handleEditProduct);
            view.addToggleProductListener(this::handleToggleProduct);

            initialized = true;
        }
        if (model.isConnected()) {
            refreshBitacora();
            refreshReportes();
            refreshCuadrantes();
            refreshUsers();
            refreshProducts();
            view.setTotalUsers(model.getTotalUsuarios());
        }
    }

    // ── Cuadrantes ────────────────────────────────────────────────────────────

    private void handleCreateCuadrante(String[] data) {
        // data: [nombre, descripcion, distanciaKm]
        double distanciaDesdeUPB = 0;
        try { distanciaDesdeUPB = Double.parseDouble(data[2]); }
        catch (Exception ignored) {}

        Cuadrante nuevo = new Cuadrante(data[0], data[1], distanciaDesdeUPB);
        boolean ok = model.agregarCuadrante(nuevo);
        if (!ok) { view.setMessage("No se pudo agregar el cuadrante (ya existe)."); return; }

        StringBuilder msg = new StringBuilder("Cuadrante '" + data[0] + "' agregado.");

        // Conectar automáticamente a UPB
        if (distanciaDesdeUPB > 0) {
            boolean conectadoUPB = model.conectarCuadrantes("UPB", data[0], distanciaDesdeUPB);
            if (conectadoUPB) msg.append(" Conectado a UPB (").append(data[2]).append(" km).");
        }

        // Auto-conectar a nodos vecinos con distancia similar
        LinkedList<Cuadrante> existentes = model.listarCuadrantes();
        edu.fsadriann.model.iterator.Iterator<Cuadrante> it = existentes.iterator();
        int autoConexiones = 0;
        while (it.hasNext()) {
            Cuadrante otro = it.next();
            if (otro == null) continue;
            if (otro.getNombre().equals(data[0])) continue;
            if ("UPB".equals(otro.getNombre())) continue;

            double distOtro       = otro.getDistanciaDesdeUPB();
            double distEntreNodos = Math.abs(distanciaDesdeUPB - distOtro);

            if (distEntreNodos <= UMBRAL_VECINO_KM) {
                boolean conectado = model.conectarCuadrantes(
                        data[0], otro.getNombre(), Math.max(distEntreNodos, 0.05));
                if (conectado) autoConexiones++;
            }
        }

        if (autoConexiones > 0)
            msg.append(" Auto-conectado a ").append(autoConexiones).append(" nodo(s) vecino(s).");

        refreshCuadrantes();
        view.setMessage(msg.toString());
    }

    private void handleEditCuadrante() {
        String nombre = view.getSelectedCuadNombre();
        if (nombre == null) return;

        Cuadrante selected = findCuadrante(nombre);
        if (selected == null) { view.setMessage("No se encontró el cuadrante."); return; }

        view.showCuadForm(
                new String[]{
                        selected.getNombre(),
                        selected.getDescripcion() != null ? selected.getDescripcion() : "",
                        String.valueOf(selected.getDistanciaDesdeUPB())
                },
                data -> {
                    double nuevaDist = 0;
                    try { nuevaDist = Double.parseDouble(data[2]); }
                    catch (Exception ignored) {}

                    Cuadrante actualizado = new Cuadrante(data[0], data[1], nuevaDist);
                    boolean ok = model.editarCuadrante(actualizado);
                    if (!ok) { view.setMessage("No se pudo editar el cuadrante."); return; }

                    // Actualizar conexión a UPB si cambió la distancia
                    if (nuevaDist > 0 && nuevaDist != selected.getDistanciaDesdeUPB()) {
                        model.conectarCuadrantes("UPB", data[0], nuevaDist);
                    }

                    refreshCuadrantes();
                    view.setMessage("Cuadrante '" + data[0] + "' actualizado.");
                }
        );
    }

    private void handleDeleteCuadrante() {
        String nombre = view.getSelectedCuadNombre();
        if (nombre == null) return;

        int confirm = javax.swing.JOptionPane.showConfirmDialog(
                view.getFrame(),
                "¿Eliminar el cuadrante '" + nombre + "'?\nEsta acción no se puede deshacer.",
                "Confirmar eliminación",
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.WARNING_MESSAGE
        );
        if (confirm != javax.swing.JOptionPane.YES_OPTION) return;

        boolean ok = model.eliminarCuadrante(nombre);
        if (!ok) { view.setMessage("No se pudo eliminar el cuadrante."); return; }

        refreshCuadrantes();
        view.setMessage("Cuadrante '" + nombre + "' eliminado.");
    }

    private void refreshCuadrantes() {
        LinkedList<Cuadrante> cuadrantes = model.listarCuadrantes();
        view.setCuadrantes(cuadrantes);
    }

    private Cuadrante findCuadrante(String nombre) {
        LinkedList<Cuadrante> lista = model.listarCuadrantes();
        edu.fsadriann.model.iterator.Iterator<Cuadrante> it = lista.iterator();
        while (it.hasNext()) {
            Cuadrante c = it.next();
            if (c != null && c.getNombre().equals(nombre)) return c;
        }
        return null;
    }

    // ── Reportes ──────────────────────────────────────────────────────────────

    private void refreshReportes() {
        LinkedList<Order> pedidos = model.getPedidosTodos();
        view.setReportePedidos(pedidos);

        int total = 0, enCocina = 0, entregados = 0;
        int complejosCount = 0, rapidosCount = 0;
        double ingresos = 0;

        edu.fsadriann.model.iterator.Iterator<Order> it = pedidos.iterator();
        while (it.hasNext()) {
            Order o = it.next();
            if (o == null) continue;
            total++;
            if (o.getEstado() == edu.fsadriann.server.model.order.EstadoPedido.EN_PREPARACION) enCocina++;
            if (o.getEstado() == edu.fsadriann.server.model.order.EstadoPedido.ENTREGADO)      entregados++;
            if (o.getTotal() > 0) ingresos += o.getTotal();

            // Clasificar por tipo de productos en el carrito
            if (o.getCarrito() != null) {
                edu.fsadriann.model.iterator.Iterator<edu.fsadriann.server.model.product.Product> ip =
                        o.getCarrito().iterator();
                boolean tieneComplejo = false;
                while (ip.hasNext()) {
                    edu.fsadriann.server.model.product.Product p = ip.next();
                    if (p != null && p.isComplejo()) { tieneComplejo = true; break; }
                }
                if (tieneComplejo) complejosCount++;
                else rapidosCount++;
            }
        }

        // Estimado: complejo = 15 min, rápido = 8 min
        String tiempoProm = "—";
        if (total > 0) {
            double promedio = (complejosCount * 15.0 + rapidosCount * 8.0) / total;
            tiempoProm = String.format("%.0f min", promedio);
        }

        view.setMetricPedidos(total);
        view.setMetricCocina(enCocina);
        view.setMetricEntregas(entregados);
        view.setReporteMetricas(total, ingresos, model.getTotalUsuarios());
        view.setMetricTiempoProm(tiempoProm);
    }

    private void refreshBitacora() {
        LinkedList<String> eventos = model.verBitacora();
        view.setBitacora(eventos);
    }

    // ── Productos ─────────────────────────────────────────────────────────────

    private void handleCreateProduct(String[] data) {
        Product p = new Product(
                java.util.UUID.randomUUID().toString(),
                data[0], data[3],
                Integer.parseInt(data[2]),
                "Sí".equals(data[4])
        );
        p.setDescripcion(data[1]);

        boolean ok = model.agregarProducto(p);
        if (!ok) { view.setMessage("No se pudo agregar el producto."); return; }

        refreshProducts();
        view.setMessage("Producto agregado correctamente.");
    }

    private void handleEditProduct() {
        String id = view.getSelectedProductId();
        if (id == null) return;

        Product selected = model.buscarProductoPorId(id);
        if (selected == null) { view.setMessage("No se encontró el producto."); return; }

        view.showProductForm(
                new String[]{
                        selected.getNombre(),
                        selected.getDescripcion() != null ? selected.getDescripcion() : "",
                        String.valueOf(selected.getPrecio()),
                        selected.getCategoria(),
                        selected.isComplejo() ? "Sí" : "No"
                },
                data -> {
                    Product updated = new Product(
                            selected.getProductoId(), data[0], data[3],
                            Integer.parseInt(data[2]), "Sí".equals(data[4]));
                    updated.setDescripcion(data[1]);
                    updated.setDisponible(selected.isDisponible());

                    boolean ok = model.editarProducto(updated);
                    if (!ok) { view.setMessage("No se pudo editar el producto."); return; }

                    refreshProducts();
                    view.setMessage("Producto actualizado correctamente.");
                }
        );
    }

    private void handleToggleProduct() {
        String id = view.getSelectedProductId();
        if (id == null) return;

        boolean disponibleActual = view.getSelectedProductDisponible();
        boolean ok = model.toggleDisponibilidadProducto(id, !disponibleActual);
        if (!ok) { view.setMessage("No se pudo cambiar la disponibilidad."); return; }

        refreshProducts();
        view.setMessage(disponibleActual ? "Producto desactivado." : "Producto activado.");
    }

    private void refreshProducts() {
        LinkedList<Product> products = model.listarProductos();
        view.setProducts(products);
    }

    // ── Usuarios ──────────────────────────────────────────────────────────────

    private void handleCreateUser(AdminUserFormData data) {
        Rol rol = parseRol(data.getRol());
        if (rol == null) { view.setMessage("Rol inválido."); return; }

        String cedula = rol == Rol.CLIENTE
                ? "CLI-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase()
                : data.getTelefono();

        String contrasena = data.getContrasena();
        if (contrasena == null || contrasena.isBlank()) {
            contrasena = data.getTelefono();
        }

        User user = new User(
                data.getCorreo(), data.getNombre(), data.getApellido(), rol,
                cedula, data.isPremium(),
                data.getTelefono(), data.getDireccionCompleta(), null
        );

        if (data.getCuadrante() != null && !data.getCuadrante().isBlank())
            user.setCuadrante(data.getCuadrante());

        boolean created = model.registrarUsuario(user, contrasena);
        System.out.println("[DEBUG] registrarUsuario -> " + created + " | pass: '" + contrasena + "'"); // ← aquí
        if (!created) { view.setMessage("No se pudo registrar el usuario."); return; }

        refreshUsers();
        view.setTotalUsers(model.getTotalUsuarios());
        view.setMessage("Usuario registrado correctamente.");
    }
    private void handleEditUser() {
        String telefono = view.getSelectedUserTelefono();
        if (telefono == null) return;

        LinkedList<User> users = model.listarUsuarios();
        User selected = null;
        edu.fsadriann.model.iterator.Iterator<User> it = users.iterator();
        while (it.hasNext()) {
            User u = it.next();
            if (u != null && telefono.equals(u.getTelefono())) { selected = u; break; }
        }
        if (selected == null) { view.setMessage("No se encontró el usuario."); return; }

        final User userToEdit = selected;
        view.showEditUserForm(
                userToEdit.getNombres(), userToEdit.getApellidos(),
                userToEdit.getTelefono(), userToEdit.getId(),
                userToEdit.getRol() != null ? userToEdit.getRol().name() : "CLIENTE",
                userToEdit.getDireccion() != null ? userToEdit.getDireccion() : "",
                getCuadrantesNombres(),
                data -> {
                    Rol rol = parseRol(data.getRol());
                    if (rol == null) { view.setMessage("Rol inválido."); return; }

                    User updated = new User(
                            data.getCorreo(), data.getNombre(), data.getApellido(), rol,
                            userToEdit.getCedula(), data.isPremium(),
                            data.getTelefono(), data.getDireccionCompleta(), null
                    );
                    if (data.getCuadrante() != null && !data.getCuadrante().isBlank())
                        updated.setCuadrante(data.getCuadrante());

                    boolean ok = model.actualizarCliente(updated);
                    if (!ok) { view.setMessage("No se pudo actualizar el usuario."); return; }

                    refreshUsers();
                    view.setMessage("Usuario actualizado correctamente.");
                }
        );
    }

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

        LinkedList<User> users = model.listarUsuarios();
        String cedula = null;
        edu.fsadriann.model.iterator.Iterator<User> it = users.iterator();
        while (it.hasNext()) {
            User u = it.next();
            if (u != null && telefono.equals(u.getTelefono())) { cedula = u.getCedula(); break; }
        }
        if (cedula == null) { view.setMessage("No se encontró el usuario."); return; }

        boolean ok = model.eliminarCliente(cedula);
        if (!ok) { view.setMessage("No se pudo eliminar el usuario."); return; }

        refreshUsers();
        view.setTotalUsers(model.getTotalUsuarios());
        view.setMessage("Usuario eliminado correctamente.");
    }

    private void refreshUsers() {
        LinkedList<User> users = model.listarUsuarios();
        view.setUsers(users);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String[] getCuadrantesNombres() {
        LinkedList<Cuadrante> cuads = model.listarCuadrantes();
        java.util.List<String> nombres = new java.util.ArrayList<>();
        edu.fsadriann.model.iterator.Iterator<Cuadrante> it = cuads.iterator();
        while (it.hasNext()) {
            Cuadrante c = it.next();
            if (c != null && !"UPB".equals(c.getNombre())) nombres.add(c.getNombre());
        }
        return nombres.toArray(new String[0]);
    }

    private Rol parseRol(String rol) {
        if (rol == null) return null;
        try { return Rol.valueOf(rol.trim().toUpperCase()); }
        catch (IllegalArgumentException ex) { return null; }
    }
}