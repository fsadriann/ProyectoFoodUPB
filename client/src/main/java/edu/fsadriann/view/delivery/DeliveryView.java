package edu.fsadriann.view.delivery;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class DeliveryView extends JFrame {

    private static final Color BG       = Color.WHITE;
    private static final Color BG2      = new Color(245, 245, 245);
    private static final Color BLUE     = new Color(24, 95, 165);
    private static final Color BLUE_BG  = new Color(230, 241, 251);
    private static final Color TEXT     = new Color(30, 30, 30);
    private static final Color TEXT2    = new Color(100, 100, 100);
    private static final Color BORDER_C  = new Color(220, 220, 220);

    private static final String[] QUADRANT_NAMES  = {"A", "B", "C", "D", "E", "F"};
    private static final String[] QUADRANT_LABELS = {
        "Centro", "Laureles", "Floridablanca", "Girón", "Piedecuesta", "Sin asignar"
    };

    private final JFrame frame;
    private JButton logoutBtn;
    private JLabel  statusLabel;

    private JLabel metricAssigned, metricTransit, metricDelivered, metricFailed;
    private JPanel assignedOrdersBody;
    private JPanel[] quadrantCells;
    private JPanel routeBody;

    public DeliveryView(String userLabel) {
        super("Food UPB — Entregas");
        frame = this;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 650));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG2);
        root.add(buildHeader(),  BorderLayout.NORTH);
        root.add(buildContent(), BorderLayout.CENTER);
        root.add(buildStatus(),  BorderLayout.SOUTH);
        setContentPane(root);
        pack();
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(BG);
        h.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, BORDER_C),
            new EmptyBorder(10, 16, 10, 16)
        ));
        JLabel title = new JLabel("Food UPB — Entregas");
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        title.setForeground(BLUE);

        logoutBtn = new JButton("Cerrar sesión");
        logoutBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        h.add(title,     BorderLayout.WEST);
        h.add(logoutBtn, BorderLayout.EAST);
        return h;
    }

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
        metricFailed    = new JLabel("—");

        row.add(metricTile("Asignados",       metricAssigned));
        row.add(metricTile("En tránsito",     metricTransit));
        row.add(metricTile("Entregados hoy",  metricDelivered));
        row.add(metricTile("No entregados",   metricFailed));
        return row;
    }

    private JPanel metricTile(String label, JLabel val) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_C), new EmptyBorder(10, 12, 10, 12)
        ));
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
        body.add(buildAssignedPanel(), BorderLayout.WEST);
        body.add(buildMapAndRoute(),   BorderLayout.CENTER);
        return body;
    }

    // ── Left: assigned orders ─────────────────────────────────────────────────

    private JScrollPane buildAssignedPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG);

        JLabel lbl = new JLabel("PEDIDOS ASIGNADOS");
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl.setForeground(TEXT2);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(8));

        assignedOrdersBody = new JPanel();
        assignedOrdersBody.setLayout(new BoxLayout(assignedOrdersBody, BoxLayout.Y_AXIS));
        assignedOrdersBody.setBackground(BG);
        assignedOrdersBody.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(assignedOrdersBody);
        panel.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(new LineBorder(BORDER_C));
        scroll.getViewport().setBackground(BG);
        scroll.setPreferredSize(new Dimension(300, 0));
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    // ── Right: quadrant map + route ───────────────────────────────────────────

    private JPanel buildMapAndRoute() {
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBackground(BG2);

        JLabel mapLbl = new JLabel("MAPA DE CUADRANTES");
        mapLbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        mapLbl.setForeground(TEXT2);
        mapLbl.setAlignmentX(LEFT_ALIGNMENT);
        right.add(mapLbl);
        right.add(Box.createVerticalStrut(6));
        right.add(buildQuadrantGrid());
        right.add(Box.createVerticalStrut(14));

        JLabel routeLbl = new JLabel("RUTA OPTIMIZADA");
        routeLbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        routeLbl.setForeground(TEXT2);
        routeLbl.setAlignmentX(LEFT_ALIGNMENT);
        right.add(routeLbl);
        right.add(Box.createVerticalStrut(6));

        routeBody = new JPanel();
        routeBody.setLayout(new BoxLayout(routeBody, BoxLayout.Y_AXIS));
        routeBody.setBackground(BG);
        routeBody.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_C), new EmptyBorder(10, 12, 10, 12)
        ));
        routeBody.setAlignmentX(LEFT_ALIGNMENT);
        routeBody.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        JLabel empty = new JLabel("Sin ruta calculada.");
        empty.setFont(new Font("SansSerif", Font.PLAIN, 12));
        empty.setForeground(TEXT2);
        routeBody.add(empty);
        right.add(routeBody);
        right.add(Box.createVerticalGlue());
        return right;
    }

    private JPanel buildQuadrantGrid() {
        JPanel grid = new JPanel(new GridLayout(2, 3, 8, 8));
        grid.setOpaque(false);
        grid.setAlignmentX(LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        quadrantCells = new JPanel[6];
        for (int i = 0; i < 6; i++) {
            quadrantCells[i] = buildCell(i, false);
            grid.add(quadrantCells[i]);
        }
        return grid;
    }

    private JPanel buildCell(int index, boolean hasOrders) {
        JPanel cell = new JPanel();
        cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
        cell.setBackground(hasOrders ? BLUE_BG : BG2);
        cell.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(hasOrders ? BLUE : BORDER_C),
            new EmptyBorder(8, 10, 8, 10)
        ));

        JLabel nameLbl = new JLabel("Cuadrante " + QUADRANT_NAMES[index]);
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        nameLbl.setForeground(hasOrders ? BLUE : TEXT);
        nameLbl.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subLbl = new JLabel(QUADRANT_LABELS[index]);
        subLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subLbl.setForeground(TEXT2);
        subLbl.setAlignmentX(CENTER_ALIGNMENT);

        JLabel statusLbl = new JLabel(hasOrders ? "Pedidos activos" : "Sin pedidos");
        statusLbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        statusLbl.setForeground(hasOrders ? BLUE : TEXT2);
        statusLbl.setAlignmentX(CENTER_ALIGNMENT);

        cell.add(Box.createVerticalGlue());
        cell.add(nameLbl);
        cell.add(Box.createVerticalStrut(2));
        cell.add(subLbl);
        cell.add(Box.createVerticalStrut(4));
        cell.add(statusLbl);
        cell.add(Box.createVerticalGlue());
        return cell;
    }

    private JPanel buildStatus() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG2);
        bar.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(1, 0, 0, 0, BORDER_C),
            new EmptyBorder(5, 14, 5, 14)
        ));
        statusLabel = new JLabel("Listo");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusLabel.setForeground(TEXT2);
        bar.add(statusLabel, BorderLayout.WEST);
        return bar;
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    public void addAssignedOrder(String orderId, String clientName, String address, String quadrant) {
        SwingUtilities.invokeLater(() -> {
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(BG);
            card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_C), new EmptyBorder(8, 10, 8, 10)
            ));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
            card.setAlignmentX(LEFT_ALIGNMENT);

            JPanel top = new JPanel(new BorderLayout(4, 0));
            top.setOpaque(false);
            JLabel id = new JLabel(orderId + " — " + clientName);
            id.setFont(new Font("SansSerif", Font.BOLD, 12));
            id.setForeground(TEXT);
            JLabel q = new JLabel("Cuad. " + quadrant);
            q.setFont(new Font("SansSerif", Font.BOLD, 10));
            q.setBackground(BLUE_BG);
            q.setForeground(BLUE);
            q.setOpaque(true);
            q.setBorder(new EmptyBorder(1, 4, 1, 4));
            top.add(id, BorderLayout.WEST);
            top.add(q,  BorderLayout.EAST);

            JLabel addr = new JLabel(address);
            addr.setFont(new Font("SansSerif", Font.PLAIN, 11));
            addr.setForeground(TEXT2);
            addr.setAlignmentX(LEFT_ALIGNMENT);

            card.add(top);
            card.add(Box.createVerticalStrut(4));
            card.add(addr);

            assignedOrdersBody.add(card);
            assignedOrdersBody.add(Box.createVerticalStrut(6));
            assignedOrdersBody.revalidate();
            assignedOrdersBody.repaint();
        });
    }

    public void setQuadrantActive(int index, boolean active) {
        if (index < 0 || index >= quadrantCells.length) return;
        SwingUtilities.invokeLater(() -> {
            JPanel parent = (JPanel) quadrantCells[index].getParent();
            if (parent == null) return;
            int pos = -1;
            for (int i = 0; i < parent.getComponentCount(); i++) {
                if (parent.getComponent(i) == quadrantCells[index]) { pos = i; break; }
            }
            if (pos < 0) return;
            quadrantCells[index] = buildCell(index, active);
            parent.remove(pos);
            parent.add(quadrantCells[index], pos);
            parent.revalidate();
            parent.repaint();
        });
    }

    public void addRouteStop(int stopNumber, String destination, String orderIds) {
        SwingUtilities.invokeLater(() -> {
            if (routeBody.getComponentCount() == 1
                    && routeBody.getComponent(0) instanceof JLabel
                    && ((JLabel) routeBody.getComponent(0)).getText().startsWith("Sin ruta")) {
                routeBody.removeAll();
            }
            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
            row.setAlignmentX(LEFT_ALIGNMENT);
            JLabel stop = new JLabel(stopNumber + ".  " + destination);
            stop.setFont(new Font("SansSerif", Font.BOLD, 12));
            stop.setForeground(TEXT);
            JLabel ids = new JLabel(orderIds);
            ids.setFont(new Font("SansSerif", Font.PLAIN, 11));
            ids.setForeground(TEXT2);
            row.add(stop, BorderLayout.WEST);
            row.add(ids,  BorderLayout.EAST);
            routeBody.add(row);
            routeBody.add(Box.createVerticalStrut(4));
            routeBody.revalidate();
            routeBody.repaint();
        });
    }

    public void setMetrics(int assigned, int transit, int delivered, int failed) {
        SwingUtilities.invokeLater(() -> {
            metricAssigned.setText(String.valueOf(assigned));
            metricTransit.setText(String.valueOf(transit));
            metricDelivered.setText(String.valueOf(delivered));
            metricFailed.setText(String.valueOf(failed));
        });
    }

    public void addLogoutListener(Runnable action) { logoutBtn.addActionListener(e -> action.run()); }
    public void setMessage(String msg)             { SwingUtilities.invokeLater(() -> statusLabel.setText(msg)); }
    public void showView()   { setVisible(true); toFront(); }
    public void hideView()   { setVisible(false); }
    public JFrame getFrame() { return frame; }
}
