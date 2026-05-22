package edu.fsadriann.client.controller.kitchen;

import edu.fsadriann.client.model.kitchen.KitchenModel;
import edu.fsadriann.model.iterator.Iterator;
import edu.fsadriann.server.model.order.Order;
import edu.fsadriann.server.model.product.Product;
import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.view.kitchen.KitchenView;

import javax.swing.Timer;

public class KitchenController {

    private static final int REFRESH_MS = 8_000;

    public static class EntradaFogon {
        final String orderId;
        final Order  order;
        final boolean esComplejo;
        final long    startTime;

        public EntradaFogon(Order order, boolean esComplejo) {
            this.orderId    = order.getOrderId();
            this.order      = order;
            this.esComplejo = esComplejo;
            this.startTime  = System.currentTimeMillis();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof EntradaFogon)) return false;
            return this.orderId.equals(((EntradaFogon) obj).orderId);
        }

        @Override
        public int hashCode() { return orderId.hashCode(); }
    }

    private final KitchenModel model;
    private final KitchenView view;

    private final LinkedList<String>      enqueuedIds = new LinkedList<>();
    private final LinkedList<String>      listosIds   = new LinkedList<>();
    private final LinkedList<EntradaFogon> enFogones  = new LinkedList<>();

    private int  readyTodayCount = 0;
    private long totalPrepMillis = 0;
    private int  totalPrepCount  = 0;

    private Timer    refreshTimer;
    private Runnable logoutAction;

    public KitchenController(KitchenModel model, KitchenView view) {
        this.model = model;
        this.view  = view;
    }

    public void init() {
        view.addLogoutListener(this::handleLogout);
        view.addRefreshListener(this::refresh);
        view.addProcesarSiguienteListener(this::handleProcesarSiguiente);

        refreshTimer = new Timer(REFRESH_MS, e -> refresh());
        refreshTimer.setInitialDelay(0);
        refreshTimer.start();

        view.setMessage("Cocina activa, actualizacion cada " + (REFRESH_MS / 1000) + " s.");
    }

    public void show()                        { view.showView(); }
    public void hide()                        { view.hideView(); }
    public void setLogoutAction(Runnable action) { this.logoutAction = action; }

    // ── Refresh ───────────────────────────────────────────────────────────────

    private void refresh() {
        try {
            LinkedList<Order> pedidos = model.getPedidosEnPreparacion();

            int nuevos = 0;
            Iterator<Order> itP = pedidos.iterator();
            while (itP.hasNext()) {
                Order order = itP.next();
                if (order == null) continue;
                String id = order.getOrderId();

                if (enqueuedIds.contains(id)
                        || enFogonesContiene(id)
                        || listosIds.contains(id)) continue;

                model.encolarPedido(order);
                enqueuedIds.add(id);
                view.addToQueueFull(id, order);
                nuevos++;
            }

            // FIX: intentar mover todos los compatibles a fogones disponibles
            intentarPasarAFogon();

            updateMetrics();
            if (nuevos > 0)
                view.setMessage(nuevos + " pedido(s) nuevo(s) en cola " + timestamp());

        } catch (Exception ex) {
            view.setMessage("Error al refrescar: " + ex.getMessage());
        }
    }

    // ── Procesar siguiente (botón manual) ─────────────────────────────────────

    private void handleProcesarSiguiente() {
        if (model.colaVacia()) {
            view.setMessage("No hay pedidos en cola.");
            return;
        }
        intentarPasarAFogon();
        updateMetrics();
    }

    // ── Mover pedidos compatibles a fogones disponibles ───────────────────────

    private void intentarPasarAFogon() {
        LinkedList<Order> procesados = model.procesarPedidosDisponibles();
        Iterator<Order> itPr = procesados.iterator();
        while (itPr.hasNext()) {
            Order processed = itPr.next();
            if (processed == null) continue;
            mostrarPedidoEnFogon(processed);
        }
    }

    // ── Mostrar pedido en la columna de fogón correspondiente ─────────────────

    private void mostrarPedidoEnFogon(Order processed) {
        String  id       = processed.getOrderId();
        String  sid      = shortId(id);
        boolean complejo = tieneProductoComplejo(processed);

        enFogones.add(new EntradaFogon(processed, complejo));
        view.removeFromQueue(id);

        if (complejo) {
            view.addToComplejasFull(id, processed, () -> handleMarcarListo(id));
            view.setMessage("Pedido " + sid + " → Fogon Grande B4.");
        } else {
            view.addToRapidasFull(id, processed, () -> handleMarcarListo(id));
            view.setMessage("Pedido " + sid + " → Fogon Normal B1/B2/B3.");
        }
    }

    // ── Marcar listo ──────────────────────────────────────────────────────────

    private void handleMarcarListo(String orderId) {
        EntradaFogon entrada = buscarEnFogones(orderId);
        if (entrada == null) {
            view.setMessage("El pedido no esta en un fogon activo.");
            return;
        }

        boolean ok = model.marcarPedidoListo(orderId);
        if (!ok) {
            view.setMessage("No se pudo marcar como listo.");
            return;
        }

        enFogones.remove(entrada);
        listosIds.add(orderId);

        if (entrada.startTime > 0) {
            totalPrepMillis += System.currentTimeMillis() - entrada.startTime;
            totalPrepCount++;
        }
        readyTodayCount++;

        String sid = shortId(orderId);

        if (entrada.esComplejo) {
            view.removeFromComplejas(orderId);
        } else {
            view.removeFromRapidas(orderId);
        }

        view.addToListosFull(orderId, sid, buildClientLabel(entrada.order),
                entrada.order, () -> handleEnviarAEntrega(orderId));        updateMetrics();
        view.setMessage("Pedido " + sid + " listo para entrega.");

        // FIX: al liberar el fogón, mover automáticamente el siguiente compatible
        javax.swing.SwingUtilities.invokeLater(() -> {
            intentarPasarAFogon();
            updateMetrics();
        });
    }

    private void handleEnviarAEntrega(String orderId) {
        view.removeFromListos(orderId);
        listosIds.remove(orderId);
        updateMetrics();
        view.setMessage("Pedido " + shortId(orderId) + " listo para el módulo de entregas.");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private EntradaFogon buscarEnFogones(String orderId) {
        Iterator<EntradaFogon> it = enFogones.iterator();
        while (it.hasNext()) {
            EntradaFogon e = it.next();
            if (e.orderId.equals(orderId)) return e;
        }
        return null;
    }

    private boolean enFogonesContiene(String orderId) {
        return buscarEnFogones(orderId) != null;
    }

    private void updateMetrics() {
        int    queueSize = model.tamanoCola();
        int    inPrep    = enFogones.size();
        String avgTime   = totalPrepCount == 0 ? "-"
                : formatElapsed(totalPrepMillis / totalPrepCount);
        view.setMetrics(queueSize, inPrep, readyTodayCount, avgTime);
    }

    private boolean tieneProductoComplejo(Order order) {
        if (order == null || order.getCarrito() == null) return false;
        Iterator<Product> it = order.getCarrito().iterator();
        while (it.hasNext()) {
            Product p = it.next();
            if (p != null && p.isComplejo()) return true;
        }
        return false;
    }

    private String buildClientLabel(Order order) {
        if (order == null) return "-";
        return order.isPremium() ? "Premium" : "Estandar";
    }

    private String shortId(String id) {
        if (id == null || id.isBlank()) return "-";
        return id.length() > 8 ? id.substring(0, 8) + "..." : id;
    }

    private String formatElapsed(long millis) {
        long s = millis / 1000;
        if (s < 60) return s + " s";
        return (s / 60) + " min " + (s % 60) + " s";
    }

    private String timestamp() {
        java.time.LocalTime now = java.time.LocalTime.now();
        return String.format("%02d:%02d:%02d", now.getHour(), now.getMinute(), now.getSecond());
    }

    private void handleLogout() {
        if (refreshTimer != null) refreshTimer.stop();
        model.logout();
        hide();
        if (logoutAction != null) logoutAction.run();
    }
}