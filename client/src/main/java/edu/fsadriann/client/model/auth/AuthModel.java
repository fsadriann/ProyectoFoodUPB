package edu.fsadriann.client.model.auth;

import edu.fsadriann.server.model.admin.AdminInterface;
import edu.fsadriann.server.model.cuadrante.CuadranteInterface;
import edu.fsadriann.server.model.delivery.DeliveryInterface;
import edu.fsadriann.server.model.order.KitchenInterface;
import edu.fsadriann.server.model.order.OrderInterface;
import edu.fsadriann.server.model.product.ProductInterface;
import edu.fsadriann.server.model.user.Rol;
import edu.fsadriann.server.model.user.UserInterface;

import java.rmi.Naming;

public class AuthModel {

    private final String baseUri;

    private UserInterface      userService;
    private ProductInterface   productService;
    private OrderInterface     orderService;
    private KitchenInterface   kitchenService;
    private AdminInterface     adminService;
    private DeliveryInterface  deliveryService;
    private CuadranteInterface cuadranteService;

    private boolean connected;
    private Rol     currentRole;

    public AuthModel(String ip, int port, String serviceName) {
        this.baseUri = "rmi://" + ip + ":" + port + "/" + serviceName;
    }

    public boolean connect() {
        try {
            this.userService      = (UserInterface)      Naming.lookup(baseUri + "-users");
            this.productService   = (ProductInterface)   Naming.lookup(baseUri + "-products");
            this.orderService     = (OrderInterface)     Naming.lookup(baseUri + "-orders");
            this.kitchenService   = (KitchenInterface)   Naming.lookup(baseUri + "-kitchen");
            this.adminService     = (AdminInterface)     Naming.lookup(baseUri + "-admin");
            this.deliveryService  = (DeliveryInterface)  Naming.lookup(baseUri + "-delivery");
            this.cuadranteService = (CuadranteInterface) Naming.lookup(baseUri + "-cuadrantes");
            this.connected = true;
        } catch (Exception e) {
            this.connected = false;
            System.err.println("Error al conectar: " + e.getMessage());
        }
        return connected;
    }

    public Rol login(String correo, String contrasena) {
        try {
            this.currentRole = userService.login(correo, contrasena);
            return this.currentRole;
        } catch (Exception e) {
            System.err.println("Error al iniciar sesion: " + e.getMessage());
            return null;
        }
    }

    public void logout() {
        this.currentRole = null;
    }

    public boolean isConnected()    { return connected; }
    public Rol     getCurrentRole() { return currentRole; }

    // ── Getters de servicios para que AuthClientController los distribuya ──
    public UserInterface      getUserService()      { return userService; }
    public ProductInterface   getProductService()   { return productService; }
    public OrderInterface     getOrderService()     { return orderService; }
    public KitchenInterface   getKitchenService()   { return kitchenService; }
    public AdminInterface     getAdminService()     { return adminService; }
    public DeliveryInterface  getDeliveryService()  { return deliveryService; }
    public CuadranteInterface getCuadranteService() { return cuadranteService; }
}