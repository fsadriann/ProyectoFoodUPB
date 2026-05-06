package edu.fsadriann.client.controller.delivery;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.client.model.ClientModel;
import edu.fsadriann.server.model.cuadrante.Cuadrante;
import edu.fsadriann.server.model.cuadrante.CuadranteRepository;
import edu.fsadriann.server.model.delivery.Delivery;
import edu.fsadriann.server.model.order.EstadoPedido;
import edu.fsadriann.server.model.order.Order;
import edu.fsadriann.view.delivery.DeliveryView;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeliveryController {

    private static final String ORIGEN     = "UPB";
    private static final int    REFRESH_MS = 8_000;

    private final ClientModel  model;
    private final DeliveryView view;
    private boolean initialized;
    private boolean cuadrantesLoaded = false; // ← evita reconstruir grafo en cada refresh
    private boolean rutaCalculada    = false; // ← controla si hay ruta antes de iniciar entrega
    private Timer   refreshTimer;
    private final edu.fsadriann.client.router.ViewRouter router;

    public DeliveryController(ClientModel model, DeliveryView view, edu.fsadriann.client.router.ViewRouter router) {
        this.model = model;
        this.view  = view;
        this.router = router;
    }

    public void init() {
        if (!initialized) {
            view.addLogoutListener(this::handleLogout);
            view.addCalcRouteListener(this::handleCalcularRuta);
            view.addRefreshListener(this::handleManualRefresh);
            initialized = true;

            refreshTimer = new Timer(REFRESH_MS, e -> refreshOrders());
            refreshTimer.setInitialDelay(0);
            refreshTimer.start();
        }
        if (model.isConnected()) {
            refreshCuadrantes(); // solo al iniciar
            refreshOrders();
        }
    }

    private void handleManualRefresh() {
        cuadrantesLoaded = false;
        refreshCuadrantes();
        refreshOrders();
    }

    public void refresh() {
        refreshOrders();
    }

    // ── Cuadrantes ────────────────────────────────────────────────────────────

    private void refreshCuadrantes() {
        if (cuadrantesLoaded) return;

        LinkedList<Cuadrante> cuadrantes = model.listarCuadrantes();
        view.setCuadrantes(cuadrantes);
        if (cuadrantes == null) return;

        // Solo las conexiones reales del JSON
        LinkedList<CuadranteRepository.ConexionEntry> conexiones = model.listarConexiones();
        if (conexiones != null) {
            edu.fsadriann.model.iterator.Iterator<CuadranteRepository.ConexionEntry> it =
                    conexiones.iterator();
            while (it.hasNext()) {
                CuadranteRepository.ConexionEntry conn = it.next();
                if (conn != null) {
                    view.addGraphEdge(conn.origen, conn.destino, conn.distancia);
                }
            }
        }

        cuadrantesLoaded = true;
    }

    // ── Pedidos ───────────────────────────────────────────────────────────────

    private void refreshOrders() {
        view.clearOrders();

        LinkedList<Order> todos = model.getPedidosTodos();
        if (todos == null) return;

        int listos = 0, enTransito = 0, entregados = 0, pendientes = 0;
        Map<String, Integer> pedidosPorCuadrante = new HashMap<>();

        edu.fsadriann.model.iterator.Iterator<Order> it = todos.iterator();
        while (it.hasNext()) {
            Order o = it.next();
            if (o == null) continue;

            switch (o.getEstado()) {
                case LISTO: {
                    listos++;
                    String cuad       = o.getCuadranteDestino();
                    String cliente    = o.getCedulaCliente() != null ? o.getCedulaCliente() : "—";
                    boolean tieneCuad = cuad != null && !cuad.isBlank() && !cuad.equals("—");
                    String cuadDisplay = tieneCuad ? cuad : "Sin cuadrante";

                    Delivery delivery = model.buscarEntregaPorPedido(o.getOrderId());
                    boolean asignado  = delivery != null
                            && delivery.getRepartidorId() != null
                            && !delivery.getRepartidorId().isBlank();

                    view.addOrderCard(
                            o.getOrderId(), cliente, cuadDisplay, EstadoPedido.LISTO,
                            asignado ? delivery.getRepartidorId() : null,
                            // Botón "Asignar repartidor" — solo si no tiene repartidor
                            asignado ? null : () -> handleAsignarRepartidor(o.getOrderId()),
                            // Botón "Iniciar entrega" — solo si tiene repartidor Y hay ruta
                            asignado ? () -> handleIniciarEntrega(o.getOrderId()) : null
                    );

                    if (tieneCuad) pedidosPorCuadrante.merge(cuad, 1, Integer::sum);
                    break;
                }
                case EN_CAMINO: {
                    enTransito++;
                    String cuad       = o.getCuadranteDestino();
                    String cliente    = o.getCedulaCliente() != null ? o.getCedulaCliente() : "—";
                    boolean tieneCuad = cuad != null && !cuad.isBlank() && !cuad.equals("—");
                    String cuadDisplay = tieneCuad ? cuad : "Sin cuadrante";

                    Delivery delivery = model.buscarEntregaPorPedido(o.getOrderId());

                    view.addOrderCard(
                            o.getOrderId(), cliente, cuadDisplay, EstadoPedido.EN_CAMINO,
                            delivery != null ? delivery.getRepartidorId() : null,
                            null,
                            () -> handleCompletarEntrega(o.getOrderId())
                    );

                    if (tieneCuad) pedidosPorCuadrante.merge(cuad, 1, Integer::sum);
                    break;
                }
                case ENTREGADO:
                    entregados++;
                    break;
                case PENDIENTE:
                case EN_PREPARACION:
                    pendientes++;
                    break;
                default:
                    break;
            }
        }

        for (Map.Entry<String, Integer> entry : pedidosPorCuadrante.entrySet()) {
            view.setCuadranteConPedidos(entry.getKey(), entry.getValue());
        }

        view.setMetrics(listos, enTransito, entregados, pendientes);
        view.setMessage("Listos: " + listos + "  |  En tránsito: " + enTransito
                + "  |  Entregados: " + entregados);
    }

    // ── Handlers de acciones ──────────────────────────────────────────────────

    private void handleAsignarRepartidor(String orderId) {
        String repartidor = view.promptRepartidor();
        if (repartidor == null || repartidor.isBlank()) return;

        Delivery delivery = model.asignarPedidoARepartidor(orderId, repartidor);
        if (delivery == null) {
            view.showError("No se pudo asignar el repartidor.\n"
                    + "Verifica que el pedido esté en estado LISTO y tenga cuadrante asignado.");
            return;
        }

        view.setMessage("Repartidor «" + repartidor + "» asignado. "
                + "Calcula la ruta e inicia la entrega.");
        refreshOrders();
    }

    private void handleIniciarEntrega(String orderId) {
        // Validar que se haya calculado la ruta antes de iniciar
        if (!rutaCalculada) {
            view.showError("Debes calcular la ruta óptima antes de iniciar la entrega.\n"
                    + "Presiona el botón \"Calcular ruta óptima\".");
            return;
        }

        if (!view.confirm("¿Entregar el pedido al repartidor e iniciar envío?")) return;

        boolean ok = model.iniciarEntrega(orderId);
        if (!ok) {
            view.showError("No se pudo iniciar la entrega.");
            return;
        }

        view.setMessage("Pedido " + shortId(orderId) + " en camino.");
        refreshOrders();
    }

    private void handleCompletarEntrega(String orderId) {
        if (!view.confirm("¿Confirmar que el pedido fue entregado al cliente?")) return;

        boolean ok = model.completarEntrega(orderId);
        if (!ok) {
            view.showError("No se pudo registrar la entrega.\n"
                    + "Verifica la conexión con el servidor.");
            return;
        }

        // Notificación visible al completar entrega
        JOptionPane.showMessageDialog(
                view.getFrame(),
                "✓ Pedido " + shortId(orderId) + " entregado exitosamente al cliente.",
                "Entrega completada",
                JOptionPane.INFORMATION_MESSAGE
        );

        view.setMessage("Pedido " + shortId(orderId) + " entregado. ✓");
        refreshOrders();
    }

    // ── Ruta ──────────────────────────────────────────────────────────────────

    private void handleCalcularRuta() {
        List<String> destinos = getDestinosConPedidos();

        if (destinos.isEmpty()) {
            int sinCuadrante = contarPedidosSinCuadrante();
            if (sinCuadrante > 0) {
                view.showError(sinCuadrante + " pedido(s) no tienen cuadrante asignado.\n"
                        + "El operador debe asignarlo al generar la factura.");
            } else {
                view.showError("No hay pedidos LISTO o EN_CAMINO para calcular ruta.");
            }
            rutaCalculada = false;
            return;
        }

        List<String> rutaOrdenada = calcularRutaGreedy(ORIGEN, destinos);
        LinkedList<String> rutaCompleta = new LinkedList<>();
        double distanciaTotal = 0;
        String actual = ORIGEN;
        rutaCompleta.add(ORIGEN);

        for (String destino : rutaOrdenada) {
            LinkedList<String> tramo     = model.calcularRutaMasCorta(actual, destino);
            double             distTramo = model.calcularDistanciaCuadrantes(actual, destino);
            if (tramo == null || distTramo < 0) {
                view.setMessage("No existe ruta hacia " + destino + ".");
                continue;
            }
            edu.fsadriann.model.iterator.Iterator<String> itT = tramo.iterator();
            boolean primero = true;
            while (itT.hasNext()) {
                String nodo = itT.next();
                if (primero) { primero = false; continue; }
                if (nodo != null) rutaCompleta.add(nodo);
            }
            distanciaTotal += distTramo;
            actual = destino;
        }

        view.setRuta(rutaCompleta, distanciaTotal);
        rutaCalculada = true; // ← habilita el botón "Iniciar entrega"
        view.setMessage(String.format(
                "Ruta calculada: %d parada(s), %.2f km totales. Ya puedes iniciar entregas.",
                rutaOrdenada.size(), distanciaTotal));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private List<String> calcularRutaGreedy(String origen, List<String> destinos) {
        List<String> pendientes = new ArrayList<>(destinos);
        List<String> ruta       = new ArrayList<>();
        String actual           = origen;
        while (!pendientes.isEmpty()) {
            String masCercano     = null;
            double menorDistancia = Double.MAX_VALUE;
            for (String candidato : pendientes) {
                double dist = model.calcularDistanciaCuadrantes(actual, candidato);
                if (dist >= 0 && dist < menorDistancia) {
                    menorDistancia = dist;
                    masCercano    = candidato;
                }
            }
            if (masCercano == null) break;
            ruta.add(masCercano);
            pendientes.remove(masCercano);
            actual = masCercano;
        }
        return ruta;
    }

    private List<String> getDestinosConPedidos() {
        List<String> destinos = new ArrayList<>();
        LinkedList<Order> todos = model.getPedidosTodos();
        if (todos == null) return destinos;
        edu.fsadriann.model.iterator.Iterator<Order> it = todos.iterator();
        while (it.hasNext()) {
            Order o = it.next();
            if (o == null) continue;
            if (o.getEstado() == EstadoPedido.LISTO
                    || o.getEstado() == EstadoPedido.EN_CAMINO) {
                String cuad = o.getCuadranteDestino();
                if (cuad != null && !cuad.isBlank() && !cuad.equals("—")
                        && !destinos.contains(cuad)) destinos.add(cuad);
            }
        }
        return destinos;
    }

    private int contarPedidosSinCuadrante() {
        int count = 0;
        LinkedList<Order> todos = model.getPedidosTodos();
        if (todos == null) return 0;
        edu.fsadriann.model.iterator.Iterator<Order> it = todos.iterator();
        while (it.hasNext()) {
            Order o = it.next();
            if (o == null) continue;
            if (o.getEstado() == EstadoPedido.LISTO) {
                String cuad = o.getCuadranteDestino();
                if (cuad == null || cuad.isBlank() || cuad.equals("—")) count++;
            }
        }
        return count;
    }

    private void handleLogout() {
        if (refreshTimer != null) refreshTimer.stop();
        rutaCalculada    = false;
        cuadrantesLoaded = false;
        model.logout();
        view.hideView();
        router.showLogin();
    }

    private String shortId(String id) {
        if (id == null || id.isBlank()) return "—";
        return id.length() > 8 ? id.substring(0, 8) + "..." : id;
    }
}