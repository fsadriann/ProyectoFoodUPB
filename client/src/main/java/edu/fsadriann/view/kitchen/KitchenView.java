package edu.fsadriann.view.kitchen;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class KitchenView extends JFrame {

    private static final Color BG      = Color.WHITE;
    private static final Color BG2     = new Color(245, 245, 245);
    private static final Color BLUE    = new Color(24, 95, 165);
    private static final Color TEXT    = new Color(30, 30, 30);
    private static final Color TEXT2   = new Color(100, 100, 100);
    private static final Color BORDER_C = new Color(220, 220, 220);

    private final JFrame frame;
    private JButton logoutBtn;
    private JLabel  statusLabel;

    private JLabel metricQueue, metricPrep, metricReady, metricTime;
    private JPanel queueBody, rapidasBody, complejasBody, listosBody;

    public KitchenView(String userLabel) {
        super("Food UPB — Cocina");
        frame = this;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 650));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG2);
        root.add(buildHeader(), BorderLayout.NORTH);
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
        JLabel title = new JLabel("Food UPB — Cocina");
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
        c.add(buildKanban(),  BorderLayout.CENTER);
        return c;
    }

    private JPanel buildMetrics() {
        JPanel row = new JPanel(new GridLayout(1, 4, 10, 0));
        row.setOpaque(false);

        metricQueue = new JLabel("—");
        metricPrep  = new JLabel("—");
        metricReady = new JLabel("—");
        metricTime  = new JLabel("—");

        row.add(metricTile("En cola",          metricQueue));
        row.add(metricTile("En preparación",   metricPrep));
        row.add(metricTile("Listos hoy",       metricReady));
        row.add(metricTile("Tiempo prom.",     metricTime));
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

    private JPanel buildKanban() {
        JPanel k = new JPanel(new GridLayout(1, 4, 10, 0));
        k.setOpaque(false);

        queueBody     = colBody();
        rapidasBody   = colBody();
        complejasBody = colBody();
        listosBody    = colBody();

        k.add(col("Cola de espera",      queueBody));
        k.add(col("Rápidas (B1-B2-B3)",  rapidasBody));
        k.add(col("Complejas (B4)",       complejasBody));
        k.add(col("Listos para entrega",  listosBody));
        return k;
    }

    private JPanel col(String title, JPanel body) {
        JPanel c = new JPanel(new BorderLayout(0, 6));
        c.setBackground(BG);
        c.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_C), new EmptyBorder(8, 8, 8, 8)
        ));
        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.BOLD, 12));
        t.setForeground(TEXT2);
        t.setBorder(new EmptyBorder(0, 0, 4, 0));
        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        c.add(t,      BorderLayout.NORTH);
        c.add(scroll, BorderLayout.CENTER);
        return c;
    }

    private JPanel colBody() {
        JPanel b = new JPanel();
        b.setLayout(new BoxLayout(b, BoxLayout.Y_AXIS));
        b.setBackground(BG);
        b.setBorder(new EmptyBorder(2, 0, 2, 0));
        return b;
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

    // ── Order card ────────────────────────────────────────────────────────────

    private JPanel orderCard(String id, String name, String items, String badge, Color badgeBg) {
        JPanel c = new JPanel();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBackground(BG);
        c.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_C), new EmptyBorder(8, 10, 8, 10)
        ));
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        c.setAlignmentX(LEFT_ALIGNMENT);

        JPanel top = new JPanel(new BorderLayout(4, 0));
        top.setOpaque(false);
        JLabel idL = new JLabel(id);
        idL.setFont(new Font("SansSerif", Font.BOLD, 12));
        idL.setForeground(TEXT);
        top.add(idL, BorderLayout.WEST);
        if (badge != null) {
            JLabel bl = new JLabel(" " + badge + " ");
            bl.setFont(new Font("SansSerif", Font.BOLD, 10));
            bl.setBackground(badgeBg);
            bl.setForeground(TEXT);
            bl.setOpaque(true);
            top.add(bl, BorderLayout.EAST);
        }
        c.add(top);

        if (name != null && !name.isBlank()) {
            JLabel l = new JLabel(name);
            l.setFont(new Font("SansSerif", Font.PLAIN, 11));
            l.setForeground(TEXT2);
            l.setAlignmentX(LEFT_ALIGNMENT);
            c.add(l);
        }
        if (items != null && !items.isBlank()) {
            JLabel l = new JLabel(items);
            l.setFont(new Font("SansSerif", Font.PLAIN, 11));
            l.setForeground(new Color(140, 140, 140));
            l.setAlignmentX(LEFT_ALIGNMENT);
            c.add(l);
        }
        return c;
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    public void addToQueue(String orderId, String clientName, String items, boolean premium) {
        SwingUtilities.invokeLater(() -> {
            queueBody.add(orderCard(orderId, clientName, items,
                premium ? "Premium" : null, new Color(255, 247, 230)));
            queueBody.add(Box.createVerticalStrut(6));
            queueBody.revalidate();
            queueBody.repaint();
        });
    }

    public void addToRapidas(String orderId, String items) {
        SwingUtilities.invokeLater(() -> {
            rapidasBody.add(orderCard(orderId, null, items, "Rápida", new Color(230, 241, 251)));
            rapidasBody.add(Box.createVerticalStrut(6));
            rapidasBody.revalidate();
        });
    }

    public void addToComplejas(String orderId, String items) {
        SwingUtilities.invokeLater(() -> {
            complejasBody.add(orderCard(orderId, null, items, "Compleja", new Color(255, 247, 230)));
            complejasBody.add(Box.createVerticalStrut(6));
            complejasBody.revalidate();
        });
    }

    public void addToListos(String orderId, String clientName) {
        SwingUtilities.invokeLater(() -> {
            listosBody.add(orderCard(orderId, clientName, null, "Listo", new Color(234, 243, 222)));
            listosBody.add(Box.createVerticalStrut(6));
            listosBody.revalidate();
        });
    }

    public void setMetrics(int queue, int inPrep, int readyToday, String avgTime) {
        SwingUtilities.invokeLater(() -> {
            metricQueue.setText(String.valueOf(queue));
            metricPrep.setText(String.valueOf(inPrep));
            metricReady.setText(String.valueOf(readyToday));
            metricTime.setText(avgTime);
        });
    }

    public void addLogoutListener(Runnable action) { logoutBtn.addActionListener(e -> action.run()); }
    public void setMessage(String msg)             { SwingUtilities.invokeLater(() -> statusLabel.setText(msg)); }
    public void showView()  { setVisible(true); toFront(); }
    public void hideView()  { setVisible(false); }
    public JFrame getFrame() { return frame; }
}
