package edu.fsadriann.view.kitchen;

import edu.fsadriann.model.iterator.Iterator;
import edu.fsadriann.server.model.order.Order;
import edu.fsadriann.server.model.product.Product;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class KitchenView extends JFrame {

    // ── Paleta ────────────────────────────────────────────────────────────────
    private static final Color BG        = Color.WHITE;
    private static final Color BG2       = new Color(245, 245, 245);
    private static final Color BLUE      = new Color(24, 95, 165);
    private static final Color GREEN     = new Color(40, 167, 69);
    private static final Color ORANGE    = new Color(210, 95, 20);
    private static final Color RED_BADGE = new Color(220, 53, 69);
    private static final Color GOLD      = new Color(255, 193, 7);
    private static final Color TEXT      = new Color(30, 30, 30);
    private static final Color TEXT2     = new Color(100, 100, 100);
    private static final Color TEXT3     = new Color(140, 140, 140);
    private static final Color BORDER_C  = new Color(220, 220, 220);
    private static final Color COL_QUEUE = new Color(248, 249, 250);
    private static final Color COL_RAP   = new Color(232, 245, 253);
    private static final Color COL_COMP  = new Color(255, 248, 235);
    private static final Color COL_LIST  = new Color(236, 248, 236);

    private static final NumberFormat COP = NumberFormat.getIntegerInstance(new Locale("es", "CO"));

    // ── Header ────────────────────────────────────────────────────────────────
    private JButton logoutBtn;
    private JButton refreshBtn;
    private JLabel  statusLabel;

    // ── Métricas ──────────────────────────────────────────────────────────────
    private JLabel metricQueue, metricPrep, metricReady, metricTime;

    // ── Kanban bodies ─────────────────────────────────────────────────────────
    private JPanel queueBody, rapidasBody, complejasBody, listosBody;
    private JButton procesarSiguienteBtn;

    // ── Registro de tarjetas ──────────────────────────────────────────────────
    private final Map<String, Component[]> queueCards     = new LinkedHashMap<>();
    private final Map<String, Component[]> rapidasCards   = new LinkedHashMap<>();
    private final Map<String, Component[]> complejasCards = new LinkedHashMap<>();
    private final Map<String, Component[]> listosCards    = new LinkedHashMap<>();

    // ─────────────────────────────────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────────────────────────────────

    public KitchenView(String userLabel) {
        super("Food UPB — Cocina");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 700));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG2);
        root.add(buildHeader(),  BorderLayout.NORTH);
        root.add(buildContent(), BorderLayout.CENTER);
        root.add(buildStatus(),  BorderLayout.SOUTH);
        setContentPane(root);
        pack();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CONSTRUCCION DE LA INTERFAZ
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(BG);
        h.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER_C),
                new EmptyBorder(10, 16, 10, 16)));

        JLabel title = new JLabel("Food UPB — Cocina");
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        title.setForeground(BLUE);

        refreshBtn = smallBtn("Actualizar", BG, TEXT);
        logoutBtn  = smallBtn("Cerrar sesion", BG, TEXT);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.add(refreshBtn);
        btnRow.add(logoutBtn);

        h.add(title,  BorderLayout.WEST);
        h.add(btnRow, BorderLayout.EAST);
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
        metricQueue = new JLabel("0");
        metricPrep  = new JLabel("0");
        metricReady = new JLabel("0");
        metricTime  = new JLabel("-");
        row.add(metricTile("En cola",        metricQueue));
        row.add(metricTile("En preparacion", metricPrep));
        row.add(metricTile("Listos hoy",     metricReady));
        row.add(metricTile("Tiempo prom.",   metricTime));
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

    private JPanel buildKanban() {
        JPanel k = new JPanel(new GridLayout(1, 4, 10, 0));
        k.setOpaque(false);

        queueBody     = colBody();
        rapidasBody   = colBody();
        complejasBody = colBody();
        listosBody    = colBody();

        procesarSiguienteBtn = new JButton("Procesar siguiente");
        procesarSiguienteBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        procesarSiguienteBtn.setBackground(ORANGE);
        procesarSiguienteBtn.setForeground(Color.WHITE);
        procesarSiguienteBtn.setOpaque(true);
        procesarSiguienteBtn.setBorderPainted(false);
        procesarSiguienteBtn.setFocusPainted(false);
        procesarSiguienteBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        k.add(colWithAction("Cola de espera",       COL_QUEUE, queueBody,     procesarSiguienteBtn));
        k.add(col("Rapidas  (B1 B2 B3)",            COL_RAP,   rapidasBody));
        k.add(col("Complejas  (B4)",                COL_COMP,  complejasBody));
        k.add(col("Listos para entrega",            COL_LIST,  listosBody));
        return k;
    }

    private JPanel col(String title, Color bg, JPanel body) {
        JPanel c = new JPanel(new BorderLayout(0, 6));
        c.setBackground(bg);
        c.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_C), new EmptyBorder(8, 8, 8, 8)));
        c.add(colTitle(title), BorderLayout.NORTH);
        c.add(colScroll(body, bg), BorderLayout.CENTER);
        return c;
    }

    private JPanel colWithAction(String title, Color bg, JPanel body, JButton btn) {
        JPanel c = new JPanel(new BorderLayout(0, 6));
        c.setBackground(bg);
        c.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_C), new EmptyBorder(8, 8, 8, 8)));
        JPanel header = new JPanel(new BorderLayout(6, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 4, 0));
        header.add(colTitle(title), BorderLayout.WEST);
        header.add(btn, BorderLayout.EAST);
        c.add(header, BorderLayout.NORTH);
        c.add(colScroll(body, bg), BorderLayout.CENTER);
        return c;
    }

    private JLabel colTitle(String text) {
        JLabel t = new JLabel(text);
        t.setFont(new Font("SansSerif", Font.BOLD, 12));
        t.setForeground(TEXT2);
        return t;
    }

    private JScrollPane colScroll(JPanel body, Color bg) {
        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(bg);
        return scroll;
    }

    private JPanel colBody() {
        JPanel b = new JPanel();
        b.setLayout(new BoxLayout(b, BoxLayout.Y_AXIS));
        b.setOpaque(false);
        b.setBorder(new EmptyBorder(2, 0, 2, 0));
        return b;
    }

    private JPanel buildStatus() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG2);
        bar.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, BORDER_C),
                new EmptyBorder(5, 14, 5, 14)));
        statusLabel = new JLabel("Iniciando cocina...");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusLabel.setForeground(TEXT2);
        bar.add(statusLabel, BorderLayout.WEST);
        return bar;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CONSTRUCCION DE TARJETAS DETALLADAS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Tarjeta completa para la columna de cola — muestra todos los detalles del pedido.
     */
    private JPanel buildQueueCard(Order order) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_C), new EmptyBorder(10, 12, 10, 12)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 999));
        card.setAlignmentX(LEFT_ALIGNMENT);

        // Fila superior: ID corto + badge premium
        JPanel topRow = new JPanel(new BorderLayout(4, 0));
        topRow.setOpaque(false);
        String sid = shortId(order.getOrderId());
        JLabel idLabel = new JLabel("Pedido " + sid);
        idLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        idLabel.setForeground(TEXT);
        topRow.add(idLabel, BorderLayout.WEST);
        if (order.isPremium()) {
            JLabel badge = badge("PREMIUM", GOLD, new Color(90, 60, 0));
            topRow.add(badge, BorderLayout.EAST);
        }
        card.add(topRow);
        card.add(vgap(6));

        // Informacion del cliente
        card.add(infoRow("Cliente:", order.getCedulaCliente()));
        card.add(vgap(2));
        card.add(infoRow("Estado:", order.getEstado().name()));
        card.add(vgap(2));

        // Cuadrante destino si existe
        if (order.getCuadranteDestino() != null && !order.getCuadranteDestino().isBlank()) {
            card.add(infoRow("Destino:", order.getCuadranteDestino()));
            card.add(vgap(2));
        }

        // Separador
        card.add(separator());
        card.add(vgap(6));

        // Lista de productos del carrito
        JLabel prodTitle = new JLabel("Productos:");
        prodTitle.setFont(new Font("SansSerif", Font.BOLD, 11));
        prodTitle.setForeground(TEXT2);
        prodTitle.setAlignmentX(LEFT_ALIGNMENT);
        card.add(prodTitle);
        card.add(vgap(4));

        boolean tieneComplejo = false;
        Iterator<Product> it = order.getCarrito().iterator();
        while (it.hasNext()) {
            Product p = it.next();
            if (p == null) continue;
            if (p.isComplejo()) tieneComplejo = true;
            card.add(buildProductRow(p));
            card.add(vgap(2));
        }

        // Badge de complejidad
        card.add(vgap(4));
        if (tieneComplejo) {
            JLabel compBadge = badge("Requiere Fogon Grande (B4)", new Color(255, 230, 200), new Color(150, 60, 0));
            compBadge.setAlignmentX(LEFT_ALIGNMENT);
            card.add(compBadge);
        } else {
            JLabel simpBadge = badge("Fogon Normal (B1/B2/B3)", new Color(220, 240, 255), new Color(0, 60, 120));
            simpBadge.setAlignmentX(LEFT_ALIGNMENT);
            card.add(simpBadge);
        }

        card.add(vgap(6));
        card.add(separator());
        card.add(vgap(6));

        // Totales
        card.add(infoRow("Subtotal:", "$" + COP.format((long) order.getSubtotal())));
        card.add(vgap(2));
        card.add(infoRow("IVA (19%):", "$" + COP.format((long) order.getImpuesto())));
        card.add(vgap(2));
        card.add(infoRow("Domicilio:", order.isPremium() ? "Gratis" : "$" + COP.format((long) order.getCostoDomi())));
        card.add(vgap(4));

        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        totalRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        JLabel totalLbl = new JLabel("TOTAL");
        totalLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        totalLbl.setForeground(TEXT);
        JLabel totalVal = new JLabel("$" + COP.format((long) order.getTotal()));
        totalVal.setFont(new Font("SansSerif", Font.BOLD, 13));
        totalVal.setForeground(BLUE);
        totalRow.add(totalLbl, BorderLayout.WEST);
        totalRow.add(totalVal, BorderLayout.EAST);
        totalRow.setAlignmentX(LEFT_ALIGNMENT);
        card.add(totalRow);

        return card;
    }

    /**
     * Tarjeta compacta para fogones (rapidas/complejas) con boton de listo.
     */
    private JPanel buildFogonCard(Order order, String fogonLabel, Color fogonColor, Runnable onListo) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(fogonColor, 2), new EmptyBorder(10, 12, 10, 12)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 999));
        card.setAlignmentX(LEFT_ALIGNMENT);

        // Cabecera: ID + badge de fogon
        JPanel topRow = new JPanel(new BorderLayout(4, 0));
        topRow.setOpaque(false);
        JLabel idLabel = new JLabel("Pedido " + shortId(order.getOrderId()));
        idLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        idLabel.setForeground(TEXT);
        topRow.add(idLabel, BorderLayout.WEST);
        JLabel fogonBadge = badge(fogonLabel, fogonColor, Color.WHITE);
        topRow.add(fogonBadge, BorderLayout.EAST);
        card.add(topRow);
        card.add(vgap(2));

        // Cliente + tipo
        card.add(infoRow("Cliente:", order.getCedulaCliente()));
        card.add(vgap(2));
        if (order.isPremium()) {
            JLabel prem = badge("PREMIUM", GOLD, new Color(90, 60, 0));
            prem.setAlignmentX(LEFT_ALIGNMENT);
            card.add(prem);
            card.add(vgap(4));
        }

        card.add(separator());
        card.add(vgap(6));

        // Productos
        JLabel prodTitle = new JLabel("Productos en preparacion:");
        prodTitle.setFont(new Font("SansSerif", Font.BOLD, 11));
        prodTitle.setForeground(TEXT2);
        prodTitle.setAlignmentX(LEFT_ALIGNMENT);
        card.add(prodTitle);
        card.add(vgap(4));

        Iterator<Product> it = order.getCarrito().iterator();
        while (it.hasNext()) {
            Product p = it.next();
            if (p == null) continue;
            card.add(buildProductRow(p));
            card.add(vgap(2));
        }

        card.add(vgap(8));

        // Boton marcar listo
        JButton listoBtn = new JButton("Marcar listo");
        listoBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        listoBtn.setBackground(GREEN);
        listoBtn.setForeground(Color.WHITE);
        listoBtn.setOpaque(true);
        listoBtn.setBorderPainted(false);
        listoBtn.setFocusPainted(false);
        listoBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        listoBtn.setAlignmentX(LEFT_ALIGNMENT);
        listoBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        listoBtn.addActionListener(e -> onListo.run());
        card.add(listoBtn);

        return card;
    }

    /**
     * Tarjeta compacta para columna de listos.
     */
    private JPanel buildListoCard(Order order, String displayId, String clientLabel) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(GREEN, 2), new EmptyBorder(10, 12, 10, 12)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 999));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        JLabel idLabel = new JLabel("Pedido " + displayId);
        idLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        idLabel.setForeground(TEXT);
        topRow.add(idLabel, BorderLayout.WEST);
        JLabel checkBadge = badge("LISTO", GREEN, Color.WHITE);
        topRow.add(checkBadge, BorderLayout.EAST);
        card.add(topRow);
        card.add(vgap(4));

        card.add(infoRow("Cliente:", clientLabel));
        card.add(vgap(2));

        if (order != null && order.getCarrito() != null) {
            card.add(separator());
            card.add(vgap(4));
            Iterator<Product> it = order.getCarrito().iterator();
            while (it.hasNext()) {
                Product p = it.next();
                if (p == null) continue;
                JLabel pl = new JLabel(p.getNombre() + " x" + p.getCantidad());
                pl.setFont(new Font("SansSerif", Font.PLAIN, 11));
                pl.setForeground(TEXT3);
                pl.setAlignmentX(LEFT_ALIGNMENT);
                card.add(pl);
                card.add(vgap(1));
            }
            card.add(vgap(4));
            if (order.getTotal() > 0) {
                card.add(infoRow("Total:", "$" + COP.format((long) order.getTotal())));
            }
        }

        return card;
    }

    /**
     * Fila de un producto con nombre, cantidad, precio y badge de complejidad.
     */
    private JPanel buildProductRow(Product p) {
        JPanel row = new JPanel(new BorderLayout(4, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JLabel nameLabel = new JLabel(p.getNombre() + " x" + p.getCantidad());
        nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        nameLabel.setForeground(TEXT);
        row.add(nameLabel, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        right.setOpaque(false);

        if (p.isComplejo()) {
            JLabel cBadge = badge("complejo", new Color(255, 220, 180), new Color(140, 60, 0));
            right.add(cBadge);
        }

        JLabel priceLabel = new JLabel("$" + COP.format((long) p.getPrecio() * p.getCantidad()));
        priceLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        priceLabel.setForeground(BLUE);
        right.add(priceLabel);

        row.add(right, BorderLayout.EAST);
        return row;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS DE CONSTRUCCION
    // ─────────────────────────────────────────────────────────────────────────

    private JLabel badge(String text, Color bg, Color fg) {
        JLabel l = new JLabel(" " + text + " ");
        l.setFont(new Font("SansSerif", Font.BOLD, 9));
        l.setBackground(bg);
        l.setForeground(fg);
        l.setOpaque(true);
        l.setBorder(new EmptyBorder(1, 4, 1, 4));
        return l;
    }

    private JPanel infoRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));
        row.setAlignmentX(LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        lbl.setForeground(TEXT2);
        JLabel val = new JLabel(value);
        val.setFont(new Font("SansSerif", Font.PLAIN, 11));
        val.setForeground(TEXT);
        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        return row;
    }

    private JSeparator separator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_C);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private Component vgap(int h) {
        return Box.createVerticalStrut(h);
    }

    private String shortId(String id) {
        if (id == null || id.isBlank()) return "-";
        return id.length() > 8 ? id.substring(0, 8) + "..." : id;
    }

    private JButton smallBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_C), new EmptyBorder(4, 10, 4, 10)));
        return btn;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // API PUBLICA — gestion de tarjetas
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Agrega un pedido completo a la columna "Cola de espera" con todos sus detalles.
     * El Order debe tener el carrito y los totales ya calculados.
     */
    public void addToQueue(String orderId, String displayId, String clientName,
                           String items, boolean premium) {
        // Esta firma se mantiene por compatibilidad con el controller existente.
        // Construimos una tarjeta minima cuando no tenemos el Order completo.
        SwingUtilities.invokeLater(() -> {
            JPanel card = buildMinimalQueueCard(displayId, clientName, items, premium);
            Component spacer = Box.createVerticalStrut(8);
            queueCards.put(orderId, new Component[]{card, spacer});
            queueBody.add(card);
            queueBody.add(spacer);
            queueBody.revalidate();
            queueBody.repaint();
        });
    }

    /**
     * Agrega un pedido a la cola con el objeto Order completo (version enriquecida).
     * Usar esta version cuando se tenga el Order con carrito y totales.
     */
    public void addToQueueFull(String orderId, Order order) {
        SwingUtilities.invokeLater(() -> {
            JPanel card = buildQueueCard(order);
            Component spacer = Box.createVerticalStrut(8);
            queueCards.put(orderId, new Component[]{card, spacer});
            queueBody.add(card);
            queueBody.add(spacer);
            queueBody.revalidate();
            queueBody.repaint();
        });
    }

    /** Agrega pedido a Rapidas con tarjeta completa de fogon. */
    public void addToRapidas(String orderId, String displayId, String items, Runnable onListo) {
        // Firma de compatibilidad — tarjeta compacta sin Order completo
        SwingUtilities.invokeLater(() -> {
            JPanel card = buildSimpleFogonCard(displayId, items,
                    "Fogon Normal", new Color(100, 160, 220), onListo);
            Component spacer = Box.createVerticalStrut(8);
            rapidasCards.put(orderId, new Component[]{card, spacer});
            rapidasBody.add(card);
            rapidasBody.add(spacer);
            rapidasBody.revalidate();
        });
    }

    /** Agrega pedido a Rapidas con el Order completo. */
    public void addToRapidasFull(String orderId, Order order, Runnable onListo) {
        SwingUtilities.invokeLater(() -> {
            JPanel card = buildFogonCard(order, "Fogon Normal", new Color(100, 160, 220), onListo);
            Component spacer = Box.createVerticalStrut(8);
            rapidasCards.put(orderId, new Component[]{card, spacer});
            rapidasBody.add(card);
            rapidasBody.add(spacer);
            rapidasBody.revalidate();
        });
    }

    /** Agrega pedido a Complejas con tarjeta compacta. */
    public void addToComplejas(String orderId, String displayId, String items, Runnable onListo) {
        SwingUtilities.invokeLater(() -> {
            JPanel card = buildSimpleFogonCard(displayId, items,
                    "Fogon Grande B4", new Color(230, 150, 50), onListo);
            Component spacer = Box.createVerticalStrut(8);
            complejasCards.put(orderId, new Component[]{card, spacer});
            complejasBody.add(card);
            complejasBody.add(spacer);
            complejasBody.revalidate();
        });
    }

    /** Agrega pedido a Complejas con el Order completo. */
    public void addToComplejasFull(String orderId, Order order, Runnable onListo) {
        SwingUtilities.invokeLater(() -> {
            JPanel card = buildFogonCard(order, "Fogon Grande B4", new Color(230, 150, 50), onListo);
            Component spacer = Box.createVerticalStrut(8);
            complejasCards.put(orderId, new Component[]{card, spacer});
            complejasBody.add(card);
            complejasBody.add(spacer);
            complejasBody.revalidate();
        });
    }

    /** Agrega pedido a Listos con tarjeta compacta (sin Order). */
    public void addToListos(String orderId, String displayId, String clientName) {
        addToListosFull(orderId, displayId, clientName, null);
    }

    /** Agrega pedido a Listos con Order completo para mostrar resumen. */
    public void addToListosFull(String orderId, String displayId, String clientName, Order order) {
        SwingUtilities.invokeLater(() -> {
            JPanel card = buildListoCard(order, displayId, clientName);
            Component spacer = Box.createVerticalStrut(8);
            listosCards.put(orderId, new Component[]{card, spacer});
            listosBody.add(card);
            listosBody.add(spacer);
            listosBody.revalidate();
        });
    }

    // Quitar tarjetas
    public void removeFromQueue(String orderId)     { removeCard(queueCards,     queueBody,     orderId); }
    public void removeFromRapidas(String orderId)   { removeCard(rapidasCards,   rapidasBody,   orderId); }
    public void removeFromComplejas(String orderId) { removeCard(complejasCards, complejasBody, orderId); }

    private void removeCard(Map<String, Component[]> map, JPanel body, String orderId) {
        SwingUtilities.invokeLater(() -> {
            Component[] comps = map.remove(orderId);
            if (comps != null) {
                body.remove(comps[0]);
                body.remove(comps[1]);
                body.revalidate();
                body.repaint();
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TARJETAS DE COMPATIBILIDAD (sin Order completo)
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildMinimalQueueCard(String displayId, String clientName,
                                         String items, boolean premium) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_C), new EmptyBorder(10, 12, 10, 12)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 999));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JPanel topRow = new JPanel(new BorderLayout(4, 0));
        topRow.setOpaque(false);
        JLabel idLabel = new JLabel("Pedido " + displayId);
        idLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        idLabel.setForeground(TEXT);
        topRow.add(idLabel, BorderLayout.WEST);
        if (premium) topRow.add(badge("PREMIUM", GOLD, new Color(90, 60, 0)), BorderLayout.EAST);
        card.add(topRow);
        card.add(vgap(4));

        if (clientName != null && !clientName.isBlank()) {
            card.add(infoRow("Cliente:", clientName));
            card.add(vgap(2));
        }
        if (items != null && !items.isBlank()) {
            card.add(separator());
            card.add(vgap(4));
            JLabel itemsLbl = new JLabel(items);
            itemsLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
            itemsLbl.setForeground(TEXT3);
            itemsLbl.setAlignmentX(LEFT_ALIGNMENT);
            card.add(itemsLbl);
        }
        return card;
    }

    private JPanel buildSimpleFogonCard(String displayId, String items,
                                        String fogonLabel, Color fogonColor, Runnable onListo) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(fogonColor, 2), new EmptyBorder(10, 12, 10, 12)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 999));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JPanel topRow = new JPanel(new BorderLayout(4, 0));
        topRow.setOpaque(false);
        JLabel idLabel = new JLabel("Pedido " + displayId);
        idLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        idLabel.setForeground(TEXT);
        topRow.add(idLabel, BorderLayout.WEST);
        topRow.add(badge(fogonLabel, fogonColor, Color.WHITE), BorderLayout.EAST);
        card.add(topRow);
        card.add(vgap(4));

        if (items != null && !items.isBlank()) {
            card.add(separator());
            card.add(vgap(4));
            JLabel itemsLbl = new JLabel(items);
            itemsLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
            itemsLbl.setForeground(TEXT3);
            itemsLbl.setAlignmentX(LEFT_ALIGNMENT);
            card.add(itemsLbl);
            card.add(vgap(6));
        }

        JButton listoBtn = new JButton("Marcar listo");
        listoBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        listoBtn.setBackground(GREEN);
        listoBtn.setForeground(Color.WHITE);
        listoBtn.setOpaque(true);
        listoBtn.setBorderPainted(false);
        listoBtn.setFocusPainted(false);
        listoBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        listoBtn.setAlignmentX(LEFT_ALIGNMENT);
        listoBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        listoBtn.addActionListener(e -> onListo.run());
        card.add(listoBtn);
        return card;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // API PUBLICA — metricas y mensajes
    // ─────────────────────────────────────────────────────────────────────────

    public void setMetrics(int queue, int inPrep, int readyToday, String avgTime) {
        SwingUtilities.invokeLater(() -> {
            metricQueue.setText(String.valueOf(queue));
            metricPrep.setText(String.valueOf(inPrep));
            metricReady.setText(String.valueOf(readyToday));
            metricTime.setText(avgTime);
        });
    }

    public void addLogoutListener(Runnable action)            { logoutBtn.addActionListener(e -> action.run()); }
    public void addRefreshListener(Runnable action)           { refreshBtn.addActionListener(e -> action.run()); }
    public void addProcesarSiguienteListener(Runnable action) { procesarSiguienteBtn.addActionListener(e -> action.run()); }

    public void setMessage(String msg) { SwingUtilities.invokeLater(() -> statusLabel.setText(msg)); }
    public void showError(String msg)  { JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE); }
    public void showView()             { setVisible(true); toFront(); }
    public void hideView()             { setVisible(false); }
    public JFrame getFrame()           { return this; }
}