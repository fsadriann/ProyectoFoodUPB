package edu.fsadriann.view.kitchen;

import edu.fsadriann.view.UIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class KitchenView extends JFrame {

    private final JFrame frame;
    private JLabel statusLabel;
    private JButton logoutBtn;

    // ── Metric labels ─────────────────────────────────────────────────────────
    private JLabel metricQueue;
    private JLabel metricPrep;
    private JLabel metricReady;
    private JLabel metricTime;

    // ── Kanban column bodies (where order cards are added) ────────────────────
    private JPanel queueBody;
    private JPanel rapidasBody;
    private JPanel complejasBody;
    private JPanel listosBody;

    // ── Column count badges ───────────────────────────────────────────────────
    private JLabel queueCount;
    private JLabel listosCount;

    public KitchenView(String userLabel) {
        super("Food UPB — Cocina");
        frame = this;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 700));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIComponents.BG2);
        root.add(buildTopbar(userLabel),  BorderLayout.NORTH);
        root.add(buildContent(),          BorderLayout.CENTER);
        root.add(buildStatusBar(),        BorderLayout.SOUTH);
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
        JLabel section = new JLabel("Cocina");
        section.setFont(UIComponents.fontBold(14));
        section.setForeground(UIComponents.ACCENT);
        section.setBorder(new EmptyBorder(0, 12, 0, 0));
        left.add(brand);
        left.add(sep);
        left.add(section);

        logoutBtn = UIComponents.roundBtnSmall("Cerrar sesión", UIComponents.BG, UIComponents.TXT, UIComponents.BORDER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(UIComponents.badge("Cocina", UIComponents.INFO_BG, UIComponents.INFO_FG));
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
        content.add(buildKanban(),  BorderLayout.CENTER);
        return content;
    }

    // ── Metrics row ───────────────────────────────────────────────────────────

    private JPanel buildMetrics() {
        JPanel row = new JPanel(new GridLayout(1, 4, 10, 0));
        row.setOpaque(false);

        metricQueue = new JLabel("—");
        metricPrep  = new JLabel("—");
        metricReady = new JLabel("—");
        metricTime  = new JLabel("—");

        row.add(metricCardPanel("En cola",        metricQueue,  UIComponents.WARNING_BG,  UIComponents.WARNING_FG));
        row.add(metricCardPanel("En preparación", metricPrep,   UIComponents.INFO_BG,     UIComponents.INFO_FG));
        row.add(metricCardPanel("Listos hoy",     metricReady,  UIComponents.SUCCESS_BG,  UIComponents.SUCCESS_FG));
        row.add(metricCardPanel("Tiempo prom.",   metricTime,   UIComponents.BG2,         UIComponents.TXT2));
        return row;
    }

    private JPanel metricCardPanel(String label, JLabel valueLabel, Color bg, Color fg) {
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

        JLabel labelLbl = new JLabel(label);
        labelLbl.setFont(UIComponents.fontPlain(12));
        labelLbl.setForeground(UIComponents.TXT2);

        valueLabel.setFont(UIComponents.fontBold(28));
        valueLabel.setForeground(UIComponents.TXT);

        card.add(labelLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(valueLabel);
        return card;
    }

    // ── Kanban 4 columns ─────────────────────────────────────────────────────

    private JPanel buildKanban() {
        JPanel kanban = new JPanel(new GridLayout(1, 4, 10, 0));
        kanban.setOpaque(false);

        queueCount  = badgeLabel("0", UIComponents.WARNING_BG, UIComponents.WARNING_FG);
        listosCount = badgeLabel("0", UIComponents.SUCCESS_BG, UIComponents.SUCCESS_FG);

        queueBody    = columnBody();
        rapidasBody  = columnBody();
        complejasBody= columnBody();
        listosBody   = columnBody();

        kanban.add(buildColumn("Cola de espera",      queueCount,
                               UIComponents.WARNING_BG, UIComponents.WARNING_FG,  queueBody));
        kanban.add(buildColumn("Rápidas (B1·B2·B3)",
                               badgeLabel("0", UIComponents.INFO_BG, UIComponents.INFO_FG),
                               UIComponents.INFO_BG,    UIComponents.INFO_FG,     rapidasBody));
        kanban.add(buildColumn("Complejas (B4)",
                               badgeLabel("0", UIComponents.WARNING_BG, UIComponents.WARNING_FG),
                               UIComponents.WARNING_BG, UIComponents.WARNING_FG,  complejasBody));
        kanban.add(buildColumn("Listos para entrega",  listosCount,
                               UIComponents.SUCCESS_BG, UIComponents.SUCCESS_FG,  listosBody));
        return kanban;
    }

    private JPanel buildColumn(String title, JLabel countBadge,
                               Color badgeBg, Color badgeFg, JPanel body) {
        JPanel col = new JPanel(new BorderLayout(0, 8));
        col.setBackground(UIComponents.BG2);
        col.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 1, 1, 1, UIComponents.BORDER),
            new EmptyBorder(10, 10, 10, 10)
        ));

        // Column header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UIComponents.fontBold(12));
        titleLbl.setForeground(UIComponents.TXT2);
        header.add(titleLbl,   BorderLayout.WEST);
        header.add(countBadge, BorderLayout.EAST);

        // Body in scroll
        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIComponents.BG2);

        col.add(header, BorderLayout.NORTH);
        col.add(scroll, BorderLayout.CENTER);
        return col;
    }

    private JPanel columnBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(UIComponents.BG2);
        body.setBorder(new EmptyBorder(4, 0, 4, 0));
        return body;
    }

    private JLabel badgeLabel(String text, Color bg, Color fg) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(UIComponents.fontBold(11));
        lbl.setOpaque(true);
        lbl.setBackground(bg);
        lbl.setForeground(fg);
        lbl.setBorder(new EmptyBorder(2, 7, 2, 7));
        return lbl;
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

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC API — builds order cards for the kanban
    // ─────────────────────────────────────────────────────────────────────────

    /** Adds an order card to the waiting queue column. */
    public void addToQueue(String orderId, String clientName, String items, boolean premium) {
        SwingUtilities.invokeLater(() -> {
            queueBody.add(orderCard(orderId, clientName, items, premium, UIComponents.WARNING_BG));
            queueBody.add(Box.createVerticalStrut(6));
            queueBody.revalidate();
            queueBody.repaint();
        });
    }

    /** Adds an order card to the fast-prep column. */
    public void addToRapidas(String orderId, String items) {
        SwingUtilities.invokeLater(() -> {
            rapidasBody.add(orderCard(orderId, "", items, false, UIComponents.INFO_BG));
            rapidasBody.add(Box.createVerticalStrut(6));
            rapidasBody.revalidate();
        });
    }

    /** Adds an order card to the complex-prep column. */
    public void addToComplejas(String orderId, String items) {
        SwingUtilities.invokeLater(() -> {
            complejasBody.add(orderCard(orderId, "", items, false, UIComponents.WARNING_BG));
            complejasBody.add(Box.createVerticalStrut(6));
            complejasBody.revalidate();
        });
    }

    /** Adds an order card to the ready-for-delivery column. */
    public void addToListos(String orderId, String clientName) {
        SwingUtilities.invokeLater(() -> {
            listosBody.add(orderCard(orderId, clientName, "", false, UIComponents.SUCCESS_BG));
            listosBody.add(Box.createVerticalStrut(6));
            listosBody.revalidate();
        });
    }

    /** Updates the four metric counters. */
    public void setMetrics(int queue, int inPrep, int readyToday, String avgTime) {
        SwingUtilities.invokeLater(() -> {
            metricQueue.setText(String.valueOf(queue));
            metricPrep.setText(String.valueOf(inPrep));
            metricReady.setText(String.valueOf(readyToday));
            metricTime.setText(avgTime);
        });
    }

    private JPanel orderCard(String orderId, String clientName, String items,
                             boolean premium, Color accentBg) {
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
        card.setBorder(new EmptyBorder(8, 10, 8, 10));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel headerRow = new JPanel(new BorderLayout(4, 0));
        headerRow.setOpaque(false);
        JLabel idLbl = new JLabel(orderId);
        idLbl.setFont(UIComponents.fontBold(12));
        idLbl.setForeground(UIComponents.TXT);
        headerRow.add(idLbl, BorderLayout.WEST);
        if (premium) {
            JLabel badge = UIComponents.badge("Premium", UIComponents.PREMIUM_BG, UIComponents.PREMIUM_FG);
            headerRow.add(badge, BorderLayout.EAST);
        }
        card.add(headerRow);

        if (!clientName.isBlank()) {
            JLabel clientLbl = new JLabel(clientName);
            clientLbl.setFont(UIComponents.fontPlain(11));
            clientLbl.setForeground(UIComponents.TXT2);
            clientLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(clientLbl);
        }
        if (!items.isBlank()) {
            JLabel itemsLbl = new JLabel(items);
            itemsLbl.setFont(UIComponents.fontPlain(11));
            itemsLbl.setForeground(UIComponents.TXT3);
            itemsLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(itemsLbl);
        }
        return card;
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
