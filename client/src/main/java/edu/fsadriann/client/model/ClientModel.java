package edu.fsadriann.client.model;

import edu.fsadriann.server.model.admin.AdminInterface;
import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.server.model.order.KitchenInterface;
import edu.fsadriann.server.model.order.OrderInterface;
import edu.fsadriann.server.model.product.ProductInterface;
import edu.fsadriann.server.model.observer.Subject;
import edu.fsadriann.server.model.order.Order;
import edu.fsadriann.server.model.product.Product;
import edu.fsadriann.server.model.user.Rol;
import edu.fsadriann.server.model.user.UserInterface;
import edu.fsadriann.server.model.user.User;

import java.rmi.Naming;

public class ClientModel extends Subject {
    private final String baseUri;
    private final String usersUri;
    private final String productsUri;
    private final String ordersUri;
    private final String kitchenUri;
    private final String adminUri;
    private UserInterface userService;
    private ProductInterface productService;
    private OrderInterface orderService;
    private KitchenInterface kitchenService;
    private AdminInterface adminService;
    private boolean connected;
    private String logger;
    private Rol currentRole;
    private User currentClient;
    private Order currentOrder;

    public ClientModel(String ip, int port, String serviceName) {
        this.baseUri = "rmi://" + ip + ":" + port + "/" + serviceName;
        this.usersUri = baseUri + "-users";
        this.productsUri = baseUri + "-products";
        this.ordersUri = baseUri + "-orders";
        this.kitchenUri = baseUri + "-kitchen";
        this.adminUri = baseUri + "-admin";
        this.connected = false;
    }

    public boolean connect() {
        try {
            this.userService = (UserInterface) Naming.lookup(usersUri);
            this.productService = (ProductInterface) Naming.lookup(productsUri);
            this.orderService = (OrderInterface) Naming.lookup(ordersUri);
            this.kitchenService = (KitchenInterface) Naming.lookup(kitchenUri);
            this.adminService = (AdminInterface) Naming.lookup(adminUri);
            this.connected = true;
            updateLogger("Conectado al servidor en " + baseUri);
            return true;
        } catch (Exception e) {
            this.connected = false;
            this.userService = null;
            this.productService = null;
            this.orderService = null;
            this.kitchenService = null;
            this.adminService = null;
            updateLogger("No se pudo conectar al servidor en " + baseUri + ": " + e.getMessage());
            System.err.println(this.logger);
            return false;
        }
    }

    public Rol login(String correo, String contrasena) {
        if (!ensureConnected()) {
            return null;
        }
        try {
            this.currentRole = userService.login(correo, contrasena);
            if (this.currentRole != null) {
                updateLogger("Inicio de sesión exitoso.");
            } else {
                updateLogger("Credenciales inválidas.");
            }
            return this.currentRole;
        } catch (Exception e) {
            updateLogger("Error al iniciar sesión: " + e.getMessage());
            return null;
        }
    }

    public void logout() {
        currentRole = null;
        currentClient = null;
        currentOrder = null;
    }

    public User buscarClientePorTelefono(String telefono) {
        if (!ensureConnected()) {
            return null;
        }
        try {
            User user = userService.buscarClientePorTelefono(telefono);
            currentClient = user;
            updateLogger(user != null
                    ? "Cliente encontrado: " + user.getNombreCompleto().trim()
                    : "No se encontró cliente con ese teléfono.");
            return user;
        } catch (Exception e) {
            updateLogger("Error al buscar cliente: " + e.getMessage());
            return null;
        }
    }

    public User registrarCliente(User user) {
        if (!ensureConnected()) {
            return null;
        }
        try {
            User created = userService.registrarCliente(user);
            updateLogger("Cliente registrado correctamente.");
            return created;
        } catch (Exception e) {
            updateLogger("Error al registrar cliente: " + e.getMessage());
            return null;
        }
    }

    public LinkedList<User> listarUsuarios() {
        if (!ensureConnected()) {
            return new edu.fsadriann.app.linkedlist.singly.singly.LinkedList<>();
        }
        try {
            return userService.listarUsuarios();
        } catch (Exception e) {
            updateLogger("Error al listar usuarios: " + e.getMessage());
            return new edu.fsadriann.app.linkedlist.singly.singly.LinkedList<>();
        }
    }

