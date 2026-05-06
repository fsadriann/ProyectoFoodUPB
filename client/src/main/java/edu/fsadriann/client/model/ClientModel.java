package edu.fsadriann.client.model;

import edu.fsadriann.server.model.admin.AdminInterface;
import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.server.model.cuadrante.CuadranteInterface;
import edu.fsadriann.server.model.cuadrante.CuadranteRepository;
import edu.fsadriann.server.model.delivery.Delivery;
import edu.fsadriann.server.model.delivery.DeliveryInterface;
import edu.fsadriann.server.model.order.KitchenInterface;
import edu.fsadriann.server.model.order.OrderInterface;
import edu.fsadriann.server.model.product.ProductInterface;
import edu.fsadriann.server.model.observer.Subject;
import edu.fsadriann.server.model.order.Order;
import edu.fsadriann.server.model.product.Product;
import edu.fsadriann.server.model.user.Rol;
import edu.fsadriann.server.model.user.UserInterface;
import edu.fsadriann.server.model.user.User;
import edu.fsadriann.server.model.cuadrante.Cuadrante;

import java.rmi.Naming;

public class ClientModel extends Subject {

    private final String baseUri;
    private final String usersUri;
    private final String productsUri;
    private final String ordersUri;
    private final String kitchenUri;
    private final String adminUri;
    private final String cuadrantesUri;

    private DeliveryInterface  deliveryService;
    private UserInterface      userService;
    private ProductInterface   productService;
    private OrderInterface     orderService;
    private KitchenInterface   kitchenService;
    private AdminInterface     adminService;
    private CuadranteInterface cuadranteService;

    private boolean connected;
    private String  logger;
    private Rol     currentRole;
    private User    currentClient;
    private Order   currentOrder;

    public ClientModel(String ip, int port, String serviceName) {
        this.baseUri       = "rmi://" + ip + ":" + port + "/" + serviceName;
        this.usersUri      = baseUri + "-users";
        this.productsUri   = baseUri + "-products";
        this.ordersUri     = baseUri + "-orders";
        this.kitchenUri    = baseUri + "-kitchen";
        this.adminUri      = baseUri + "-admin";
        this.cuadrantesUri = baseUri + "-cuadrantes";
        this.connected     = false;
    }

    // ── Conexión ──────────────────────────────────────────────────────────────

    public boolean connect() {
        try {
            this.cuadranteService = (CuadranteInterface) Naming.lookup(cuadrantesUri);
            this.userService      = (UserInterface)      Naming.lookup(usersUri);
            this.productService   = (ProductInterface)   Naming.lookup(productsUri);
            this.orderService     = (OrderInterface)     Naming.lookup(ordersUri);
            this.kitchenService   = (KitchenInterface)   Naming.lookup(kitchenUri);
            this.adminService     = (AdminInterface)     Naming.lookup(adminUri);
            this.deliveryService  = (DeliveryInterface)  Naming.lookup(baseUri + "-delivery");
            this.connected = true;
            updateLogger("Conectado al servidor en " + baseUri);
            return true;
        } catch (Exception e) {
            this.cuadranteService = null;
            this.userService      = null;
            this.productService   = null;
            this.orderService     = null;
            this.kitchenService   = null;
            this.deliveryService  = null;
            this.adminService     = null;
            this.connected = false;
            updateLogger("No se pudo conectar al servidor en " + baseUri + ": " + e.getMessage());
            System.err.println(this.logger);
            return false;
        }
    }

    // ── Autenticación ─────────────────────────────────────────────────────────

    public Rol login(String correo, String contrasena) {
        if (!ensureConnected()) return null;
        try {
            this.currentRole = userService.login(correo, contrasena);
            updateLogger(currentRole != null ? "Inicio de sesión exitoso." : "Credenciales inválidas.");
            return this.currentRole;
        } catch (Exception e) {
            updateLogger("Error al iniciar sesión: " + e.getMessage());
            return null;
        }
    }

    public void logout() {
        currentRole   = null;
        currentClient = null;
        currentOrder  = null;
    }

    // ── Usuarios ──────────────────────────────────────────────────────────────

    public User buscarClientePorTelefono(String telefono) {
        if (!ensureConnected()) return null;
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
        if (!ensureConnected()) return null;
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
        if (!ensureConnected()) return new LinkedList<>();
        try {
            return userService.listarUsuarios();
        } catch (Exception e) {
            updateLogger("Error al listar usuarios: " + e.getMessage());
            return new LinkedList<>();
        }
    }

