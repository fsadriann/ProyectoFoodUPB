package edu.fsadriann.foodpbproyecto.model.delivery;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.foodpbproyecto.model.cuadrante.CuadranteInterface;
import edu.fsadriann.foodpbproyecto.model.order.EstadoPedido;
import edu.fsadriann.foodpbproyecto.model.order.Order;
import edu.fsadriann.foodpbproyecto.model.order.OrderService;
import edu.fsadriann.model.iterator.Iterator;

import java.rmi.RemoteException;

/**
 * Servicio de entregas: implementa {@link DeliveryInterface}. RF-13.
 *
 * <p><b>Almacenamiento:</b> {@code LinkedList<Delivery>} del JAR (singly linked).
 * Registro en memoria — clave de búsqueda: {@code orderId}.
 *
 * <p><b>Dependencias externas (inyectadas por parámetro):</b>
 * <ul>
 *   <li>{@code OrderService} — para validar estado del pedido y ejecutar transiciones.</li>
 *   <li>{@code CuadranteInterface} — para el cálculo de ruta (Dijkstra via RF-06).</li>
 * </ul>
 * No se guardan en el constructor para minimizar acoplamiento.
 *
 * <p><b>Ruta calculada:</b> se delega a {@code OrderService.calcularRutaEntrega()}
 * (que internamente llama a {@code CuadranteService.calcularRutaMasCorta()}).
 * Se almacena como snapshot en {@link Delivery} al momento de la asignación.
 *
 * @author fsadriann
 */
public class DeliveryService implements DeliveryInterface {

    /** Registro de entregas activas: orderId → Delivery. */
    private final LinkedList<Delivery> entregas;

    public DeliveryService() {
        this.entregas = new LinkedList<>();
    }

    // ── Asignación ────────────────────────────────────────────────────────────

    @Override
    public Delivery asignarPedidoARepartidor(String orderId, String repartidorId,
                                             CuadranteInterface cs, OrderService os) {
        // Validar entradas básicas
        if (orderId == null) return null;
        if (repartidorId == null || repartidorId.isBlank())
            throw new IllegalArgumentException(
                    "El repartidorId no puede ser nulo ni vacío.");

        // Prevenir asignación duplicada
        if (yaEstaAsignado(orderId)) return null;

        try {
            Order pedido = os.buscarPedido(orderId);
            if (pedido == null) return null;
            if (pedido.getEstado() != EstadoPedido.LISTO) return null;
            if (pedido.getCuadranteDestino() == null) return null;

            // Calcular ruta reutilizando RF-13 de OrderService (Dijkstra via CuadranteService)
            var ruta = os.calcularRutaEntrega(orderId, cs);
            if (ruta == null) return null; // no hay ruta en el grafo

            Delivery delivery = new Delivery(orderId, repartidorId, ruta);
            entregas.add(delivery);
            return delivery;
        } catch (RemoteException e) {
            return null; // no ocurre en implementación en memoria
        }
    }

    // ── Ciclo de vida ─────────────────────────────────────────────────────────

    @Override
    public boolean iniciarEntrega(String orderId, OrderService os) {
        if (orderId == null) return false;
        if (!yaEstaAsignado(orderId)) return false; // sin asignación no puede iniciar
        return os.marcarEnCamino(orderId);           // LISTO → EN_CAMINO (valida estado)
    }

    @Override
    public boolean completarEntrega(String orderId, OrderService os) {
        if (orderId == null) return false;
        if (!yaEstaAsignado(orderId)) return false;  // sin asignación no puede completar
        return os.marcarEntregado(orderId);           // EN_CAMINO → ENTREGADO (valida estado)
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    @Override
    public Delivery buscarEntregaPorPedido(String orderId) {
        if (orderId == null) return null;
        Iterator<Delivery> it = entregas.iterator();
        while (it.hasNext()) {
            Delivery d = it.next();
            if (d != null && orderId.equals(d.getOrderId())) return d;
        }
        return null;
    }

    @Override
    public boolean yaEstaAsignado(String orderId) {
        return buscarEntregaPorPedido(orderId) != null;
    }
}
