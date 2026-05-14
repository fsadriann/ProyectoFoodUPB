package edu.fsadriann.client.model.admin;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.server.model.admin.AdminInterface;
import edu.fsadriann.server.model.cuadrante.Cuadrante;
import edu.fsadriann.server.model.cuadrante.CuadranteInterface;
import edu.fsadriann.server.model.cuadrante.CuadranteRepository;
import edu.fsadriann.server.model.order.Order;
import edu.fsadriann.server.model.order.OrderInterface;
import edu.fsadriann.server.model.product.Product;
import edu.fsadriann.server.model.product.ProductInterface;
import edu.fsadriann.server.model.user.Rol;
import edu.fsadriann.server.model.user.User;
import edu.fsadriann.server.model.user.UserInterface;

public class AdminModel {

    private UserInterface      userService;
    private ProductInterface   productService;
    private OrderInterface     orderService;
    private AdminInterface     adminService;
    private CuadranteInterface cuadranteService;

    private boolean connected;

    public AdminModel() {}

    public void injectServices(UserInterface u, ProductInterface p,
                               OrderInterface o, AdminInterface a,
                               CuadranteInterface c) {
        this.userService      = u;
        this.productService   = p;
        this.orderService     = o;
        this.adminService     = a;
        this.cuadranteService = c;
        this.connected        = u != null;
    }

    public boolean isConnected() { return connected && userService != null; }

    // ── Cuadrantes ────────────────────────────────────────────────────────────

    public boolean agregarCuadrante(Cuadrante cuadrante) {
        if (!isConnected() || cuadrante == null) return false;
        try {
            return cuadranteService.agregarCuadrante(cuadrante);
        } catch (Exception e) {
            System.err.println("Error al agregar cuadrante: " + e.getMessage());
            return false;
        }
    }

    public boolean editarCuadrante(Cuadrante cuadrante) {
        if (!isConnected() || cuadrante == null) return false;
        try {
            return cuadranteService.editarCuadrante(cuadrante);
        } catch (Exception e) {
            System.err.println("Error al editar cuadrante: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminarCuadrante(String nombre) {
        if (!isConnected() || nombre == null) return false;
        try {
            return cuadranteService.eliminarCuadrante(nombre);
        } catch (Exception e) {
            System.err.println("Error al eliminar cuadrante: " + e.getMessage());
            return false;
        }
    }

    public boolean conectarCuadrantes(String nombreA, String nombreB, double distancia) {
        if (!isConnected()) return false;
        try {
            return cuadranteService.conectarCuadrantes(nombreA, nombreB, distancia);
        } catch (Exception e) {
            System.err.println("Error al conectar cuadrantes: " + e.getMessage());
            return false;
        }
    }

    public LinkedList<Cuadrante> listarCuadrantes() {
        if (!isConnected()) return new LinkedList<>();
        try {
            return cuadranteService.listarCuadrantes();
        } catch (Exception e) {
            System.err.println("Error al listar cuadrantes: " + e.getMessage());
            return new LinkedList<>();
        }
    }

    // ── Productos ─────────────────────────────────────────────────────────────

    public LinkedList<Product> listarProductos() {
        if (!isConnected()) return new LinkedList<>();
        try {
            return productService.listarProductos();
        } catch (Exception e) {
            System.err.println("Error al listar productos: " + e.getMessage());
            return new LinkedList<>();
        }
    }

    public Product buscarProductoPorId(String productoId) {
        if (!isConnected()) return null;
        try {
            return productService.buscarProducto(productoId);
        } catch (Exception e) {
            System.err.println("Error al buscar producto: " + e.getMessage());
            return null;
        }
    }

    public boolean agregarProducto(Product product) {
        if (!isConnected() || product == null) return false;
        try {
            return productService.agregarProducto(product);
        } catch (Exception e) {
            System.err.println("Error al agregar producto: " + e.getMessage());
            return false;
        }
    }

    public boolean editarProducto(Product product) {
        if (!isConnected() || product == null) return false;
        try {
            return productService.editarProducto(product);
        } catch (Exception e) {
            System.err.println("Error al editar producto: " + e.getMessage());
            return false;
        }
    }

    public boolean toggleDisponibilidadProducto(String productoId, boolean disponible) {
        if (!isConnected() || productoId == null) return false;
        try {
            return disponible
                    ? productService.activarProducto(productoId)
                    : productService.desactivarProducto(productoId);
        } catch (Exception e) {
            System.err.println("Error al actualizar disponibilidad: " + e.getMessage());
            return false;
        }
    }

    // ── Usuarios ──────────────────────────────────────────────────────────────

    public LinkedList<User> listarUsuarios() {
        if (!isConnected()) return new LinkedList<>();
        try {
            return userService.listarUsuarios();
        } catch (Exception e) {
            System.err.println("Error al listar usuarios: " + e.getMessage());
            return new LinkedList<>();
        }
    }

    public int getTotalUsuarios() {
        if (!isConnected()) return 0;
        try {
            return userService.getTotalUsuarios();
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean registrarUsuario(User user, String contrasena) {
        if (!isConnected() || user == null || contrasena == null || contrasena.isBlank()) return false;
        try {
            if (user.getRol() == Rol.CLIENTE) {
                User created = userService.registrarCliente(user);
                if (created == null) return false;
                userService.registrarCredencial(created.getCedula(), contrasena);
                return true;
            }
            boolean creado      = adminService.crearOperador(user);
            boolean rolAsignado = adminService.asignarRol(user.getCedula(), user.getRol());
            if (!creado || !rolAsignado) return false;
            try { userService.registrarCliente(user); } catch (Exception ignored) {}
            userService.registrarCredencial(user.getCedula(), contrasena);
            return true;
        } catch (Exception e) {
            System.err.println("Error al registrar usuario: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizarCliente(User user) {
        if (!isConnected() || user == null) return false;
        try {
            return userService.actualizarCliente(user);
        } catch (Exception e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminarCliente(String cedula) {
        if (!isConnected() || cedula == null) return false;
        try {
            return userService.eliminarCliente(cedula);
        } catch (Exception e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
            return false;
        }
    }

    // ── Pedidos y bitácora ────────────────────────────────────────────────────

    public LinkedList<Order> getPedidosTodos() {
        if (!isConnected()) return new LinkedList<>();
        try {
            return adminService.getPedidosTodos();
        } catch (Exception e) {
            System.err.println("Error al obtener pedidos: " + e.getMessage());
            return new LinkedList<>();
        }
    }

    public LinkedList<String> verBitacora() {
        if (!isConnected()) return new LinkedList<>();
        try {
            return adminService.verBitacoraAuditoria();
        } catch (Exception e) {
            System.err.println("Error al obtener bitácora: " + e.getMessage());
            return new LinkedList<>();
        }
    }
}