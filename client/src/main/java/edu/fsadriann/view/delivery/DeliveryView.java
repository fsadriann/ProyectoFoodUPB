package edu.fsadriann.view.delivery;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.server.model.cuadrante.Cuadrante;
import edu.fsadriann.server.model.order.EstadoPedido;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class DeliveryView extends JFrame {

    private static final Color BG       = Color.WHITE;
    private static final Color BG2      = new Color(245, 245, 245);
    private static final Color BLUE     = new Color(24, 95, 165);
    private static final Color BLUE_BG  = new Color(230, 241, 251);
    private static final Color GREEN    = new Color(40, 167, 69);
    private static final Color GREEN_BG = new Color(212, 237, 218);
    private static final Color GREEN_FG = new Color(21, 87, 36);
    private static final Color ORANGE   = new Color(210, 95, 20);
    private static final Color ORANGE_BG= new Color(255, 235, 200);
    private static final Color TEXT     = new Color(30, 30, 30);
    private static final Color TEXT2    = new Color(100, 100, 100);
    private static final Color TEXT3    = new Color(150, 150, 150);
    private static final Color BORDER_C = new Color(220, 220, 220);
    private static final Color DANGER   = new Color(220, 53, 69);

    private final JFrame frame;
    private JButton logoutBtn;
    private JButton calcRouteBtn;
    private JButton refreshBtn;
    private JLabel  statusLabel;

    private JLabel metricAssigned;
    private JLabel metricTransit;
    private JLabel metricDelivered;
    private JLabel metricPending;

    private JPanel assignedOrdersBody;
    private JPanel cuadrantesGrid;
    private JPanel routeBody;
    private GraphPanel graphPanel;

    private final Map<String, JPanel> cuadranteCells = new HashMap<>();

    public DeliveryView(String userLabel) {
        super("Food UPB — Entregas");
        frame = this;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 820));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG2);
        root.add(buildHeader(),  BorderLayout.NORTH);
        root.add(buildContent(), BorderLayout.CENTER);
        root.add(buildStatus(),  BorderLayout.SOUTH);
        setContentPane(root);
        pack();
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(BG);
        h.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER_C),
                new EmptyBorder(10, 16, 10, 16)));

        JLabel title = new JLabel("Food UPB — Entregas");
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        title.setForeground(BLUE);

        refreshBtn = new JButton("Actualizar");
        refreshBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        logoutBtn = new JButton("Cerrar sesión");
        logoutBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);
        btns.add(refreshBtn);
        btns.add(logoutBtn);

        h.add(title, BorderLayout.WEST);
        h.add(btns,  BorderLayout.EAST);
        return h;
    }

    // ── Content ───────────────────────────────────────────────────────────────

    private JPanel buildContent() {
        JPanel c = new JPanel(new BorderLayout(0, 12));
        c.setBackground(BG2);
        c.setBorder(new EmptyBorder(14, 14, 14, 14));
        c.add(buildMetrics(), BorderLayout.NORTH);
        c.add(buildBody(),    BorderLayout.CENTER);
        return c;
    }

    private JPanel buildMetrics() {
        JPanel row = new JPanel(new GridLayout(1, 4, 10, 0));
        row.setOpaque(false);
        metricAssigned  = new JLabel("—");
        metricTransit   = new JLabel("—");
        metricDelivered = new JLabel("—");
        metricPending   = new JLabel("—");
        row.add(metricTile("Listos para entregar", metricAssigned));
        row.add(metricTile("En tránsito",          metricTransit));
        row.add(metricTile("Entregados hoy",        metricDelivered));
        row.add(metricTile("Pendientes",            metricPending));
        return row;
    }

    private JPanel metricTile(String label, JLabel val) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_C), new EmptyBorder(10, 12, 10, 12)));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lbl.setForeground(TEXT2);
        val.setFont(new Font("SansSerif", Font.BOLD, 22));
        val.setForeground(TEXT);
        p.add(lbl);
        p.add(Box.createVerticalStrut(4));
        p.add(val);
        return p;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(14, 0));
        body.setOpaque(false);
        body.add(buildOrdersPanel(), BorderLayout.WEST);
        body.add(buildMapAndRoute(), BorderLayout.CENTER);
        return body;
    }

    // ── Panel izquierdo: pedidos ──────────────────────────────────────────────

    private JScrollPane buildOrdersPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG);
        header.setBorder(new EmptyBorder(8, 10, 6, 10));
        JLabel lbl = new JLabel("PEDIDOS");
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl.setForeground(TEXT2);
        header.add(lbl, BorderLayout.WEST);

        assignedOrdersBody = new JPanel();
        assignedOrdersBody.setLayout(new BoxLayout(assignedOrdersBody, BoxLayout.Y_AXIS));
        assignedOrdersBody.setBackground(BG);
        assignedOrdersBody.setBorder(new EmptyBorder(0, 8, 8, 8));

        // Wrapper que empuja tarjetas al norte para que no se estiren verticalmente
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG);
        wrapper.add(header, BorderLayout.NORTH);
        wrapper.add(assignedOrdersBody, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(new LineBorder(BORDER_C));
        scroll.getViewport().setBackground(BG);
        scroll.setPreferredSize(new Dimension(290, 0));
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        return scroll;
    }

    public void addOrderCard(String orderId, String clientName, String cuadrante,
                             EstadoPedido estado, String repartidor,
                             Runnable onAsignar, Runnable onAccion) {
        SwingUtilities.invokeLater(() -> {
            boolean esListo  = estado == EstadoPedido.LISTO;
            boolean enCamino = estado == EstadoPedido.EN_CAMINO;
            boolean asignado = repartidor != null && !repartidor.isBlank();

            Color bordeColor = enCamino ? BLUE    : GREEN;
            Color badgeBg    = enCamino ? BLUE_BG : GREEN_BG;
            Color badgeFg    = enCamino ? BLUE    : GREEN_FG;
            String estadoTxt = enCamino ? "En tránsito" : (asignado ? "Repartidor asignado" : "Listo");

            // Tarjeta — igual que KitchenView: BoxLayout Y_AXIS + border + MAX_WIDTH libre
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(BG);
            card.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 999));
            card.setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(0, 4, 0, 0, bordeColor),
                    BorderFactory.createCompoundBorder(
                            new LineBorder(BORDER_C),
                            new EmptyBorder(10, 10, 10, 10))));

            // Fila superior: ID + badge
            JPanel topRow = new JPanel(new BorderLayout(4, 0));
            topRow.setOpaque(false);
            topRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
            topRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel idLbl = new JLabel(shortId(orderId) + " — " + clientName);
            idLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
            idLbl.setForeground(TEXT);
            JLabel badge = new JLabel(" " + estadoTxt + " ");
            badge.setFont(new Font("SansSerif", Font.BOLD, 9));
            badge.setBackground(badgeBg);
            badge.setForeground(badgeFg);
            badge.setOpaque(true);
            badge.setBorder(new EmptyBorder(1, 4, 1, 4));
            topRow.add(idLbl,  BorderLayout.WEST);
            topRow.add(badge,  BorderLayout.EAST);
            card.add(topRow);
            card.add(Box.createVerticalStrut(4));

            // Cuadrante
            boolean sinCuad = cuadrante == null || cuadrante.equals("Sin cuadrante");
            JLabel cuadLbl = new JLabel("Destino: " + (sinCuad ? "Sin cuadrante" : cuadrante));
            cuadLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
            cuadLbl.setForeground(sinCuad ? DANGER : TEXT2);
            cuadLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(cuadLbl);

            // Repartidor
            if (asignado) {
                card.add(Box.createVerticalStrut(2));
                JLabel repLbl = new JLabel("Repartidor: " + repartidor);
                repLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
                repLbl.setForeground(BLUE);
                repLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
                card.add(repLbl);
            }

            card.add(Box.createVerticalStrut(8));

            // Botón — igual que KitchenView: MAX_WIDTH libre, altura fija 28
            if (esListo && !asignado && onAsignar != null) {
                JButton btn = actionBtn("Asignar repartidor", ORANGE, Color.WHITE);
                btn.addActionListener(e -> onAsignar.run());
                card.add(btn);
            } else if (esListo && asignado && onAccion != null) {
                JButton btn = actionBtn("Iniciar entrega", GREEN, Color.WHITE);
                btn.addActionListener(e -> onAccion.run());
                card.add(btn);
            } else if (enCamino && onAccion != null) {
                JButton btn = actionBtn("Marcar como entregado", BLUE, Color.WHITE);
                btn.addActionListener(e -> onAccion.run());
                card.add(btn);
            }

            card.add(Box.createVerticalStrut(4));

            assignedOrdersBody.add(card);
            assignedOrdersBody.add(Box.createVerticalStrut(8));
            assignedOrdersBody.revalidate();
            assignedOrdersBody.repaint();
        });
    }
    // ── Panel derecho ─────────────────────────────────────────────────────────

    private JPanel buildMapAndRoute() {
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBackground(BG2);

        JPanel mapHeader = new JPanel(new BorderLayout());
        mapHeader.setOpaque(false);
        mapHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel mapLbl = new JLabel("CUADRANTES DE ENTREGA");
        mapLbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        mapLbl.setForeground(TEXT2);
        calcRouteBtn = new JButton("Calcular ruta óptima");
        calcRouteBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        calcRouteBtn.setBackground(BLUE);
        calcRouteBtn.setForeground(Color.WHITE);
        calcRouteBtn.setOpaque(true);
        calcRouteBtn.setBorderPainted(false);
        calcRouteBtn.setFocusPainted(false);
        calcRouteBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        mapHeader.add(mapLbl,       BorderLayout.WEST);
        mapHeader.add(calcRouteBtn, BorderLayout.EAST);
        mapHeader.setAlignmentX(LEFT_ALIGNMENT);
        right.add(mapHeader);
        right.add(Box.createVerticalStrut(8));

        cuadrantesGrid = new JPanel(new WrapLayout(FlowLayout.LEFT, 8, 8));
        cuadrantesGrid.setOpaque(false);
        cuadrantesGrid.setAlignmentX(LEFT_ALIGNMENT);
        cuadrantesGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        right.add(cuadrantesGrid);
        right.add(Box.createVerticalStrut(10));

        JLabel graphLbl = new JLabel("GRAFO DE CUADRANTES");
        graphLbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        graphLbl.setForeground(TEXT2);
        graphLbl.setAlignmentX(LEFT_ALIGNMENT);
        right.add(graphLbl);
        right.add(Box.createVerticalStrut(4));

        graphPanel = new GraphPanel();
        graphPanel.setAlignmentX(LEFT_ALIGNMENT);
        graphPanel.setPreferredSize(new Dimension(600, 580));
        graphPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 580));
        right.add(graphPanel);
        right.add(Box.createVerticalStrut(12));

        JLabel routeLbl = new JLabel("RUTA OPTIMIZADA (Dijkstra desde UPB)");
        routeLbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        routeLbl.setForeground(TEXT2);
        routeLbl.setAlignmentX(LEFT_ALIGNMENT);
        right.add(routeLbl);
        right.add(Box.createVerticalStrut(6));

        routeBody = new JPanel();
        routeBody.setLayout(new BoxLayout(routeBody, BoxLayout.Y_AXIS));
        routeBody.setBackground(BG);
        routeBody.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_C), new EmptyBorder(10, 12, 10, 12)));
        routeBody.setAlignmentX(LEFT_ALIGNMENT);
        routeBody.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        JLabel empty = new JLabel("Presiona \"Calcular ruta óptima\" para ver la ruta.");
        empty.setFont(new Font("SansSerif", Font.PLAIN, 12));
        empty.setForeground(TEXT2);
        routeBody.add(empty);
        right.add(routeBody);
        right.add(Box.createVerticalGlue());
        return right;
    }

    // ── Status bar ────────────────────────────────────────────────────────────

    private JPanel buildStatus() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG);
        bar.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, BORDER_C),
                new EmptyBorder(5, 14, 5, 14)));
        statusLabel = new JLabel("Listo");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusLabel.setForeground(TEXT2);
        bar.add(statusLabel, BorderLayout.WEST);
        return bar;
    }

    // ── API pública ───────────────────────────────────────────────────────────

    public void setCuadrantes(LinkedList<Cuadrante> cuadrantes) {
        SwingUtilities.invokeLater(() -> {
            cuadrantesGrid.removeAll();
            cuadranteCells.clear();
            graphPanel.clearAll();
            if (cuadrantes == null) { cuadrantesGrid.revalidate(); return; }
            edu.fsadriann.model.iterator.Iterator<Cuadrante> it = cuadrantes.iterator();
            while (it.hasNext()) {
                Cuadrante c = it.next();
                if (c != null) {
                    JPanel cell = buildCuadranteCell(c.getNombre(),
                            String.format("%.1f km", c.getDistanciaDesdeUPB()), 0, false);
                    cuadranteCells.put(c.getNombre(), cell);
                    cuadrantesGrid.add(cell);
                    graphPanel.addNode(c.getNombre(), c.getDistanciaDesdeUPB());
                }
            }
            cuadrantesGrid.revalidate();
            cuadrantesGrid.repaint();
            graphPanel.repaint();
        });
    }

    public void addGraphEdge(String from, String to, double distKm) {
        SwingUtilities.invokeLater(() -> graphPanel.addEdge(from, to, distKm));
    }

    public void setCuadranteConPedidos(String nombre, int numeroPedidos) {
        SwingUtilities.invokeLater(() -> {
            JPanel oldCell = cuadranteCells.get(nombre);
            if (oldCell != null) {
                Container parent = oldCell.getParent();
                if (parent != null) {
                    int pos = -1;
                    for (int i = 0; i < parent.getComponentCount(); i++) {
                        if (parent.getComponent(i) == oldCell) { pos = i; break; }
                    }
                    if (pos >= 0) {
                        String dist = "—";
                        for (Component comp : oldCell.getComponents()) {
                            if (comp instanceof JLabel) {
                                String txt = ((JLabel) comp).getText();
                                if (txt != null && txt.contains("km")) { dist = txt; break; }
                            }
                        }
                        JPanel newCell = buildCuadranteCell(nombre, dist,
                                numeroPedidos, numeroPedidos > 0);
                        cuadranteCells.put(nombre, newCell);
                        parent.remove(pos);
                        parent.add(newCell, pos);
                        parent.revalidate();
                        parent.repaint();
                    }
                }
            }
            graphPanel.setNodeOrders(nombre, numeroPedidos);
        });
    }

    /**
     * Agrega una tarjeta de pedido con botones según el estado.
     *
     * LISTO sin repartidor  → botón "Asignar repartidor"
     * LISTO con repartidor  → botón "Iniciar entrega"
     * EN_CAMINO             → botón "Marcar entregado"
     */


    public void clearOrders() {
        SwingUtilities.invokeLater(() -> {
            assignedOrdersBody.removeAll();
            assignedOrdersBody.revalidate();
            assignedOrdersBody.repaint();
        });
    }

    public void setRuta(LinkedList<String> ruta, double distanciaTotal) {
        SwingUtilities.invokeLater(() -> {
            routeBody.removeAll();
            if (ruta == null) {
                JLabel noRuta = new JLabel("No se pudo calcular la ruta.");
                noRuta.setFont(new Font("SansSerif", Font.PLAIN, 12));
                noRuta.setForeground(DANGER);
                routeBody.add(noRuta);
                graphPanel.clearRoute();
                routeBody.revalidate();
                routeBody.repaint();
                return;
            }
            java.util.List<String> routeList = new java.util.ArrayList<>();
            edu.fsadriann.model.iterator.Iterator<String> itG = ruta.iterator();
            while (itG.hasNext()) { String n = itG.next(); if (n != null) routeList.add(n); }
            graphPanel.setRoute(routeList);

            JLabel header = new JLabel(String.format("Distancia total: %.2f km", distanciaTotal));
            header.setFont(new Font("SansSerif", Font.BOLD, 12));
            header.setForeground(BLUE);
            routeBody.add(header);
            routeBody.add(Box.createVerticalStrut(8));

            int[] step = {1};
            edu.fsadriann.model.iterator.Iterator<String> it = ruta.iterator();
            while (it.hasNext()) {
                String nodo = it.next();
                if (nodo == null) continue;
                JPanel row = new JPanel(new BorderLayout(8, 0));
                row.setOpaque(false);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
                row.setAlignmentX(LEFT_ALIGNMENT);
                JLabel stopLbl = new JLabel(step[0] + ".  " + nodo);
                stopLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
                stopLbl.setForeground("UPB".equals(nodo) ? BLUE : TEXT);
                row.add(stopLbl, BorderLayout.WEST);
                routeBody.add(row);
                routeBody.add(Box.createVerticalStrut(4));
                step[0]++;
            }
            routeBody.revalidate();
            routeBody.repaint();
        });
    }

    public void setMetrics(int listos, int enTransito, int entregados, int pendientes) {
        SwingUtilities.invokeLater(() -> {
            metricAssigned.setText(String.valueOf(listos));
            metricTransit.setText(String.valueOf(enTransito));
            metricDelivered.setText(String.valueOf(entregados));
            metricPending.setText(String.valueOf(pendientes));
        });
    }

    // ── Diálogos ──────────────────────────────────────────────────────────────

    public String promptRepartidor() {
        return JOptionPane.showInputDialog(
                this, "Nombre o ID del repartidor:",
                "Asignar repartidor", JOptionPane.PLAIN_MESSAGE);
    }

    public boolean confirm(String msg) {
        return JOptionPane.showConfirmDialog(
                this, msg, "Confirmar",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ── Listeners ─────────────────────────────────────────────────────────────

    public void addLogoutListener(Runnable r)    { logoutBtn.addActionListener(e -> r.run()); }
    public void addCalcRouteListener(Runnable r) { calcRouteBtn.addActionListener(e -> r.run()); }
    public void addRefreshListener(Runnable r)   { refreshBtn.addActionListener(e -> r.run()); }
    public void setMessage(String msg)           { SwingUtilities.invokeLater(() -> statusLabel.setText(msg)); }
    public void showView()   { setVisible(true); toFront(); }
    public void hideView()   { setVisible(false); }
    public JFrame getFrame() { return frame; }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private JButton actionBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28)); // igual que KitchenView
        return btn;
    }

    private JPanel buildCuadranteCell(String nombre, String distancia,
                                      int pedidos, boolean activo) {
        boolean esOrigen = "UPB".equals(nombre);
        JPanel cell = new JPanel();
        cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
        cell.setPreferredSize(new Dimension(160, 80));
        cell.setBackground(esOrigen ? BLUE_BG : (activo ? new Color(235, 250, 240) : BG));
        Color bordeColor = esOrigen ? BLUE : (activo ? GREEN : BORDER_C);
        cell.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 4, 0, 0, bordeColor),
                BorderFactory.createCompoundBorder(
                        new LineBorder(BORDER_C, 1),
                        new EmptyBorder(8, 10, 8, 10))));
        JLabel nameLbl = new JLabel(nombre);
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        nameLbl.setForeground(esOrigen ? BLUE : TEXT);
        JLabel distLbl = new JLabel(esOrigen ? "Origen" : distancia);
        distLbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
        distLbl.setForeground(TEXT3);
        JLabel statusLbl;
        if (esOrigen) {
            statusLbl = new JLabel("Punto de partida");
            statusLbl.setForeground(BLUE);
        } else if (activo) {
            statusLbl = new JLabel(pedidos + " pedido(s)");
            statusLbl.setForeground(GREEN);
        } else {
            statusLbl = new JLabel("Sin pedidos");
            statusLbl.setForeground(TEXT3);
        }
        statusLbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        cell.add(nameLbl);
        cell.add(Box.createVerticalStrut(2));
        cell.add(distLbl);
        cell.add(Box.createVerticalStrut(4));
        cell.add(statusLbl);
        return cell;
    }

    private String shortId(String id) {
        if (id == null || id.isBlank()) return "—";
        return id.length() > 8 ? id.substring(0, 8) + "..." : id;
    }

    // ── WrapLayout ────────────────────────────────────────────────────────────

    private static class WrapLayout extends FlowLayout {
        public WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }

        @Override public Dimension preferredLayoutSize(Container target) { return layoutSize(target, true); }
        @Override public Dimension minimumLayoutSize(Container target)   { return layoutSize(target, false); }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int width = target.getWidth();
                if (width == 0) width = Integer.MAX_VALUE;
                int x = getHgap(), y = getVgap(), rowH = 0, start = x;
                Insets insets = target.getInsets();
                width -= insets.left + insets.right + getHgap() * 2;
                for (Component c : target.getComponents()) {
                    if (!c.isVisible()) continue;
                    Dimension d = preferred ? c.getPreferredSize() : c.getMinimumSize();
                    if (x + d.width > width && x != start) { y += rowH + getVgap(); rowH = 0; x = start; }
                    x += d.width + getHgap();
                    rowH = Math.max(rowH, d.height);
                }
                y += rowH + getVgap() + insets.top + insets.bottom;
                return new Dimension(width, y);
            }
        }
    }

    private static class GraphPanel extends JPanel {

        private static final Color COL_BG_PANEL = Color.WHITE;
        private static final int   R            = 22;

        private final java.util.List<GraphNode>        nodes         = new java.util.ArrayList<>();
        private final java.util.List<GraphEdge>        edges         = new java.util.ArrayList<>();
        private final java.util.Map<String, GraphNode> nodeMap       = new java.util.HashMap<>();
        private final java.util.Set<String>            routeEdgeKeys = new java.util.HashSet<>();
        private final java.util.List<String>           routeOrder    = new java.util.ArrayList<>();

        GraphPanel() {
            setBackground(COL_BG_PANEL);
            setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(220, 220, 220)),
                    new EmptyBorder(8, 8, 20, 8)));
        }

        void clearAll()   { nodes.clear(); edges.clear(); nodeMap.clear(); routeEdgeKeys.clear(); routeOrder.clear(); repaint(); }
        void clearRoute() { routeEdgeKeys.clear(); routeOrder.clear(); repaint(); }

        void addNode(String id, double distFromUPB) {
            if (nodeMap.containsKey(id)) return;
            GraphNode n = new GraphNode(id, distFromUPB, "UPB".equals(id));
            nodes.add(n);
            nodeMap.put(id, n);
        }

        void addEdge(String from, String to, double distKm) {
            if (!nodeMap.containsKey(from) || !nodeMap.containsKey(to)) return;
            // Evitar aristas duplicadas (el grafo no dirigido agrega A→B y B→A)
            String key = edgeKey(from, to);
            for (GraphEdge e : edges) {
                if (edgeKey(e.from, e.to).equals(key)) return;
            }
            edges.add(new GraphEdge(from, to, distKm));
            repaint();
        }

        void setNodeOrders(String id, int orders) {
            GraphNode n = nodeMap.get(id);
            if (n != null) { n.orders = orders; repaint(); }
        }

        void setRoute(java.util.List<String> route) {
            routeEdgeKeys.clear();
            routeOrder.clear();
            routeOrder.addAll(route);
            for (int i = 0; i < route.size() - 1; i++)
                routeEdgeKeys.add(edgeKey(route.get(i), route.get(i + 1)));
            repaint();
        }

        // ── Coordenadas geográficas ───────────────────────────────────────────────
        // 4 niveles jerárquicos por distancia desde UPB:
        //   Nivel 0 (y≈0.08): UPB
        //   Nivel 1 (y≈0.30): Centro, Provenza, SanFrancisco
        //   Nivel 2 (y≈0.55): Girón, Lagos, Cabecera, Mejoras
        //   Nivel 3 (y≈0.78): Floridablanca, Piedecuesta

        private static final java.util.Map<String, double[]> GEO_COORDS;
        static {
            GEO_COORDS = new java.util.HashMap<>();
            GEO_COORDS.put("UPB",           new double[]{ 0.50, 0.04 });
            GEO_COORDS.put("Centro",        new double[]{ 0.13, 0.30 });
            GEO_COORDS.put("Provenza",      new double[]{ 0.36, 0.30 });
            GEO_COORDS.put("SanFrancisco",  new double[]{ 0.66, 0.30 });
            GEO_COORDS.put("Giron",         new double[]{ 0.12, 0.62 });
            GEO_COORDS.put("Lagos",         new double[]{ 0.35, 0.62 });
            GEO_COORDS.put("Cabecera",      new double[]{ 0.57, 0.62 });
            GEO_COORDS.put("Mejoras",       new double[]{ 0.80, 0.62 });
            GEO_COORDS.put("Floridablanca", new double[]{ 0.39, 0.90 });
            GEO_COORDS.put("Piedecuesta",   new double[]{ 0.72, 0.90 });
            GEO_COORDS.put("San Francisco", GEO_COORDS.get("SanFrancisco"));
        }

        private void computeLayout(int w, int h) {
            final int PAD  = 50;
            final int LEGH = 28;
            int drawW = w - PAD * 2;
            int drawH = h - PAD * 2 - LEGH;

            for (GraphNode n : nodes) {
                String key = normalize(n.id);
                double[] pos = GEO_COORDS.get(key);
                if (pos != null) {
                    n.px = PAD + (int)(pos[0] * drawW);
                    n.py = PAD + (int)(pos[1] * drawH);
                } else {
                    n.px = PAD + drawW / 2;
                    n.py = PAD + drawH / 2;
                }
            }
            resolveOverlaps();
        }

        private void resolveOverlaps() {
            final int MIN_DIST = 52, ITERS = 80;
            for (int iter = 0; iter < ITERS; iter++) {
                boolean moved = false;
                for (int i = 0; i < nodes.size(); i++) {
                    for (int j = i + 1; j < nodes.size(); j++) {
                        GraphNode a = nodes.get(i), b = nodes.get(j);
                        int dx = b.px - a.px, dy = b.py - a.py;
                        double dist = Math.sqrt(dx * dx + dy * dy);
                        if (dist < MIN_DIST && dist > 0) {
                            double push = (MIN_DIST - dist) / 2.0 + 1;
                            int ax = (int)(push * dx / dist), ay = (int)(push * dy / dist);
                            a.px -= ax; a.py -= ay; b.px += ax; b.py += ay;
                            moved = true;
                        }
                    }
                }
                if (!moved) break;
            }
        }

        private static String normalize(String id) {
            if (id == null) return "";
            if (GEO_COORDS.containsKey(id)) return id;
            for (String key : GEO_COORDS.keySet())
                if (key.equalsIgnoreCase(id)) return key;
            for (String key : GEO_COORDS.keySet())
                if (key.toLowerCase().startsWith(id.toLowerCase())
                        || id.toLowerCase().startsWith(key.toLowerCase())) return key;
            return id;
        }

        // ── Pintura ───────────────────────────────────────────────────────────────

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,    RenderingHints.VALUE_STROKE_PURE);

            if (nodes.isEmpty()) {
                g.setFont(new Font("SansSerif", Font.PLAIN, 12));
                g.setColor(new Color(180, 180, 180));
                String msg = "Sin cuadrantes cargados";
                FontMetrics fm = g.getFontMetrics();
                g.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
                return;
            }

            Insets ins = getInsets();
            int w = getWidth()  - ins.left - ins.right;
            int h = getHeight() - ins.top  - ins.bottom;
            g.translate(ins.left, ins.top);
            computeLayout(w, h);
            drawEdges(g);
            drawNodes(g);
            drawLegend(g, w, h);
            g.translate(-ins.left, -ins.top);
        }

        private void drawEdges(Graphics2D g) {
            // Set para no dibujar la misma arista dos veces (grafo no dirigido)
            java.util.Set<String> drawn = new java.util.HashSet<>();

            for (GraphEdge e : edges) {
                String key = edgeKey(e.from, e.to);
                if (!drawn.add(key)) continue; // ya dibujada

                GraphNode a = nodeMap.get(e.from), b = nodeMap.get(e.to);
                if (a == null || b == null) continue;

                boolean enRuta = routeEdgeKeys.contains(key);

                double dx  = b.px - a.px, dy = b.py - a.py;
                double len = Math.sqrt(dx * dx + dy * dy);
                if (len == 0) continue;

                // La línea va de borde a borde del círculo (sin punta de flecha)
                int x1 = (int)(a.px + dx / len * R);
                int y1 = (int)(a.py + dy / len * R);
                int x2 = (int)(b.px - dx / len * R);
                int y2 = (int)(b.py - dy / len * R);

                g.setColor(enRuta ? new Color(40, 167, 69) : new Color(190, 190, 190));
                g.setStroke(new BasicStroke(enRuta ? 2.2f : 1.2f,
                        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawLine(x1, y1, x2, y2);
                g.setStroke(new BasicStroke(1f));

                // Etiqueta de distancia perpendicular a la línea
                int mx = (x1 + x2) / 2, my = (y1 + y2) / 2;
                String dist = String.format("%.1f km", e.distKm);
                g.setFont(new Font("SansSerif", Font.PLAIN, 10));
                FontMetrics fm = g.getFontMetrics();
                double perpX = -dy / len, perpY = dx / len;
                int lx = mx + (int)(perpX * 13) - fm.stringWidth(dist) / 2;
                int ly = my + (int)(perpY * 13) + fm.getAscent() / 2;
                g.setColor(enRuta ? new Color(21, 87, 36) : new Color(155, 155, 155));
                g.drawString(dist, lx, ly);
            }
        }

        private void drawNodes(Graphics2D g) {
            for (GraphNode n : nodes) {
                boolean enRuta = routeOrder.contains(n.id);

                Color fillColor   = n.origin ? new Color(230, 241, 251)
                        : enRuta           ? new Color(212, 237, 218)
                        : new Color(241, 239, 232);
                Color strokeColor = n.origin ? new Color(24, 95, 165)
                        : enRuta           ? new Color(40, 167, 69)
                        : new Color(180, 180, 180);

                // Sombra suave
                g.setColor(new Color(0, 0, 0, 18));
                g.fillOval(n.px - R + 2, n.py - R + 2, R * 2, R * 2);

                g.setColor(fillColor);
                g.fillOval(n.px - R, n.py - R, R * 2, R * 2);
                g.setColor(strokeColor);
                g.setStroke(new BasicStroke(n.origin || enRuta ? 2.2f : 1.5f));
                g.drawOval(n.px - R, n.py - R, R * 2, R * 2);
                g.setStroke(new BasicStroke(1f));

                // Etiqueta — acortar solo si no cabe
                String label = n.id.length() > 9 ? n.id.substring(0, 8) + "." : n.id;
                g.setFont(new Font("SansSerif",
                        (n.origin || enRuta) ? Font.BOLD : Font.PLAIN, 11));
                FontMetrics fm = g.getFontMetrics();
                g.setColor(n.origin ? new Color(12, 68, 124)
                        : enRuta   ? new Color(21, 87, 36)
                        : new Color(90, 90, 90));
                g.drawString(label,
                        n.px - fm.stringWidth(label) / 2,
                        n.py + fm.getAscent() / 2 - 1);
            }
        }

        private void drawLegend(Graphics2D g, int w, int h) {
            Object[][] items = {
                    { new Color(230, 241, 251), new Color(24,  95, 165), "Origen (UPB)" },
                    { new Color(241, 239, 232), new Color(180, 180, 180), "Sin pedidos"  },
                    { new Color(212, 237, 218), new Color(40,  167,  69), "Ruta óptima"  },
            };
            g.setFont(new Font("SansSerif", Font.PLAIN, 10));
            FontMetrics fm = g.getFontMetrics();
            int itemW  = 130;
            int startX = (w - itemW * items.length) / 2;
            int ly     = h - 10;
            for (int i = 0; i < items.length; i++) {
                Color  fill   = (Color)  items[i][0];
                Color  stroke = (Color)  items[i][1];
                String label  = (String) items[i][2];
                int cx = startX + i * itemW + 8;
                g.setColor(fill);
                g.fillOval(cx - 7, ly - 7, 14, 14);
                g.setColor(stroke);
                g.setStroke(new BasicStroke(1.5f));
                g.drawOval(cx - 7, ly - 7, 14, 14);
                g.setStroke(new BasicStroke(1f));
                g.setColor(new Color(90, 90, 90));
                g.drawString(label, cx + 10, ly + 4);
            }
        }

        // ── Helpers ───────────────────────────────────────────────────────────────

        private static String edgeKey(String a, String b) {
            return a.compareTo(b) < 0 ? a + "§" + b : b + "§" + a;
        }

        // ── Clases internas ───────────────────────────────────────────────────────

        private static class GraphNode {
            String id; double distFromUPB; boolean origin; int px, py, orders;
            GraphNode(String id, double d, boolean o) { this.id = id; distFromUPB = d; origin = o; }
        }

        private static class GraphEdge {
            String from, to; double distKm;
            GraphEdge(String f, String t, double d) { from = f; to = t; distKm = d; }
        }
    }
}