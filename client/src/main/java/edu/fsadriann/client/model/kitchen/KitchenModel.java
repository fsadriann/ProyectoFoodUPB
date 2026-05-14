package edu.fsadriann.client.model.kitchen;

import edu.fsadriann.server.model.order.KitchenInterface;
import edu.fsadriann.server.model.order.Order;
import edu.fsadriann.server.model.order.OrderInterface;

import java.util.ArrayList;
import java.util.List;

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

    // ── Cola de cocina ────────────────────────────────────────────────────────

    public void encolarPedido(Order pedido) {
        if (!isConnected()) return;
        try {
            kitchenService.encolarPedido(pedido);
        } catch (Exception e) {
            System.err.println("Error al encolar pedido: " + e.getMessage());
        }
    }

    public List<Order> procesarPedidosDisponibles() {
        if (!isConnected()) return new ArrayList<>();
        try {
            return kitchenService.procesarPedidosDisponibles();
        } catch (Exception e) {
            System.err.println("Error al procesar pedidos disponibles: " + e.getMessage());
            return new ArrayList<>();
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

    // ── Pedidos en preparación ────────────────────────────────────────────────

    public List<Order> getPedidosEnPreparacion() {
        if (!isConnected()) return new ArrayList<>();
        try {
            edu.fsadriann.app.linkedlist.singly.singly.LinkedList<Order> lista =
                    orderService.getPedidosEnPreparacion();
            List<Order> resultado = new ArrayList<>();
            if (lista == null) return resultado;
            edu.fsadriann.model.iterator.Iterator<Order> it = lista.iterator();
            while (it.hasNext()) {
                Order o = it.next();
                if (o != null) resultado.add(o);
            }
            return resultado;
        } catch (Exception e) {
            System.err.println("Error al obtener pedidos en preparación: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void logout() {

    }
}