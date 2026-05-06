package edu.fsadriann.server.model.order;

import edu.fsadriann.app.priorityqueue.PriorityQueue;
import edu.fsadriann.server.model.product.Product;
import edu.fsadriann.model.iterator.Iterator;

import java.rmi.RemoteException;
import java.util.List;

public class KitchenService implements KitchenInterface {

    private static final int NIVELES_PRIORIDAD = 2;

    private final PriorityQueue<Order> cola;

    private final OrderService orderService;

    // ── Constructores ─────────────────────────────────────────────────────────────
    public KitchenService() {
        this(null);
    }

    public KitchenService(OrderService orderService) {
        this.cola         = new PriorityQueue<>(NIVELES_PRIORIDAD);
        this.orderService = orderService;
    }

    // ── Estado de fogones (null = libre) ─────────────────────────────────────
    private Order fogonGrande;    // FOGON_GRANDE  — pedidos complejos
    private Order fogonNormal1;   // FOGON_NORMAL_1
    private Order fogonNormal2;   // FOGON_NORMAL_2
    private Order fogonNormal3;   // FOGON_NORMAL_3

    // ── encolarPedido ─────────────────────────────────────────────────────────

    @Override
    public void encolarPedido(Order pedido) {
        if (pedido == null)
            throw new IllegalArgumentException("El pedido no puede ser nulo.");
        EstadoPedido e = pedido.getEstado();
        if (e == EstadoPedido.CANCELADO || e == EstadoPedido.ENTREGADO)
            throw new IllegalStateException("No se puede encolar en estado: " + e);
        cola.insert(pedido.isPremium() ? 0 : 1, pedido);
    }

    @Override
    public Order procesarSiguientePedido() {
        if (cola.isEmpty()) return null;

        Order siguiente = cola.peek();
        if (siguiente == null) return null;

        if (esComplejo(siguiente)) {
            if (fogonGrande == null) {
                cola.extract();
                siguiente.setEstado(EstadoPedido.EN_PREPARACION);
                fogonGrande = siguiente;
                return siguiente;
            }
            return null; // FOGON_GRANDE ocupado, pedido espera en cola
        } else {
            // Simple: primer fogón normal libre
            if (fogonNormal1 == null) {
                cola.extract();
                siguiente.setEstado(EstadoPedido.EN_PREPARACION);
                fogonNormal1 = siguiente;
                return siguiente;
            } else if (fogonNormal2 == null) {
                cola.extract();
                siguiente.setEstado(EstadoPedido.EN_PREPARACION);
                fogonNormal2 = siguiente;
                return siguiente;
            } else if (fogonNormal3 == null) {
                cola.extract();
                siguiente.setEstado(EstadoPedido.EN_PREPARACION);
                fogonNormal3 = siguiente;
                return siguiente;
            }
            return null; // todos los fogones normales ocupados, pedido espera
        }
    }

    @Override
    public boolean marcarPedidoListo(String pedidoId) {
        if (pedidoId == null)
            throw new IllegalArgumentException("pedidoId no puede ser nulo.");

        Order enFogon = buscarEnFogones(pedidoId);
        if (enFogon == null)
            throw new IllegalStateException(
                "Pedido no encontrado en ningún fogón activo: " + pedidoId);
        if (enFogon.getEstado() != EstadoPedido.EN_PREPARACION)
            throw new IllegalStateException(
                "Solo se marca LISTO desde EN_PREPARACION. Estado: " + enFogon.getEstado());

        if (orderService != null) {
            orderService.marcarListo(pedidoId); // delega setEstado(LISTO) + actualizar()
        } else {
            enFogon.setEstado(EstadoPedido.LISTO); // legacy: mutación directa
        }
        liberarFogon(pedidoId);
        return true;
    }

    // ── obtenerEstadoFogones ──────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public String obtenerEstadoFogones() {
        return "FOGON_GRANDE   : " + descripcionFogon(fogonGrande)  + "\n" +
               "FOGON_NORMAL_1 : " + descripcionFogon(fogonNormal1) + "\n" +
               "FOGON_NORMAL_2 : " + descripcionFogon(fogonNormal2) + "\n" +
               "FOGON_NORMAL_3 : " + descripcionFogon(fogonNormal3);
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    @Override public int tamanoCola()  {
        return cola.size();
    }
    @Override public boolean colaVacia() {
        return cola.isEmpty();
    }

