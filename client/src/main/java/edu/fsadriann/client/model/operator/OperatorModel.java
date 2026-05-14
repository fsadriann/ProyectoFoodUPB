package edu.fsadriann.client.model.operator;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.model.iterator.Iterator;
import edu.fsadriann.server.model.cuadrante.Cuadrante;
import edu.fsadriann.server.model.cuadrante.CuadranteInterface;
import edu.fsadriann.server.model.order.Order;
import edu.fsadriann.server.model.order.OrderInterface;
import edu.fsadriann.server.model.product.Product;
import edu.fsadriann.server.model.product.ProductInterface;
import edu.fsadriann.server.model.user.User;
import edu.fsadriann.server.model.user.UserInterface;

public class OperatorModel {

    private UserInterface      userService;
    private ProductInterface   productService;
    private OrderInterface     orderService;
    private CuadranteInterface cuadranteService;

    private boolean connected;
    private String  logger;
    private User    currentClient;
    private Order   currentOrder;

    public OperatorModel() {}

    public void injectServices(UserInterface u, ProductInterface p,
                               OrderInterface o, CuadranteInterface c) {
        this.userService      = u;
        this.productService   = p;
        this.orderService     = o;
        this.cuadranteService = c;
        this.connected        = u != null;
    }

    public boolean isConnected() { return connected && userService != null; }
    public String  getLogger()   { return logger; }

    // ── Estado de sesión ──────────────────────────────────────────────────────

    public User  getCurrentClient() { return currentClient; }
    public Order getCurrentOrder()  { return currentOrder; }

    public void setCurrentClient(User u)  { this.currentClient = u; }
    public void setCurrentOrder(Order o)  { this.currentOrder  = o; }
    public void clearCurrentOrder()       { this.currentOrder  = null; }

    public void logout() {
        this.currentClient = null;
        this.currentOrder  = null;
    }

    // ── Usuarios ──────────────────────────────────────────────────────────────

    public User buscarClientePorTelefono(String telefono) {
        if (!isConnected()) return null;
        try {
            User user = userService.buscarClientePorTelefono(telefono);
            currentClient = user;
            logger = user != null
                    ? "Cliente encontrado: " + user.getNombreCompleto().trim()
                    : "No se encontró cliente con ese teléfono.";
            return user;
        } catch (Exception e) {
            logger = "Error al buscar cliente: " + e.getMessage();
            return null;
        }
    }

    public User registrarCliente(User user) {
        if (!isConnected()) return null;
        try {
            User created = userService.registrarCliente(user);
            logger = "Cliente registrado correctamente.";
            return created;
        } catch (Exception e) {
            logger = "Error al registrar cliente: " + e.getMessage();
            return null;
        }
    }

    public LinkedList<Order> getPedidosFrecuentes(String cedula) {
        if (!isConnected() || cedula == null || cedula.isBlank()) return new LinkedList<>();
        try {
            return userService.getPedidosFrecuentes(cedula);
        } catch (Exception e) {
            logger = "Error al obtener pedidos frecuentes: " + e.getMessage();
            return new LinkedList<>();
        }
    }

    // ── Productos ─────────────────────────────────────────────────────────────

