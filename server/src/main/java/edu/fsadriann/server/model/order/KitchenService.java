package edu.fsadriann.server.model.order;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.app.priorityqueue.PriorityQueue;
import edu.fsadriann.server.model.product.Product;
import edu.fsadriann.model.iterator.Iterator;

/**
 * Servicio de cocina que gestiona la cola de pedidos y el estado de los fogones.
 * Usa una cola de prioridad con dos niveles: 0 para clientes premium, 1 para estándar.
 * Los pedidos con productos complejos se asignan al fogón grande; los demás, a los normales.
 */
public class KitchenService implements KitchenInterface {

    private static final int NIVELES_PRIORIDAD = 2;

    private final PriorityQueue<Order> cola;
    private final OrderService         orderService;

    private Order fogonGrande;
    private Order fogonNormal1;
    private Order fogonNormal2;
    private Order fogonNormal3;

    /**
     * Crea el servicio de cocina sin integración con {@link OrderService}.
     */
    public KitchenService() {
        this(null);
    }

    /**
     * Crea el servicio de cocina con integración al servicio de pedidos.
     *
     * @param orderService servicio de pedidos para actualizar estados al marcar como listo
     */
    public KitchenService(OrderService orderService) {
        this.cola         = new PriorityQueue<>(NIVELES_PRIORIDAD);
        this.orderService = orderService;
    }

    /**
     * Agrega un pedido a la cola según su prioridad (premium = mayor prioridad).
     *
     * @param pedido pedido a encolar
     * @throws IllegalArgumentException si el pedido es nulo
     * @throws IllegalStateException    si el pedido está cancelado o entregado
     */
    @Override
    public void encolarPedido(Order pedido) {
        if (pedido == null)
            throw new IllegalArgumentException("El pedido no puede ser nulo.");
        EstadoPedido e = pedido.getEstado();
        if (e == EstadoPedido.CANCELADO || e == EstadoPedido.ENTREGADO)
            throw new IllegalStateException("No se puede encolar en estado: " + e);
        cola.insert(pedido.isPremium() ? 0 : 1, pedido);
    }

    /**
     * Extrae el pedido de mayor prioridad y lo asigna al fogón disponible correspondiente.
     * Los pedidos complejos van al fogón grande; los simples, al primer fogón normal libre.
     *
     * @return el pedido asignado al fogón, o {@code null} si no hay fogón libre
     */
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
            return null;
        } else {
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
            return null;
        }
    }

    /**
     * Marca un pedido en preparación como listo y libera el fogón que ocupaba.
     *
     * @param pedidoId identificador del pedido
     * @return {@code true} si se marcó exitosamente
     * @throws IllegalArgumentException si el pedidoId es nulo
     * @throws IllegalStateException    si el pedido no está en ningún fogón activo
     *                                  o no está en estado EN_PREPARACION
     */
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
            orderService.marcarListo(pedidoId);
        } else {
            enFogon.setEstado(EstadoPedido.LISTO);
        }
        liberarFogon(pedidoId);
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String obtenerEstadoFogones() {
        return "FOGON_GRANDE   : " + descripcionFogon(fogonGrande)  + "\n" +
               "FOGON_NORMAL_1 : " + descripcionFogon(fogonNormal1) + "\n" +
               "FOGON_NORMAL_2 : " + descripcionFogon(fogonNormal2) + "\n" +
               "FOGON_NORMAL_3 : " + descripcionFogon(fogonNormal3);
    }

    /** @return cantidad de pedidos en espera en la cola */
    @Override
    public int tamanoCola() { return cola.size(); }

    /** @return {@code true} si no hay pedidos en la cola */
    @Override
    public boolean colaVacia() { return cola.isEmpty(); }

    /**
     * Procesa todos los pedidos de la cola que tengan un fogón compatible disponible.
     * Los pedidos que no puedan asignarse vuelven a la cola manteniendo su prioridad.
     *
     * @return lista de pedidos que fueron asignados a fogones
     */
    @Override
    public LinkedList<Order> procesarPedidosDisponibles() {
        LinkedList<Order> procesados = new LinkedList<>();
        if (cola.isEmpty()) return procesados;

        LinkedList<Order> pendientes = new LinkedList<>();
        while (!cola.isEmpty()) {
            Order o = cola.extract();
            if (o != null) pendientes.add(o);
        }

        LinkedList<Order> devolver = new LinkedList<>();
        Iterator<Order> it = pendientes.iterator();
        while (it.hasNext()) {
            Order o = it.next();
            if (o == null) continue;
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

        Iterator<Order> itD = devolver.iterator();
        while (itD.hasNext()) {
            Order o = itD.next();
            if (o != null) cola.insert(o.isPremium() ? 0 : 1, o);
        }

        return procesados;
    }

    private boolean esComplejo(Order pedido) {
        Iterator<Product> it = pedido.getCarrito().iterator();
        while (it.hasNext()) {
            Product p = it.next();
            if (p != null && p.isComplejo()) return true;
        }
        return false;
    }

    private Order buscarEnFogones(String pedidoId) {
        if (fogonGrande  != null && pedidoId.equals(fogonGrande.getOrderId()))  return fogonGrande;
        if (fogonNormal1 != null && pedidoId.equals(fogonNormal1.getOrderId())) return fogonNormal1;
        if (fogonNormal2 != null && pedidoId.equals(fogonNormal2.getOrderId())) return fogonNormal2;
        if (fogonNormal3 != null && pedidoId.equals(fogonNormal3.getOrderId())) return fogonNormal3;
        return null;
    }

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
