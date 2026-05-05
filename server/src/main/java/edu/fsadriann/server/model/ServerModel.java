package edu.fsadriann.server.model;

import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import edu.fsadriann.server.model.admin.AdminService;
import edu.fsadriann.server.model.cuadrante.CuadranteService;
import edu.fsadriann.server.model.delivery.DeliveryService;
import edu.fsadriann.server.model.order.KitchenService;
import edu.fsadriann.server.model.order.OrderService;
import edu.fsadriann.server.model.product.ProductService;
import edu.fsadriann.server.model.user.UserService;

public class ServerModel {

    private String ip;
    private int port;
    private String serviceName;
    private String uri;
    private Registry registry;
    private boolean createdRegistry = false;
    private boolean running = false;

    private CuadranteService cuadranteService;
    private DeliveryService deliveryService;
    private UserService userService;
    private ProductService productService;
    private OrderService orderService;
    private KitchenService kitchenService;
    private AdminService adminService;

    public ServerModel(String ip, int port, String serviceName) {
        this.ip = ip;
        this.port = port;
        this.serviceName = serviceName;
        this.uri = "//" + ip + ":" + port + "/" + serviceName;
    }

    public boolean deploy() {
        try {
            System.setProperty("java.rmi.server.hostname", ip);

            cuadranteService = new CuadranteService();
            deliveryService  = new DeliveryService(cuadranteService);
            orderService = new OrderService();
            userService = new UserService(orderService);
            productService = new ProductService();
            kitchenService = new KitchenService(orderService);
            adminService = new AdminService(userService, productService, orderService);

            try {
                registry = LocateRegistry.getRegistry(port);
                // Intentar listar para comprobar conexión
                registry.list();
                System.out.println("Servidor RMI ya activo, reutilizando instancia...");
                createdRegistry = false;
            } catch (Exception ex) {
                // No hay registry accesible -> crear uno nuevo
                registry = LocateRegistry.createRegistry(port);
                createdRegistry = true;
                System.out.println("Servidor RMI iniciado correctamente en " + ip + ":" + port);
            }

            bindService("-cuadrantes", cuadranteService);
            bindService("-users", userService);
            bindService("-products", productService);
            bindService("-orders", orderService);
            bindService("-kitchen", kitchenService);
            bindService("-admin", adminService);

            running = true;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean stop() {
        try {
            Naming.unbind(uri + "-cuadrantes");
            Naming.unbind(uri + "-users");
            Naming.unbind(uri + "-products");
            Naming.unbind(uri + "-orders");
            Naming.unbind(uri + "-kitchen");
            Naming.unbind(uri + "-admin");

            if (cuadranteService != null) UnicastRemoteObject.unexportObject(cuadranteService, true);
            if (userService != null) UnicastRemoteObject.unexportObject(userService, true);
            if (productService != null) UnicastRemoteObject.unexportObject(productService, true);
            if (orderService != null) UnicastRemoteObject.unexportObject(orderService, true);
            if (kitchenService != null) UnicastRemoteObject.unexportObject(kitchenService, true);
            if (adminService != null) UnicastRemoteObject.unexportObject(adminService, true);

            // Solo unexport si el registry fue creado por este proceso
            if (createdRegistry && registry != null) {
                try {
                    UnicastRemoteObject.unexportObject(registry, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            running = false;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void bindService(String suffix, Remote service) throws Exception {
        Naming.rebind(uri + suffix, UnicastRemoteObject.exportObject(service, 0));
    }

    public boolean isRunning() {
        return running;
    }
}