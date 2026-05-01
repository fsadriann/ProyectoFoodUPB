package edu.fsadriann.foodpbproyecto.model.order;

import edu.fsadriann.app.priorityqueue.PriorityQueue;
import edu.fsadriann.foodpbproyecto.model.product.Product;
import edu.fsadriann.model.iterator.Iterator;

/**
 * Servicio de cocina: cola priorizada + estaciones reales. RF-05.
 *
 * <p><b>Estructura principal:</b> {@code PriorityQueue<Order>} del JAR (2 niveles).
 * <ul>
 *   <li>Nivel 0 → pedidos premium (mayor prioridad)</li>
 *   <li>Nivel 1 → pedidos estándar</li>
 *   <li>Dentro de cada nivel: FIFO garantizado por la cola interna del JAR</li>
 * </ul>
 *
 * <p><b>Estado de fogones:</b> 4 campos {@code Order} (null = libre, no-null = ocupado).
 * No se usa ninguna colección de Java para esto — solo referencias directas.
 * <pre>
 *   fogonGrande   → null | Order  (complejo)
 *   fogonNormal1  → null | Order  (simple)
 *   fogonNormal2  → null | Order  (simple)
 *   fogonNormal3  → null | Order  (simple)
 * </pre>
 *
 * <p><b>Lógica de bloqueo:</b> si el fogón compatible está ocupado, el pedido
 * permanece en la cola hasta que {@link #marcarPedidoListo(String)} libere el fogón
 * y dispare {@link #procesarSiguientePedido()} automáticamente.
 *
 * @author fsadriann
 */
public class KitchenService implements KitchenInterface {

    private static final int NIVELES_PRIORIDAD = 2;

    /** Cola de prioridad del JAR. */
    private final PriorityQueue<Order> cola;

    // ── Estado de fogones (null = libre) ─────────────────────────────────────
    private Order fogonGrande;    // FOGON_GRANDE  — pedidos complejos
    private Order fogonNormal1;   // FOGON_NORMAL_1
    private Order fogonNormal2;   // FOGON_NORMAL_2
    private Order fogonNormal3;   // FOGON_NORMAL_3

    public KitchenService() {
        this.cola = new PriorityQueue<>(NIVELES_PRIORIDAD);
    }

    // ── encolarPedido ─────────────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     * Premium → prioridad 0 | Estándar → prioridad 1.
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

    // ── procesarSiguientePedido ───────────────────────────────────────────────

    /**
     * {@inheritDoc}
     *
     * <p>Algoritmo:
     * <ol>
     *   <li>Mira el tope de la cola sin extraer ({@code peek}).</li>
     *   <li>Si es complejo y {@link #fogonGrande} está libre → extrae y asigna.</li>
     *   <li>Si es simple y hay algún {@code FOGON_NORMAL} libre → extrae y asigna al primero.</li>
     *   <li>Si el fogón compatible está ocupado → no extrae, retorna {@code null}.</li>
     * </ol>
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

    // ── marcarPedidoListo ─────────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     *
     * <p>Pasos: busca el pedido en fogones → valida estado → marca LISTO →
     * libera fogón → llama {@link #procesarSiguientePedido()}.
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

        enFogon.setEstado(EstadoPedido.LISTO);
        liberarFogon(pedidoId);
        procesarSiguientePedido(); // intenta asignar siguiente compatible
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

    @Override public int tamanoCola()  { return cola.size(); }
    @Override public boolean colaVacia() { return cola.isEmpty(); }

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