    public boolean registrarUsuario(User user, String contrasena) {
        if (!ensureConnected() || user == null || contrasena == null || contrasena.isBlank()) {
            return false;
        }
        try {
            if (user.getRol() == Rol.CLIENTE) {
                User created = userService.registrarCliente(user);
                if (created == null) {
                    updateLogger("No se pudo registrar el usuario.");
                    return false;
                }
                userService.registrarCredencial(created.getCedula(), contrasena);
                updateLogger("Usuario registrado correctamente.");
                return true;
            }

            boolean creado = adminService.crearOperador(user);
            boolean rolAsignado = adminService.asignarRol(user.getCedula(), user.getRol());
            if (!creado || !rolAsignado) {
                updateLogger("No se pudo registrar el usuario operativo.");
                return false;
            }
            // Agregar también a UserService para que aparezca en listarUsuarios()
            // y para que registrarCredencial() pueda encontrar al usuario.
            try { userService.registrarCliente(user); } catch (Exception ignored) {}
            userService.registrarCredencial(user.getCedula(), contrasena);
            updateLogger("Usuario operativo registrado correctamente.");
            return true;
        } catch (Exception e) {
            updateLogger("Error al registrar usuario: " + e.getMessage());
            return false;
        }
    }

    public boolean registrarUsuarioOperativo(User user, Rol rol) {
        if (!ensureConnected() || adminService == null || user == null || rol == null) {
            return false;
        }
        try {
            boolean creado = adminService.crearOperador(user);
            if (!creado) {
                updateLogger("No se pudo crear el usuario operativo.");
                return false;
            }
            boolean rolAsignado = adminService.asignarRol(user.getCedula(), rol);
            updateLogger(rolAsignado ? "Usuario operativo registrado." : "No se pudo asignar el rol.");
            return rolAsignado;
        } catch (Exception e) {
            updateLogger("Error al registrar usuario operativo: " + e.getMessage());
            return false;
        }
    }

    public int getTotalUsuarios() {
        if (!ensureConnected()) {
            return 0;
        }
        try {
            return userService.getTotalUsuarios();
        } catch (Exception e) {
            updateLogger("Error al obtener el total de usuarios: " + e.getMessage());
            return 0;
        }
    }

    public LinkedList<Product> listarProductos() {
        if (!ensureConnected()) {
            return new LinkedList<>();
        }
        try {
            LinkedList<Product> products = productService.listarProductos();
            updateLogger("Catálogo cargado.");
            return products;
        } catch (Exception e) {
            e.printStackTrace();
            updateLogger("Error al listar productos: " + e.getMessage());
            return new LinkedList<>();
        }
    }

    public Product buscarProductoPorId(String productoId) {
        if (!ensureConnected()) {
            return null;
        }
        try {
            return productService.buscarProducto(productoId);
        } catch (Exception e) {
            updateLogger("Error al buscar producto por ID: " + e.getMessage());
            return null;
        }
    }

    public LinkedList<Product> buscarProductoPorNombre(String nombre) {
        if (!ensureConnected()) {
            return new LinkedList<>();
        }
        try {
            return productService.buscarProductoPorNombre(nombre);
        } catch (Exception e) {
            updateLogger("Error al buscar producto: " + e.getMessage());
            return new LinkedList<>();
        }
    }

    public LinkedList<Product> buscarProducto(String query) {
        if (query == null || query.isBlank()) {
            return listarProductos();
        }
        Product exact = buscarProductoPorId(query.trim());
        LinkedList<Product> resultado = new LinkedList<>();
        if (exact != null) {
            resultado.add(exact);
            return resultado;
        }
        return buscarProductoPorNombre(query.trim());
    }

    public Order crearPedido(String cedulaCliente, boolean isPremium) {
        if (!ensureConnected()) {
            return null;
        }
        try {
            currentOrder = orderService.crearPedido(cedulaCliente, isPremium);
            updateLogger("Pedido creado para " + cedulaCliente + ".");
            return currentOrder;
        } catch (Exception e) {
            updateLogger("Error al crear pedido: " + e.getMessage());
            return null;
        }
    }

