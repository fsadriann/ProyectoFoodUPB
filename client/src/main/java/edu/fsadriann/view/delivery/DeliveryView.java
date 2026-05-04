package edu.fsadriann.view.delivery;

import edu.fsadriann.view.UIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DeliveryView extends JFrame {

    private final JFrame frame;
    private JLabel statusLabel;
    private JButton logoutBtn;

    // ── Metric labels ─────────────────────────────────────────────────────────
    private JLabel metricAssigned;
    private JLabel metricTransit;
    private JLabel metricDelivered;
    private JLabel metricFailed;

    // ── Left column — assigned orders ─────────────────────────────────────────
    private JPanel assignedOrdersBody;

    // ── Right column — quadrant grid cells ───────────────────────────────────
    private JPanel[] quadrantCells;
    private static final String[] QUADRANT_NAMES  = {"A", "B", "C", "D", "E", "F"};
    private static final String[] QUADRANT_LABELS = {
        "Centro", "Laureles", "Floridablanca", "Girón", "Piedecuesta", "Sin asignar"
    };

    // ── Right column — route panel ────────────────────────────────────────────
    private JPanel routeBody;

    public DeliveryView(String userLabel) {
        super("Food UPB — Entrega");
        frame = this;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 700));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIComponents.BG2);
        root.add(buildTopbar(userLabel), BorderLayout.NORTH);
        root.add(buildContent(),         BorderLayout.CENTER);
        root.add(buildStatusBar(),       BorderLayout.SOUTH);
        setContentPane(root);
        pack();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TOP BAR
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildTopbar(String userLabel) {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(UIComponents.BG);
        top.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIComponents.BORDER),
            new EmptyBorder(10, 20, 10, 20)
        ));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        JLabel brand = new JLabel("Food UPB");
        brand.setFont(UIComponents.fontBold(13));
        brand.setForeground(UIComponents.TXT2);
        brand.setBorder(new EmptyBorder(0, 0, 0, 12));
        JPanel sep = new JPanel();
        sep.setOpaque(true);
        sep.setBackground(UIComponents.BORDER);
        sep.setPreferredSize(new Dimension(1, 18));
        JLabel section = new JLabel("Entregas");
        section.setFont(UIComponents.fontBold(14));
        section.setForeground(UIComponents.ACCENT);
        section.setBorder(new EmptyBorder(0, 12, 0, 0));
        left.add(brand);
        left.add(sep);
        left.add(section);

        logoutBtn = UIComponents.roundBtnSmall("Cerrar sesión", UIComponents.BG, UIComponents.TXT, UIComponents.BORDER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(UIComponents.badge("Entregas", UIComponents.INFO_BG, UIComponents.INFO_FG));
        right.add(logoutBtn);

        top.add(left,  BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);
        return top;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CONTENT
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setBorder(new EmptyBorder(16, 18, 16, 18));
        content.setBackground(UIComponents.BG2);
        content.add(buildMetrics(), BorderLayout.NORTH);
        content.add(buildBody(),    BorderLayout.CENTER);
        return content;
    }

    // ── Metrics row ───────────────────────────────────────────────────────────

    private JPanel buildMetrics() {
        JPanel row = new JPanel(new GridLayout(1, 4, 10, 0));
        row.setOpaque(false);

        metricAssigned  = new JLabel("—");
        metricTransit   = new JLabel("—");
        metricDelivered = new JLabel("—");
        metricFailed    = new JLabel("—");

        row.add(metricCard("Asignados",      metricAssigned));
        row.add(metricCard("En tránsito",    metricTransit));
        row.add(metricCard("Entregados hoy", metricDelivered));
        row.add(metricCard("No entregados",  metricFailed));
        return row;
    }

    private JPanel metricCard(String label, JLabel valueLabel) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIComponents.BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(UIComponents.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel lbl = new JLabel(label);
        lbl.setFont(UIComponents.fontPlain(12));
        lbl.setForeground(UIComponents.TXT2);
        valueLabel.setFont(UIComponents.fontBold(28));
        valueLabel.setForeground(UIComponents.TXT);

        card.add(lbl);
        card.add(Box.createVerticalStrut(4));
        card.add(valueLabel);
        return card;
    }

    // ── Body: left (assigned orders) + right (map + route) ───────────────────

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
        panel.setBackground(UIComponents.BG2);

        JLabel title = secLabel("Pedidos asignados");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(8));

        assignedOrdersBody = new JPanel();
        assignedOrdersBody.setLayout(new BoxLayout(assignedOrdersBody, BoxLayout.Y_AXIS));
        assignedOrdersBody.setBackground(UIComponents.BG2);
        assignedOrdersBody.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(assignedOrdersBody);
        panel.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIComponents.BG2);
        scroll.setPreferredSize(new Dimension(320, 0));
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    // ── Right: quadrant map + route ───────────────────────────────────────────

    private JPanel buildMapAndRoute() {
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);

        // Map section
        JPanel mapSection = new JPanel();
        mapSection.setLayout(new BoxLayout(mapSection, BoxLayout.Y_AXIS));
        mapSection.setOpaque(false);
        mapSection.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel mapTitle = secLabel("Mapa de cuadrantes");
        mapTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        mapSection.add(mapTitle);
        mapSection.add(Box.createVerticalStrut(8));
        mapSection.add(buildQuadrantGrid());

        // Route section
        JPanel routeSection = new JPanel();
        routeSection.setLayout(new BoxLayout(routeSection, BoxLayout.Y_AXIS));
        routeSection.setOpaque(false);
        routeSection.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel routeTitle = secLabel("Ruta optimizada");
        routeTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        routeSection.add(routeTitle);
        routeSection.add(Box.createVerticalStrut(8));

        routeBody = new JPanel();
        routeBody.setLayout(new BoxLayout(routeBody, BoxLayout.Y_AXIS));
        routeBody.setBackground(UIComponents.BG);
        routeBody.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 1, 1, 1, UIComponents.BORDER),
            new EmptyBorder(12, 14, 12, 14)
        ));
        routeBody.setAlignmentX(Component.LEFT_ALIGNMENT);
        addEmptyRoute();
        routeSection.add(routeBody);

        right.add(mapSection);
        right.add(Box.createVerticalStrut(14));
        right.add(routeSection);
        right.add(Box.createVerticalGlue());
        return right;
    }

    private JPanel buildQuadrantGrid() {
        JPanel grid = new JPanel(new GridLayout(2, 3, 8, 8));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        quadrantCells = new JPanel[6];
        for (int i = 0; i < 6; i++) {
            quadrantCells[i] = buildQuadrantCell(QUADRANT_NAMES[i], QUADRANT_LABELS[i], false);
            grid.add(quadrantCells[i]);
        }
        return grid;
    }

    private JPanel buildQuadrantCell(String name, String label, boolean hasOrders) {
        JPanel cell = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = hasOrders ? UIComponents.INFO_BG : UIComponents.BG2;
                Color border = hasOrders ? UIComponents.INFO : UIComponents.BORDER;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        cell.setOpaque(false);
        cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
        cell.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel nameLbl = new JLabel("Cuadrante " + name);
        nameLbl.setFont(UIComponents.fontBold(12));
        nameLbl.setForeground(hasOrders ? UIComponents.INFO_FG : UIComponents.TXT);
        nameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLbl = new JLabel(label);
        subLbl.setFont(UIComponents.fontPlain(11));
        subLbl.setForeground(UIComponents.TXT3);
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel statusLbl = new JLabel(hasOrders ? "Pedidos activos" : "Sin pedidos");
        statusLbl.setFont(UIComponents.fontBold(10));
        statusLbl.setForeground(hasOrders ? UIComponents.INFO_FG : UIComponents.TXT3);
        statusLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        cell.add(Box.createVerticalGlue());
        cell.add(nameLbl);
        cell.add(Box.createVerticalStrut(2));
        cell.add(subLbl);
        cell.add(Box.createVerticalStrut(4));
        cell.add(statusLbl);
        cell.add(Box.createVerticalGlue());
        return cell;
    }

    private void addEmptyRoute() {
        JLabel lbl = new JLabel("Sin ruta calculada");
        lbl.setFont(UIComponents.fontPlain(12));
        lbl.setForeground(UIComponents.TXT3);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        routeBody.add(lbl);
    }

    // ── Status bar ────────────────────────────────────────────────────────────

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UIComponents.BG);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, UIComponents.BORDER),
            new EmptyBorder(6, 18, 6, 18)
        ));
        statusLabel = new JLabel("Listo");
        statusLabel.setFont(UIComponents.fontPlain(12));
        statusLabel.setForeground(UIComponents.TXT2);
        bar.add(statusLabel, BorderLayout.WEST);
        return bar;
    }

    private JLabel secLabel(String text) {
        JLabel lbl = new JLabel(text.toUpperCase());
        lbl.setFont(UIComponents.fontBold(10));
        lbl.setForeground(UIComponents.TXT3);
        return lbl;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC API — data binding methods
    // ─────────────────────────────────────────────────────────────────────────

    /** Adds an order card to the assigned orders list. */
    public void addAssignedOrder(String orderId, String clientName, String address, String quadrant) {
        SwingUtilities.invokeLater(() -> {
            JPanel card = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(UIComponents.BG);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setColor(UIComponents.BORDER);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                    g2.dispose();
                }
            };
            card.setOpaque(false);
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(new EmptyBorder(10, 12, 10, 12));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
            card.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel headerRow = new JPanel(new BorderLayout(4, 0));
            headerRow.setOpaque(false);
            JLabel idLbl = new JLabel(orderId + "  →  " + clientName);
            idLbl.setFont(UIComponents.fontBold(12));
            idLbl.setForeground(UIComponents.TXT);
            JLabel qBadge = UIComponents.badge("Cuadrante " + quadrant, UIComponents.INFO_BG, UIComponents.INFO_FG);
            headerRow.add(idLbl,   BorderLayout.WEST);
            headerRow.add(qBadge,  BorderLayout.EAST);

            JLabel addrLbl = new JLabel(address);
            addrLbl.setFont(UIComponents.fontPlain(11));
            addrLbl.setForeground(UIComponents.TXT2);
            addrLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

            card.add(headerRow);
            card.add(Box.createVerticalStrut(4));
            card.add(addrLbl);

            assignedOrdersBody.add(card);
            assignedOrdersBody.add(Box.createVerticalStrut(8));
            assignedOrdersBody.revalidate();
            assignedOrdersBody.repaint();
        });
    }

    /** Marks a quadrant as having active orders. */
    public void setQuadrantActive(int index, boolean active) {
        if (index < 0 || index >= quadrantCells.length) return;
        SwingUtilities.invokeLater(() -> {
            JPanel old = quadrantCells[index];
            JPanel parent = (JPanel) old.getParent();
            int pos = -1;
            for (int i = 0; i < parent.getComponentCount(); i++) {
                if (parent.getComponent(i) == old) { pos = i; break; }
            }
            if (pos < 0) return;
            quadrantCells[index] = buildQuadrantCell(QUADRANT_NAMES[index], QUADRANT_LABELS[index], active);
            parent.remove(pos);
            parent.add(quadrantCells[index], pos);
            parent.revalidate();
            parent.repaint();
        });
    }

    /** Adds a stop to the optimized route display. */
    public void addRouteStop(int stopNumber, String destination, String orderIds) {
        SwingUtilities.invokeLater(() -> {
            if (routeBody.getComponentCount() == 1
                    && routeBody.getComponent(0) instanceof JLabel
                    && ((JLabel) routeBody.getComponent(0)).getText().contains("Sin ruta")) {
                routeBody.removeAll();
            }
            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel stopLbl = new JLabel(stopNumber + ".  " + destination);
            stopLbl.setFont(UIComponents.fontBold(12));
            stopLbl.setForeground(UIComponents.TXT);
            JLabel idLbl = new JLabel(orderIds);
            idLbl.setFont(UIComponents.fontPlain(11));
            idLbl.setForeground(UIComponents.TXT2);
            row.add(stopLbl, BorderLayout.WEST);
            row.add(idLbl,   BorderLayout.EAST);
            routeBody.add(row);
            routeBody.add(Box.createVerticalStrut(4));
            routeBody.revalidate();
            routeBody.repaint();
        });
    }

    /** Updates the four metric counters. */
    public void setMetrics(int assigned, int transit, int deliveredToday, int failed) {
        SwingUtilities.invokeLater(() -> {
            metricAssigned.setText(String.valueOf(assigned));
            metricTransit.setText(String.valueOf(transit));
            metricDelivered.setText(String.valueOf(deliveredToday));
            metricFailed.setText(String.valueOf(failed));
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Existing minimal API (unchanged)
    // ─────────────────────────────────────────────────────────────────────────

    public void addLogoutListener(Runnable action) { logoutBtn.addActionListener(e -> action.run()); }

    public void setMessage(String message) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(message));
    }

    public void showView() { setVisible(true); toFront(); }
    public void hideView() { setVisible(false); }
    public JFrame getFrame() { return frame; }
}
