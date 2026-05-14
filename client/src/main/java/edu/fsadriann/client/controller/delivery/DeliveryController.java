package edu.fsadriann.client.controller.delivery;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.client.model.delivery.DeliveryModel;
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

    private final DeliveryModel  model;
    private final DeliveryView view;
    private boolean initialized;
    private boolean cuadrantesLoaded = false;
    private boolean rutaCalculada    = false;
    private Timer   refreshTimer;
    private final edu.fsadriann.client.router.ViewRouter router;

    public DeliveryController(DeliveryModel model, DeliveryView view, edu.fsadriann.client.router.ViewRouter router) {
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

        // Agrupar pedidos por repartidor
        Map<String, java.util.List<Order>> gruposPorRepartidor = new java.util.LinkedHashMap<>();
        java.util.List<Order> sinRepartidor = new java.util.ArrayList<>();

        edu.fsadriann.model.iterator.Iterator<Order> it = todos.iterator();
        while (it.hasNext()) {
            Order o = it.next();
            if (o == null) continue;

            switch (o.getEstado()) {
                case LISTO: {
                    listos++;
                    Delivery delivery = model.buscarEntregaPorPedido(o.getOrderId());
                    boolean asignado  = delivery != null
                            && delivery.getRepartidorId() != null
                            && !delivery.getRepartidorId().isBlank();

                    if (asignado) {
                        gruposPorRepartidor
                                .computeIfAbsent(delivery.getRepartidorId(),
                                        k -> new java.util.ArrayList<>())
                                .add(o);
                    } else {
                        sinRepartidor.add(o);
                    }

                    String cuad = o.getCuadranteDestino();
                    if (cuad != null && !cuad.isBlank() && !cuad.equals("—"))
                        pedidosPorCuadrante.merge(cuad, 1, Integer::sum);
                    break;
                }
                case EN_CAMINO: {
                    enTransito++;
                    Delivery delivery = model.buscarEntregaPorPedido(o.getOrderId());
                    boolean asignado  = delivery != null
                            && delivery.getRepartidorId() != null
                            && !delivery.getRepartidorId().isBlank();

                    if (asignado) {
                        gruposPorRepartidor
                                .computeIfAbsent(delivery.getRepartidorId(),
                                        k -> new java.util.ArrayList<>())
                                .add(o);
                    } else {
                        sinRepartidor.add(o);
                    }

                    String cuad = o.getCuadranteDestino();
                    if (cuad != null && !cuad.isBlank() && !cuad.equals("—"))
                        pedidosPorCuadrante.merge(cuad, 1, Integer::sum);
                    break;
                }
                case ENTREGADO:  entregados++; break;
                case PENDIENTE:
                case EN_PREPARACION: pendientes++; break;
                default: break;
            }
        }

        // Mostrar pedidos sin repartidor individualmente
        for (Order o : sinRepartidor) {
            String cuad      = o.getCuadranteDestino();
            String cliente   = o.getCedulaCliente() != null ? o.getCedulaCliente() : "—";
            String cuadDisplay = (cuad != null && !cuad.isBlank() && !cuad.equals("—"))
                    ? cuad : "Sin cuadrante";
            view.addOrderCard(
                    o.getOrderId(), cliente, cuadDisplay, EstadoPedido.LISTO,
                    null,
                    () -> handleAsignarRepartidor(o.getOrderId()),
                    null
            );
        }

        // Mostrar grupos por repartidor
        for (Map.Entry<String, java.util.List<Order>> entry : gruposPorRepartidor.entrySet()) {
            String rep = entry.getKey();
            java.util.List<Order> grupo = entry.getValue();

            java.util.List<String> ids      = new java.util.ArrayList<>();
            java.util.List<String> clientes = new java.util.ArrayList<>();
            java.util.List<String> cuads    = new java.util.ArrayList<>();
            java.util.List<Runnable> acciones = new java.util.ArrayList<>();

            EstadoPedido estadoGrupo = grupo.get(0).getEstado();

            for (Order o : grupo) {
                ids.add(o.getOrderId());
                clientes.add(o.getCedulaCliente() != null ? o.getCedulaCliente() : "—");
                String cuad = o.getCuadranteDestino();
                cuads.add((cuad != null && !cuad.isBlank() && !cuad.equals("—"))
                        ? cuad : "Sin cuadrante");
                acciones.add(() -> handleCompletarEntrega(o.getOrderId()));
            }

            view.addGroupCard(
                    rep, ids, clientes, cuads, estadoGrupo,
                    // Iniciar todas
                    estadoGrupo == EstadoPedido.LISTO
                            ? () -> handleIniciarGrupo(grupo)
                            : null,
                    // Entregar individualmente
                    estadoGrupo == EstadoPedido.EN_CAMINO ? acciones : null
            );
        }

        for (Map.Entry<String, Integer> entry : pedidosPorCuadrante.entrySet())
            view.setCuadranteConPedidos(entry.getKey(), entry.getValue());

        view.setMetrics(listos, enTransito, entregados, pendientes);
        view.setMessage("Listos: " + listos + "  |  En tránsito: " + enTransito
                + "  |  Entregados: " + entregados);
    }

    // ── Handlers de acciones ──────────────────────────────────────────────────

    private void handleIniciarGrupo(java.util.List<Order> grupo) {
        if (!rutaCalculada) {
            view.showError("Debes calcular la ruta óptima antes de iniciar la entrega.\n"
                    + "Presiona el botón \"Calcular ruta óptima\".");
            return;
        }

        if (!view.confirm("¿Iniciar entrega de " + grupo.size() + " pedido(s) a la vez?")) return;

        int iniciados = 0;
        for (Order o : grupo) {
            if (model.iniciarEntrega(o.getOrderId())) iniciados++;
        }

        view.setMessage(iniciados + " pedido(s) iniciados en camino.");
        refreshOrders();
    }

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
        // Agrupar destinos por repartidor
        Map<String, java.util.List<String>> destinosPorRepartidor = new java.util.LinkedHashMap<>();
        java.util.List<String> destinosSinRepartidor = new java.util.ArrayList<>();

        LinkedList<Order> todos = model.getPedidosTodos();
        if (todos != null) {
            edu.fsadriann.model.iterator.Iterator<Order> it = todos.iterator();
            while (it.hasNext()) {
                Order o = it.next();
                if (o == null) continue;
                if (o.getEstado() != EstadoPedido.LISTO
                        && o.getEstado() != EstadoPedido.EN_CAMINO) continue;
                String cuad = o.getCuadranteDestino();
                if (cuad == null || cuad.isBlank() || cuad.equals("—")) continue;

                Delivery delivery = model.buscarEntregaPorPedido(o.getOrderId());
                if (delivery != null && delivery.getRepartidorId() != null
                        && !delivery.getRepartidorId().isBlank()) {
                    destinosPorRepartidor
                            .computeIfAbsent(delivery.getRepartidorId(),
                                    k -> new java.util.ArrayList<>())
                            .add(cuad);
                } else {
                    if (!destinosSinRepartidor.contains(cuad))
                        destinosSinRepartidor.add(cuad);
                }
            }
        }

        if (destinosPorRepartidor.isEmpty() && destinosSinRepartidor.isEmpty()) {
            view.showError("No hay pedidos con cuadrante asignado para calcular ruta.");
            rutaCalculada = false;
            return;
        }

        // Calcular ruta combinada para mostrar en el grafo
        java.util.List<String> todosDestinos = new java.util.ArrayList<>(destinosSinRepartidor);
        for (java.util.List<String> cuads : destinosPorRepartidor.values())
            for (String c : cuads) if (!todosDestinos.contains(c)) todosDestinos.add(c);

        java.util.List<String> rutaOrdenada = calcularRutaGreedy(ORIGEN, todosDestinos);
        LinkedList<String> rutaCompleta = new LinkedList<>();
        double distanciaTotal = 0;
        String actual = ORIGEN;
        rutaCompleta.add(ORIGEN);

        for (String destino : rutaOrdenada) {
            LinkedList<String> tramo     = model.calcularRutaMasCorta(actual, destino);
            double             distTramo = model.calcularDistanciaCuadrantes(actual, destino);
            if (tramo == null || distTramo < 0) continue;
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
        rutaCalculada = true;

        // Mensaje detallado por repartidor
        StringBuilder msg = new StringBuilder("Ruta calculada. ");
        for (Map.Entry<String, java.util.List<String>> entry : destinosPorRepartidor.entrySet()) {
            msg.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue())
                    .append("  ");
        }
        view.setMessage(msg.toString());
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