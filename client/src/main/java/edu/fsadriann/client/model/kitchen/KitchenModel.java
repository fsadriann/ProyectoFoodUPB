package edu.fsadriann.client.model.kitchen;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.server.model.order.KitchenInterface;
import edu.fsadriann.server.model.order.Order;
import edu.fsadriann.server.model.order.OrderInterface;

public class KitchenModel {

    private KitchenInterface kitchenService;
    private OrderInterface   orderService;

    public KitchenModel() {}

    public void injectServices(KitchenInterface k, OrderInterface o) {
        this.kitchenService = k;
        this.orderService   = o;
    }

    private boolean isConnected() {
        return kitchenService != null && orderService != null;
    }

    public void encolarPedido(Order pedido) {
        if (!isConnected()) return;
        try {
            kitchenService.encolarPedido(pedido);
        } catch (Exception e) {
            System.err.println("Error al encolar pedido: " + e.getMessage());
        }
    }

    public LinkedList<Order> procesarPedidosDisponibles() {
        if (!isConnected()) return new LinkedList<>();
        try {
            return kitchenService.procesarPedidosDisponibles();
        } catch (Exception e) {
            System.err.println("Error al procesar pedidos disponibles: " + e.getMessage());
            return new LinkedList<>();
        }
    }

    public Order procesarSiguientePedido() {
        if (!isConnected()) return null;
        try {
            return kitchenService.procesarSiguientePedido();
        } catch (Exception e) {
            System.err.println("Error al procesar siguiente pedido: " + e.getMessage());
            return null;
        }
    }

    public boolean marcarPedidoListo(String pedidoId) {
        if (!isConnected()) return false;
        try {
            return kitchenService.marcarPedidoListo(pedidoId);
        } catch (Exception e) {
            System.err.println("Error al marcar pedido listo: " + e.getMessage());
            return false;
        }
    }

    public int tamanoCola() {
        if (!isConnected()) return 0;
        try {
            return kitchenService.tamanoCola();
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean colaVacia() {
        if (!isConnected()) return true;
        try {
            return kitchenService.colaVacia();
        } catch (Exception e) {
            return true;
        }
    }

    public LinkedList<Order> getPedidosEnPreparacion() {
        if (!isConnected()) return new LinkedList<>();
        try {
            LinkedList<Order> lista = orderService.getPedidosEnPreparacion();
            return lista != null ? lista : new LinkedList<>();
        } catch (Exception e) {
            System.err.println("Error al obtener pedidos en preparación: " + e.getMessage());
            return new LinkedList<>();
        }
    }

    public void logout() {}
}
