package edu.fsadriann.client.model.delivery;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.server.model.cuadrante.Cuadrante;
import edu.fsadriann.server.model.cuadrante.CuadranteInterface;
import edu.fsadriann.server.model.cuadrante.CuadranteRepository;
import edu.fsadriann.server.model.delivery.Delivery;
import edu.fsadriann.server.model.delivery.DeliveryInterface;
import edu.fsadriann.server.model.order.Order;
import edu.fsadriann.server.model.order.OrderInterface;
import edu.fsadriann.server.model.admin.AdminInterface;

public class DeliveryModel {

    private OrderInterface     orderService;
    private DeliveryInterface  deliveryService;
    private CuadranteInterface cuadranteService;
    private AdminInterface adminService;

    public DeliveryModel() {}

    public void injectServices(OrderInterface o, DeliveryInterface d,
                               CuadranteInterface c, AdminInterface a) {
        this.orderService     = o;
        this.deliveryService  = d;
        this.cuadranteService = c;
        this.adminService     = a;
    }

    public boolean isConnected() {
        return orderService != null && deliveryService != null && cuadranteService != null;
    }

    // ── Cuadrantes ────────────────────────────────────────────────────────────

    public LinkedList<Cuadrante> listarCuadrantes() {
        if (!isConnected()) return new LinkedList<>();
        try {
            return cuadranteService.listarCuadrantes();
        } catch (Exception e) {
            System.err.println("Error al listar cuadrantes: " + e.getMessage());
            return new LinkedList<>();
        }
    }

    public LinkedList<CuadranteRepository.ConexionEntry> listarConexiones() {
        if (!isConnected()) return new LinkedList<>();
        try {
            return cuadranteService.listarConexiones();
        } catch (Exception e) {
            System.err.println("Error al listar conexiones: " + e.getMessage());
            return new LinkedList<>();
        }
    }

    public LinkedList<String> calcularRutaMasCorta(String origen, String destino) {
        if (!isConnected()) return new LinkedList<>();
        try {
            return cuadranteService.calcularRutaMasCorta(origen, destino);
        } catch (Exception e) {
            System.err.println("Error al calcular ruta: " + e.getMessage());
            return new LinkedList<>();
        }
    }

    public double calcularDistanciaCuadrantes(String origen, String destino) {
        if (!isConnected()) return -1;
        try {
            return cuadranteService.calcularDistancia(origen, destino);
        } catch (Exception e) {
            return -1;
        }
    }

    // ── Pedidos ───────────────────────────────────────────────────────────────

    public LinkedList<Order> getPedidosTodos() {
        if (!isConnected()) return new LinkedList<>();
        try {
            return adminService.getPedidosTodos();  // ← adminService, no orderService
        } catch (Exception e) {
            System.err.println("Error al obtener pedidos: " + e.getMessage());
            return new LinkedList<>();
        }
    }

    // ── Entregas ──────────────────────────────────────────────────────────────

    public Delivery asignarPedidoARepartidor(String orderId, String repartidorId) {
        if (!isConnected()) return null;
        try {
            return deliveryService.asignarPedidoARepartidor(orderId, repartidorId);
        } catch (Exception e) {
            System.err.println("Error al asignar repartidor: " + e.getMessage());
            return null;
        }
    }

    public boolean iniciarEntrega(String orderId) {
        if (!isConnected()) return false;
        try {
            return deliveryService.iniciarEntrega(orderId);
        } catch (Exception e) {
            System.err.println("Error al iniciar entrega: " + e.getMessage());
            return false;
        }
    }

    public boolean completarEntrega(String orderId) {
        if (!isConnected()) return false;
        try {
            return deliveryService.completarEntrega(orderId);
        } catch (Exception e) {
            System.err.println("Error al completar entrega: " + e.getMessage());
            return false;
        }
    }

    public Delivery buscarEntregaPorPedido(String orderId) {
        if (!isConnected()) return null;
        try {
            return deliveryService.buscarEntregaPorPedido(orderId);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean yaEstaAsignado(String orderId) {
        if (!isConnected()) return false;
        try {
            return deliveryService.yaEstaAsignado(orderId);
        } catch (Exception e) {
            return false;
        }
    }

    public void logout() {

    }
}