    public boolean registrarUsuario(User user, String contrasena) {
        if (!ensureConnected() || user == null || contrasena == null || contrasena.isBlank()) return false;
        try {
            if (user.getRol() == Rol.CLIENTE) {
                User created = userService.registrarCliente(user);
                if (created == null) { updateLogger("No se pudo registrar el usuario."); return false; }
                userService.registrarCredencial(created.getCedula(), contrasena);
                updateLogger("Usuario registrado correctamente.");
                return true;
            }
            boolean creado      = adminService.crearOperador(user);
            boolean rolAsignado = adminService.asignarRol(user.getCedula(), user.getRol());
            if (!creado || !rolAsignado) { updateLogger("No se pudo registrar el usuario operativo."); return false; }
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
        if (!ensureConnected() || adminService == null || user == null || rol == null) return false;
        try {
            boolean creado = adminService.crearOperador(user);
            if (!creado) { updateLogger("No se pudo crear el usuario operativo."); return false; }
            boolean rolAsignado = adminService.asignarRol(user.getCedula(), rol);
            updateLogger(rolAsignado ? "Usuario operativo registrado." : "No se pudo asignar el rol.");
            return rolAsignado;
        } catch (Exception e) {
            updateLogger("Error al registrar usuario operativo: " + e.getMessage());
            return false;
        }
    }

    public int getTotalUsuarios() {
        if (!ensureConnected()) return 0;
        try {
            return userService.getTotalUsuarios();
        } catch (Exception e) {
            updateLogger("Error al obtener el total de usuarios: " + e.getMessage());
            return 0;
        }
    }

    public boolean actualizarCliente(User user) {
        if (!ensureConnected() || user == null) return false;
        try {
            boolean ok = userService.actualizarCliente(user);
            updateLogger(ok ? "Usuario actualizado." : "No se pudo actualizar el usuario.");
            return ok;
        } catch (Exception e) {
            updateLogger("Error al actualizar usuario: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminarCliente(String cedula) {
        if (!ensureConnected() || cedula == null) return false;
        try {
            boolean ok = userService.eliminarCliente(cedula);
            updateLogger(ok ? "Usuario eliminado." : "No se pudo eliminar el usuario.");
            return ok;
        } catch (Exception e) {
            updateLogger("Error al eliminar usuario: " + e.getMessage());
            return false;
        }
    }

    // ── Productos ─────────────────────────────────────────────────────────────

    public LinkedList<Product> listarProductos() {
        if (!ensureConnected()) return new LinkedList<>();
        try {
            LinkedList<Product> products = productService.listarProductos();
            updateLogger("Catálogo cargado.");
            return products;
        } catch (Exception e) {
            updateLogger("Error al listar productos: " + e.getMessage());
            return new LinkedList<>();
        }
    }

    public Product buscarProductoPorId(String productoId) {
        if (!ensureConnected()) return null;
        try {
            return productService.buscarProducto(productoId);
        } catch (Exception e) {
            updateLogger("Error al buscar producto por ID: " + e.getMessage());
            return null;
        }
    }

    public LinkedList<Product> buscarProductoPorNombre(String nombre) {
        if (!ensureConnected()) return new LinkedList<>();
        try {
            return productService.buscarProductoPorNombre(nombre);
        } catch (Exception e) {
            updateLogger("Error al buscar producto: " + e.getMessage());
            return new LinkedList<>();
        }
    }

    public LinkedList<Product> buscarProducto(String query) {
        if (query == null || query.isBlank()) return listarProductos();
        Product exact = buscarProductoPorId(query.trim());
        LinkedList<Product> resultado = new LinkedList<>();
        if (exact != null) { resultado.add(exact); return resultado; }
        return buscarProductoPorNombre(query.trim());
    }

    public boolean agregarProducto(Product product) {
        if (!ensureConnected() || product == null) return false;
        try {
            boolean ok = productService.agregarProducto(product);
            updateLogger(ok ? "Producto agregado." : "No se pudo agregar el producto.");
            return ok;
        } catch (Exception e) {
            updateLogger("Error al agregar producto: " + e.getMessage());
            return false;
        }
    }

    public boolean editarProducto(Product product) {
        if (!ensureConnected() || product == null) return false;
        try {
            boolean ok = productService.editarProducto(product);
            updateLogger(ok ? "Producto editado." : "No se pudo editar el producto.");
            return ok;
        } catch (Exception e) {
            updateLogger("Error al editar producto: " + e.getMessage());
            return false;
        }
    }

    public boolean toggleDisponibilidadProducto(String productoId, boolean disponible) {
        if (!ensureConnected() || productoId == null) return false;
        try {
            boolean ok = disponible
                    ? productService.activarProducto(productoId)
                    : productService.desactivarProducto(productoId);
            updateLogger(ok ? "Disponibilidad actualizada." : "No se pudo actualizar disponibilidad.");
            return ok;
        } catch (Exception e) {
            updateLogger("Error al actualizar disponibilidad: " + e.getMessage());
            return false;
        }
    }

    // ── Pedidos ───────────────────────────────────────────────────────────────

    /**
     * Crea un nuevo pedido y, si el cliente actual ya tiene un cuadrante registrado
     * en su perfil, lo asigna automáticamente al pedido para que el costo de
     * domicilio se calcule correctamente desde el primer momento.
     */
    public Order crearPedido(String cedulaCliente, boolean isPremium) {
        if (!ensureConnected()) return null;
        try {
            currentOrder = orderService.crearPedido(cedulaCliente, isPremium);

            // Auto-asignar cuadrante si el cliente ya lo tiene en su perfil
            if (currentOrder != null && currentClient != null) {
                String cuadrante = currentClient.getCuadrante();
                if (cuadrante != null && !cuadrante.isBlank()) {
                    orderService.asignarCuadranteDestino(currentOrder.getOrderId(), cuadrante);
                    updateLogger("Pedido creado para " + cedulaCliente
                            + " — cuadrante asignado: " + cuadrante + ".");
                } else {
                    updateLogger("Pedido creado para " + cedulaCliente
                            + " — sin cuadrante (asignar manualmente).");
                }
            } else {
                updateLogger("Pedido creado para " + cedulaCliente + ".");
            }
            return currentOrder;
        } catch (Exception e) {
            updateLogger("Error al crear pedido: " + e.getMessage());
            return null;
        }
    }

    public boolean agregarProductoAPedido(String pedidoId, Product product) {
        if (!ensureConnected()) return false;
        try {
            boolean added = orderService.agregarProducto(pedidoId, product);
            if (added) updateLogger("Producto agregado al pedido.");
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
        if (!ensureConnected()) return false;
        try {
            return orderService.quitarProducto(pedidoId, product);
        } catch (Exception e) {
            updateLogger("Error al quitar producto del pedido: " + e.getMessage());
            return false;
        }
    }

    public boolean marcarEnCamino(String orderId) {
        if (!ensureConnected() || orderId == null) return false;
        try {
            boolean ok = orderService.marcarEnCamino(orderId);
            updateLogger(ok ? "Pedido en camino." : "No se pudo marcar en camino.");
            return ok;
        } catch (Exception e) {
            updateLogger("Error al marcar en camino: " + e.getMessage());
            return false;
        }
    }

    public boolean modificarPedido(Order order) {
        if (!ensureConnected() || order == null) return false;
        try {
            boolean updated = orderService.modificarPedido(order);
            if (updated) currentOrder = order;
            return updated;
        } catch (Exception e) {
            updateLogger("Error al modificar el pedido: " + e.getMessage());
            return false;
        }
    }

    public java.util.List<Order> procesarPedidosDisponibles() {
        if (!ensureConnected()) return new java.util.ArrayList<>();
        try {
            return kitchenService.procesarPedidosDisponibles();
        } catch (Exception e) {
            updateLogger("Error al procesar pedidos disponibles: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    public double calcularFactura(Order pedido) {
        if (!ensureConnected() || pedido == null) return 0.0;
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

    public double calcularFactura() { return calcularFactura(currentOrder); }

    public boolean enviarPedidoACocina(Order pedido) {
        if (!ensureConnected() || pedido == null) return false;
        try {
            orderService.enviarPedidoACocina(pedido);
            currentOrder = pedido;
            updateLogger("Pedido enviado a cocina.");
            return true;
        } catch (Exception e) {
            updateLogger("Error al enviar pedido a cocina: " + e.getMessage());
            return false;
        }
    }

    public boolean enviarPedidoACocina() { return enviarPedidoACocina(currentOrder); }

    public boolean cancelarPedido(String pedidoId) {
        if (!ensureConnected()) return false;
        try {
            boolean cancelled = orderService.cancelarPedido(pedidoId);
            if (cancelled && currentOrder != null
                    && pedidoId.equals(currentOrder.getOrderId())) currentOrder = null;
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

    public LinkedList<Order> getPedidosFrecuentes(String cedula) {
        if (!ensureConnected() || cedula == null || cedula.isBlank()) return new LinkedList<>();
        try {
            return userService.getPedidosFrecuentes(cedula);
        } catch (Exception e) {
            updateLogger("Error al obtener pedidos frecuentes: " + e.getMessage());
            return new LinkedList<>();
        }
    }

    public LinkedList<Order> getPedidosTodos() {
        if (!ensureConnected()) return new LinkedList<>();
        try {
            return adminService.getPedidosTodos();
        } catch (Exception e) {
            updateLogger("Error al obtener pedidos: " + e.getMessage());
            return new LinkedList<>();
        }
    }

    public java.util.List<Order> getPedidosEnPreparacion() {
        if (!ensureConnected()) return new java.util.ArrayList<>();
        try {
            LinkedList<Order> lista = orderService.getPedidosEnPreparacion();
            java.util.List<Order> resultado = new java.util.ArrayList<>();
            if (lista == null) return resultado;
            edu.fsadriann.model.iterator.Iterator<Order> it = lista.iterator();
            while (it.hasNext()) { Order o = it.next(); if (o != null) resultado.add(o); }
            return resultado;
        } catch (Exception e) {
            updateLogger("Error al obtener pedidos en preparación: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    // ── Cocina ────────────────────────────────────────────────────────────────

    public void encolarPedido(Order pedido) {
        if (!ensureConnected()) return;
        try { kitchenService.encolarPedido(pedido); }
        catch (Exception e) { updateLogger("Error al encolar pedido: " + e.getMessage()); }
    }

    public Order procesarSiguientePedido() {
        if (!ensureConnected()) return null;
        try { return kitchenService.procesarSiguientePedido(); }
        catch (Exception e) { updateLogger("Error al procesar siguiente pedido: " + e.getMessage()); return null; }
    }

    public boolean marcarPedidoListo(String pedidoId) {
        if (!ensureConnected()) return false;
        try { return kitchenService.marcarPedidoListo(pedidoId); }
        catch (Exception e) { updateLogger("Error al marcar pedido listo: " + e.getMessage()); return false; }
    }

    public int     tamanoCola() { if (!ensureConnected()) return 0;   try { return kitchenService.tamanoCola(); } catch (Exception e) { return 0; } }
    public boolean colaVacia()  { if (!ensureConnected()) return true; try { return kitchenService.colaVacia(); } catch (Exception e) { return true; } }

    // ── Cuadrantes ────────────────────────────────────────────────────────────

    public boolean agregarCuadrante(Cuadrante cuadrante) {
        if (!ensureConnected() || cuadrante == null) return false;
        try {
            boolean ok = cuadranteService.agregarCuadrante(cuadrante);
            updateLogger(ok ? "Cuadrante agregado." : "No se pudo agregar el cuadrante.");
            return ok;
        } catch (Exception e) {
            updateLogger("Error al agregar cuadrante: " + e.getMessage());
            return false;
        }
    }

    public boolean editarCuadrante(Cuadrante cuadrante) {
        if (!ensureConnected() || cuadrante == null) return false;
        try {
            boolean ok = cuadranteService.editarCuadrante(cuadrante);
            updateLogger(ok ? "Cuadrante editado." : "No se pudo editar el cuadrante.");
            return ok;
        } catch (Exception e) {
            updateLogger("Error al editar cuadrante: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminarCuadrante(String nombre) {
        if (!ensureConnected() || nombre == null) return false;
        try {
            boolean ok = cuadranteService.eliminarCuadrante(nombre);
            updateLogger(ok ? "Cuadrante eliminado." : "No se pudo eliminar el cuadrante.");
            return ok;
        } catch (Exception e) {
            updateLogger("Error al eliminar cuadrante: " + e.getMessage());
            return false;
        }
    }

    public boolean conectarCuadrantes(String nombreA, String nombreB, double distancia) {
        if (!ensureConnected()) return false;
        try {
            boolean ok = cuadranteService.conectarCuadrantes(nombreA, nombreB, distancia);
            updateLogger(ok ? "Cuadrantes conectados." : "No se pudieron conectar.");
            return ok;
        } catch (Exception e) {
            updateLogger("Error al conectar cuadrantes: " + e.getMessage());
            return false;
        }
    }

    public LinkedList<Cuadrante> listarCuadrantes() {
        if (!ensureConnected()) return new LinkedList<>();
        try {
            return cuadranteService.listarCuadrantes();
        } catch (Exception e) {
            updateLogger("Error al listar cuadrantes: " + e.getMessage());
            return new LinkedList<>();
        }
    }

    public LinkedList<CuadranteRepository.ConexionEntry> listarConexiones() {
        if (!ensureConnected()) return new LinkedList<>();
        try {
            return cuadranteService.listarConexiones();
        } catch (Exception e) {
            updateLogger("Error al listar conexiones: " + e.getMessage());
            return new LinkedList<>();
        }
    }

    public LinkedList<String> calcularRutaMasCorta(String origen, String destino) {
        if (!ensureConnected()) return new LinkedList<>();
        try {
            return cuadranteService.calcularRutaMasCorta(origen, destino);
        } catch (Exception e) {
            updateLogger("Error al calcular ruta: " + e.getMessage());
            return new LinkedList<>();
        }
    }

    public double calcularDistanciaCuadrantes(String origen, String destino) {
        if (!ensureConnected()) return -1;
        try {
            return cuadranteService.calcularDistancia(origen, destino);
        } catch (Exception e) {
            updateLogger("Error al calcular distancia: " + e.getMessage());
            return -1;
        }
    }

    public boolean cambiarCantidadProducto(String pedidoId, String productoId, int cantidad) {
        if (!ensureConnected()) return false;
        try {
            return orderService.cambiarCantidadProducto(pedidoId, productoId, cantidad);
        } catch (Exception e) {
            updateLogger("Error al cambiar cantidad: " + e.getMessage());
            return false;
        }
    }

    public boolean asignarCuadranteDestino(String orderId, String cuadrante) {
        if (!ensureConnected() || orderId == null || cuadrante == null) return false;
        try {
            boolean ok = orderService.asignarCuadranteDestino(orderId, cuadrante);
            updateLogger(ok ? "Cuadrante asignado al pedido." : "No se pudo asignar el cuadrante.");
            return ok;
        } catch (Exception e) {
            updateLogger("Error al asignar cuadrante: " + e.getMessage());
            return false;
        }
    }

    // ── Delivery ──────────────────────────────────────────────────────────────

    public Delivery asignarPedidoARepartidor(String orderId, String repartidorId) {
        if (!ensureConnected()) return null;
        try {
            return deliveryService.asignarPedidoARepartidor(orderId, repartidorId);
        } catch (Exception e) {
            updateLogger("Error al asignar repartidor: " + e.getMessage());
            return null;
        }
    }

    public boolean iniciarEntrega(String orderId) {
        if (!ensureConnected()) return false;
        try {
            boolean ok = deliveryService.iniciarEntrega(orderId);
            updateLogger(ok ? "Entrega iniciada." : "No se pudo iniciar entrega.");
            return ok;
        } catch (Exception e) {
            updateLogger("Error al iniciar entrega: " + e.getMessage());
            return false;
        }
    }

    public boolean completarEntrega(String orderId) {
        if (!ensureConnected()) return false;
        try {
            boolean ok = deliveryService.completarEntrega(orderId);
            updateLogger(ok ? "Entrega completada." : "No se pudo completar entrega.");
            return ok;
        } catch (Exception e) {
            updateLogger("Error al completar entrega: " + e.getMessage());
            return false;
        }
    }

    public Delivery buscarEntregaPorPedido(String orderId) {
        if (!ensureConnected()) return null;
        try {
            return deliveryService.buscarEntregaPorPedido(orderId);
        } catch (Exception e) {
            updateLogger("Error al buscar entrega: " + e.getMessage());
            return null;
        }
    }

    public boolean yaEstaAsignado(String orderId) {
        if (!ensureConnected()) return false;
        try {
            return deliveryService.yaEstaAsignado(orderId);
        } catch (Exception e) {
            return false;
        }
    }

    // ── Admin ─────────────────────────────────────────────────────────────────

    public LinkedList<String> verBitacora() {
        if (!ensureConnected()) return new LinkedList<>();
        try {
            return adminService.verBitacoraAuditoria();
        } catch (Exception e) {
            updateLogger("Error al obtener bitácora: " + e.getMessage());
            return new LinkedList<>();
        }
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public boolean isConnected()          { return connected; }
    public Rol     getCurrentRole()       { return currentRole; }
    public String  getLogger()            { return logger; }
    public User    getCurrentClient()     { return currentClient; }
    public Order   getCurrentOrder()      { return currentOrder; }

    public void setCurrentClient(User u)  { this.currentClient = u; }
    public void setCurrentOrder(Order o)  { this.currentOrder  = o; }
    public void clearCurrentOrder()       { this.currentOrder  = null; }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean ensureConnected() {
        return connected && userService != null && productService != null
                && orderService != null && deliveryService != null;
    }

    private void updateLogger(String message) {
        this.logger = message;
        this.notifyObservers();
    }
}