    @Override
    public java.util.List<Order> procesarPedidosDisponibles() {
        java.util.List<Order> procesados = new java.util.ArrayList<>();
        if (cola.isEmpty()) return procesados;

        // Extraemos todos los pedidos de la cola temporalmente
        java.util.List<Order> pendientes = new java.util.ArrayList<>();
        while (!cola.isEmpty()) {
            Order o = cola.extract();
            if (o != null) pendientes.add(o);
        }

        // Intentamos asignar cada uno al fogón compatible
        java.util.List<Order> devolver = new java.util.ArrayList<>();
        for (Order o : pendientes) {
            boolean asignado = false;
            if (esComplejo(o)) {
                if (fogonGrande == null) {
                    o.setEstado(EstadoPedido.EN_PREPARACION);
                    fogonGrande = o;
                    procesados.add(o);
                    asignado = true;
                }
            } else {
                if (fogonNormal1 == null) {
                    o.setEstado(EstadoPedido.EN_PREPARACION);
                    fogonNormal1 = o;
                    procesados.add(o);
                    asignado = true;
                } else if (fogonNormal2 == null) {
                    o.setEstado(EstadoPedido.EN_PREPARACION);
                    fogonNormal2 = o;
                    procesados.add(o);
                    asignado = true;
                } else if (fogonNormal3 == null) {
                    o.setEstado(EstadoPedido.EN_PREPARACION);
                    fogonNormal3 = o;
                    procesados.add(o);
                    asignado = true;
                }
            }
            if (!asignado) devolver.add(o);
        }

        // Re-encolar los que no pudieron asignarse, respetando prioridad
        for (Order o : devolver) {
            cola.insert(o.isPremium() ? 0 : 1, o);
        }

        return procesados;
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    /**
     * Determina si el pedido contiene algún producto complejo.
     * Usa el {@code Iterator} tipado del JAR para evitar ClassCastException.
     */
    private boolean esComplejo(Order pedido) {
        Iterator<Product> it = pedido.getCarrito().iterator();
        while (it.hasNext()) {
            Product p = it.next();
            if (p != null && p.isComplejo()) return true;
        }
        return false;
    }

    /** Busca el pedido activo en cualquiera de los 4 fogones. */
    private Order buscarEnFogones(String pedidoId) {
        if (fogonGrande  != null && pedidoId.equals(fogonGrande.getOrderId()))  return fogonGrande;
        if (fogonNormal1 != null && pedidoId.equals(fogonNormal1.getOrderId())) return fogonNormal1;
        if (fogonNormal2 != null && pedidoId.equals(fogonNormal2.getOrderId())) return fogonNormal2;
        if (fogonNormal3 != null && pedidoId.equals(fogonNormal3.getOrderId())) return fogonNormal3;
        return null;
    }

    /** Libera el fogón que contenía el pedido (lo pone en null). */
    private void liberarFogon(String pedidoId) {
        if (fogonGrande  != null && pedidoId.equals(fogonGrande.getOrderId()))  { fogonGrande  = null; return; }
        if (fogonNormal1 != null && pedidoId.equals(fogonNormal1.getOrderId())) { fogonNormal1 = null; return; }
        if (fogonNormal2 != null && pedidoId.equals(fogonNormal2.getOrderId())) { fogonNormal2 = null; return; }
        if (fogonNormal3 != null && pedidoId.equals(fogonNormal3.getOrderId())) { fogonNormal3 = null; }
    }

    private String descripcionFogon(Order o) {
        if (o == null) return "[ LIBRE ]";
        String id = o.getOrderId();
        String shortId = id.length() > 8 ? id.substring(0, 8) + "..." : id;
        return "[" + shortId + " | " + o.getEstado() + " | " + (o.isPremium() ? "PREMIUM" : "STD") + "]";
    }
}