    public boolean agregarProductoAPedido(String pedidoId, Product product) {
        if (!ensureConnected()) {
            return false;
        }
        try {
            boolean added = orderService.agregarProducto(pedidoId, product);
            if (added) {
                updateLogger("Producto agregado al pedido.");
            }
            return added;
        } catch (Exception e) {
            updateLogger("Error al agregar producto al pedido: " + e.getMessage());
            return false;
        }
    }

    public boolean agregarProductoAPedido(Product product) {
        return currentOrder != null && agregarProductoAPedido(currentOrder.getOrderId(), product);
    }

    public boolean quitarProductoAPedido(String pedidoId, Product product) {
        if (!ensureConnected()) {
            return false;
        }
        try {
            return orderService.quitarProducto(pedidoId, product);
        } catch (Exception e) {
            updateLogger("Error al quitar producto del pedido: " + e.getMessage());
            return false;
        }
    }

    public boolean modificarPedido(Order order) {
        if (!ensureConnected() || order == null) {
            return false;
        }
        try {
            boolean updated = orderService.modificarPedido(order);
            if (updated) {
                currentOrder = order;
            }
            return updated;
        } catch (Exception e) {
            updateLogger("Error al modificar el pedido: " + e.getMessage());
            return false;
        }
    }

    public LinkedList<Order> getPedidosFrecuentes(String cedula) {
        if (!ensureConnected() || cedula == null || cedula.isBlank()) {
            return new LinkedList<>();
        }
        try {
            return userService.getPedidosFrecuentes(cedula);
        } catch (Exception e) {
            updateLogger("Error al obtener pedidos frecuentes: " + e.getMessage());
            return new LinkedList<>();
        }
    }

    public double calcularFactura(Order pedido) {
        if (!ensureConnected() || pedido == null) {
            return 0.0;
        }
        try {
            double total = orderService.calcularFactura(pedido);
            currentOrder = pedido;
            updateLogger("Factura calculada.");
            return total;
        } catch (Exception e) {
            updateLogger("Error al calcular factura: " + e.getMessage());
            return 0.0;
        }
    }

    public double calcularFactura() {
        return calcularFactura(currentOrder);
    }

    public boolean enviarPedidoACocina(Order pedido) {
        if (!ensureConnected() || pedido == null) {
            return false;
        }
        try {
            orderService.enviarPedidoACocina(pedido);
            boolean sent = true;
            if (sent) {
                currentOrder = pedido;
                updateLogger("Pedido enviado a cocina.");
            }
            return sent;
        } catch (Exception e) {
            updateLogger("Error al enviar pedido a cocina: " + e.getMessage());
            return false;
        }
    }

    public boolean enviarPedidoACocina() {
        return enviarPedidoACocina(currentOrder);
    }

    public boolean cancelarPedido(String pedidoId) {
        if (!ensureConnected()) {
            return false;
        }
        try {
            boolean cancelled = orderService.cancelarPedido(pedidoId);
            if (cancelled && currentOrder != null && pedidoId.equals(currentOrder.getOrderId())) {
                currentOrder = null;
            }
            updateLogger(cancelled ? "Pedido cancelado." : "No se pudo cancelar el pedido.");
            return cancelled;
        } catch (Exception e) {
            updateLogger("Error al cancelar pedido: " + e.getMessage());
            return false;
        }
    }

    public boolean cancelarPedido() {
        return currentOrder != null && cancelarPedido(currentOrder.getOrderId());
    }

    public boolean isConnected() {
        return connected;
    }

    public Rol getCurrentRole() {
        return currentRole;
    }

    public String getLogger() {
        return logger;
    }

    public User getCurrentClient() {
        return currentClient;
    }

    public void setCurrentClient(User currentClient) {
        this.currentClient = currentClient;
    }

    public Order getCurrentOrder() {
        return currentOrder;
    }

    public void setCurrentOrder(Order currentOrder) {
        this.currentOrder = currentOrder;
    }

    public void clearCurrentOrder() {
        this.currentOrder = null;
    }

    private boolean ensureConnected() {
        if (connected && userService != null && productService != null && orderService != null) {
            return true;
        }
        updateLogger("El servidor no está conectado.");
        return false;
    }

    private void updateLogger(String message) {
        this.logger = message;
        this.notifyObservers();
    }
}