    public LinkedList<Product> listarProductos() {
        if (!isConnected()) return new LinkedList<>();
        try {
            LinkedList<Product> products = productService.listarProductos();
            logger = "Catálogo cargado.";
            return products;
        } catch (Exception e) {
            logger = "Error al listar productos: " + e.getMessage();
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

    private Product buscarProductoPorId(String productoId) {
        if (!isConnected()) return null;
        try {
            return productService.buscarProducto(productoId);
        } catch (Exception e) {
            logger = "Error al buscar producto por ID: " + e.getMessage();
            return null;
        }
    }

    private LinkedList<Product> buscarProductoPorNombre(String nombre) {
        if (!isConnected()) return new LinkedList<>();
        try {
            return productService.buscarProductoPorNombre(nombre);
        } catch (Exception e) {
            logger = "Error al buscar producto: " + e.getMessage();
            return new LinkedList<>();
        }
    }

    // ── Pedidos ───────────────────────────────────────────────────────────────

    public Order crearPedido(String cedulaCliente, boolean isPremium) {
        if (!isConnected()) return null;
        try {
            currentOrder = orderService.crearPedido(cedulaCliente, isPremium);
            if (currentOrder != null && currentClient != null) {
                String cuadrante = currentClient.getCuadrante();
                if (cuadrante != null && !cuadrante.isBlank()) {
                    orderService.asignarCuadranteDestino(currentOrder.getOrderId(), cuadrante);
                    logger = "Pedido creado — cuadrante asignado: " + cuadrante + ".";
                } else {
                    logger = "Pedido creado — sin cuadrante (asignar manualmente).";
                }
            } else {
                logger = "Pedido creado para " + cedulaCliente + ".";
            }
            return currentOrder;
        } catch (Exception e) {
            logger = "Error al crear pedido: " + e.getMessage();
            return null;
        }
    }

    public boolean agregarProductoAPedido(String pedidoId, Product product) {
        if (!isConnected()) return false;
        try {
            boolean added = orderService.agregarProducto(pedidoId, product);
            if (added) logger = "Producto agregado al pedido.";
            return added;
        } catch (Exception e) {
            logger = "Error al agregar producto al pedido: " + e.getMessage();
            return false;
        }
    }

    public boolean agregarProductoAPedido(Product product) {
        return currentOrder != null && agregarProductoAPedido(currentOrder.getOrderId(), product);
    }

    public boolean quitarProductoAPedido(String pedidoId, Product product) {
        if (!isConnected()) return false;
        try {
            return orderService.quitarProducto(pedidoId, product);
        } catch (Exception e) {
            logger = "Error al quitar producto del pedido: " + e.getMessage();
            return false;
        }
    }

    public boolean cambiarCantidadProducto(String pedidoId, String productoId, int cantidad) {
        if (!isConnected()) return false;
        try {
            return orderService.cambiarCantidadProducto(pedidoId, productoId, cantidad);
        } catch (Exception e) {
            logger = "Error al cambiar cantidad: " + e.getMessage();
            return false;
        }
    }

    public double calcularFactura(Order pedido) {
        if (!isConnected() || pedido == null) return 0.0;
        try {
            double total = orderService.calcularFactura(pedido);
            currentOrder = pedido;
            logger = "Factura calculada.";
            return total;
        } catch (Exception e) {
            logger = "Error al calcular factura: " + e.getMessage();
            return 0.0;
        }
    }

    public double calcularFactura() { return calcularFactura(currentOrder); }

    public boolean asignarCuadranteDestino(String orderId, String cuadrante) {
        if (!isConnected() || orderId == null || cuadrante == null) return false;
        try {
            boolean ok = orderService.asignarCuadranteDestino(orderId, cuadrante);
            logger = ok ? "Cuadrante asignado al pedido." : "No se pudo asignar el cuadrante.";
            return ok;
        } catch (Exception e) {
            logger = "Error al asignar cuadrante: " + e.getMessage();
            return false;
        }
    }

    public boolean enviarPedidoACocina(Order pedido) {
        if (!isConnected() || pedido == null) return false;
        try {
            orderService.enviarPedidoACocina(pedido);
            currentOrder = pedido;
            logger = "Pedido enviado a cocina.";
            return true;
        } catch (Exception e) {
            logger = "Error al enviar pedido a cocina: " + e.getMessage();
            return false;
        }
    }

    public boolean enviarPedidoACocina() { return enviarPedidoACocina(currentOrder); }

    public boolean cancelarPedido(String pedidoId) {
        if (!isConnected()) return false;
        try {
            boolean cancelled = orderService.cancelarPedido(pedidoId);
            if (cancelled && currentOrder != null
                    && pedidoId.equals(currentOrder.getOrderId())) currentOrder = null;
            logger = cancelled ? "Pedido cancelado." : "No se pudo cancelar el pedido.";
            return cancelled;
        } catch (Exception e) {
            logger = "Error al cancelar pedido: " + e.getMessage();
            return false;
        }
    }

    public boolean cancelarPedido() {
        return currentOrder != null && cancelarPedido(currentOrder.getOrderId());
    }

    // ── Cuadrantes ────────────────────────────────────────────────────────────

    public LinkedList<Cuadrante> listarCuadrantes() {
        if (!isConnected()) return new LinkedList<>();
        try {
            return cuadranteService.listarCuadrantes();
        } catch (Exception e) {
            logger = "Error al listar cuadrantes: " + e.getMessage();
            return new LinkedList<>();
        }
    }

    public double calcularDistanciaCuadrantes(String origen, String destino) {
        if (!isConnected()) return -1;
        try {
            return cuadranteService.calcularDistancia(origen, destino);
        } catch (Exception e) {
            logger = "Error al calcular distancia: " + e.getMessage();
            return -1;
        }
    }
}