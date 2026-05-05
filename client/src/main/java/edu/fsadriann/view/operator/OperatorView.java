package edu.fsadriann.view.operator;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.function.Function;

public class OperatorView extends JFrame {

    private static final Color BG       = Color.WHITE;
    private static final Color BG2      = new Color(245, 245, 245);
    private static final Color BLUE     = new Color(24, 95, 165);
    private static final Color BLUE_BG  = new Color(230, 241, 251);
    private static final Color TEXT     = new Color(30, 30, 30);
    private static final Color TEXT2    = new Color(100, 100, 100);
    private static final Color TEXT3    = new Color(150, 150, 150);
    private static final Color BORDER_C = new Color(220, 220, 220);
    private static final Color GREEN    = new Color(40, 167, 69);
    private static final Color GREEN_BG = new Color(212, 237, 218);
    private static final Color GREEN_FG = new Color(21, 87, 36);

    // ── Step indicator ────────────────────────────────────────────────────────
    private JLabel[] stepLabels;

    // ── Left panel — identification ───────────────────────────────────────────
    private JTextField searchPhoneField;
    private JButton    searchClientBtn;

    private JPanel clientInfoPanel;
    private JLabel clientNameLabel;
    private JLabel clientAddressLabel;
    private JLabel clientPhoneLabel;
    private JLabel clientTipoLabel;       // FIX: muestra Estándar / Premium

    private JPanel frequentOrdersSection;
    private JPanel frequentOrdersListPanel;

    private DefaultTableModel clientTableModel;
    private DefaultTableModel recentOrdersModel;

    // ── Right panel — products ────────────────────────────────────────────────
    private JTextField        searchProductField;
    private JButton           searchProductBtn;
    private JButton           loadProductsBtn;
    private DefaultTableModel productsTableModel;
    private JTable            productsTable;

    // ── Right panel — current order ───────────────────────────────────────────
    // FIX: 4 columnas — Producto | Cant. | Precio unit. | Subtotal
    private DefaultTableModel currentOrderModel;
    private JTable            currentOrderTable;
    private JButton           removeProductBtn;
    private JButton           changeQuantityBtn;
    private JButton           clearOrderBtn;
    private JButton           generateInvoiceBtn;

    // ── Right panel — invoice ─────────────────────────────────────────────────
    private JLabel            invoiceClientVal;
    private JLabel            invoiceTipoVal;
    private JLabel            invoiceDireccionVal;
    private JLabel            invoiceCuadranteVal;
    private JLabel            invoiceSubtotalVal;
    private JLabel            invoiceIvaVal;
    private JLabel            invoiceDomicilioVal;
    private JLabel            invoiceTotalVal;
    // FIX: tabla de factura también con 4 columnas
    private DefaultTableModel invoiceOrderModel;
    private JTable            invoiceOrderTable;
    private JButton           clearInvoiceOrderBtn;
    private JButton           sendToKitchenBtn;

    // ── Topbar / status ───────────────────────────────────────────────────────
    private JButton logoutBtn;
    private JLabel  statusLabel;

    public OperatorView(String operadorEmail) {
        super("Food UPB — Operador");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1150, 700));
        setBackground(BG);
    }

    public void initComponents(Function<String, Void> onSearch) {
        initModels();
        wireModelListeners();

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG2);
        root.add(buildTopbar(),          BorderLayout.NORTH);
        root.add(buildContent(onSearch), BorderLayout.CENTER);
        root.add(buildStatusBar(),       BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MODEL INIT
    // ─────────────────────────────────────────────────────────────────────────

    private void initModels() {
        clientTableModel = new DefaultTableModel(
                new Object[]{"Nombres", "Correo", "Teléfono", "Tipo"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        recentOrdersModel = new DefaultTableModel(
                new Object[]{"Pedido", "Estado", "Ítems", "Total"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        productsTableModel = new DefaultTableModel(
                new Object[]{"ID", "Nombre", "Categoría", "Precio", "Disponible"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        // FIX: 4 columnas en el pedido actual
        currentOrderModel = new DefaultTableModel(
                new Object[]{"Producto", "Cant.", "Precio unit.", "Subtotal"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        // FIX: 4 columnas en la tabla de factura
        invoiceOrderModel = new DefaultTableModel(
                new Object[]{"Producto", "Cant.", "Precio unit.", "Subtotal"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    private void wireModelListeners() {
        clientTableModel.addTableModelListener(e ->
                SwingUtilities.invokeLater(this::refreshClientCard));
        recentOrdersModel.addTableModelListener(e ->
                SwingUtilities.invokeLater(this::refreshFrequentOrders));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TOP BAR
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildTopbar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(BG);
        top.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER_C),
                new EmptyBorder(10, 20, 10, 20)
        ));

        JLabel title = new JLabel("Food UPB — Operador");
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        title.setForeground(BLUE);

        logoutBtn = new JButton("Cerrar sesión");
        logoutBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        top.add(title,     BorderLayout.WEST);
        top.add(logoutBtn, BorderLayout.EAST);
        return top;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STEP INDICATOR
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildStepIndicator() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setBackground(BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER_C),
                new EmptyBorder(8, 20, 8, 20)
        ));

        String[] steps = {"1  Identificar", "2  Pedido", "3  Factura", "4  Confirmar"};
        stepLabels = new JLabel[4];
        for (int i = 0; i < steps.length; i++) {
            if (i > 0) {
                JLabel arrow = new JLabel("  →  ");
                arrow.setFont(new Font("SansSerif", Font.PLAIN, 11));
                arrow.setForeground(TEXT3);
                panel.add(arrow);
            }
            stepLabels[i] = new JLabel(steps[i]);
            stepLabels[i].setBorder(new EmptyBorder(3, 10, 3, 10));
            panel.add(stepLabels[i]);
        }
        applyStepStyle(0);
        return panel;
    }

    private void applyStepStyle(int activeIndex) {
        if (stepLabels == null) return;
        for (int i = 0; i < stepLabels.length; i++) {
            JLabel lbl = stepLabels[i];
            if (i < activeIndex) {
                lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
                lbl.setForeground(GREEN_FG);
                lbl.setBackground(GREEN_BG);
                lbl.setOpaque(true);
            } else if (i == activeIndex) {
                lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
                lbl.setForeground(BLUE);
                lbl.setBackground(BLUE_BG);
                lbl.setOpaque(true);
            } else {
                lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
                lbl.setForeground(TEXT3);
                lbl.setBackground(null);
                lbl.setOpaque(false);
            }
            lbl.repaint();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MAIN CONTENT
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildContent(Function<String, Void> onSearch) {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(BG2);
        content.add(buildStepIndicator(), BorderLayout.NORTH);

        JPanel split = new JPanel(new BorderLayout(12, 0));
        split.setBackground(BG2);
        split.setBorder(new EmptyBorder(14, 16, 14, 16));
        split.add(buildLeftPanel(),           BorderLayout.WEST);
        split.add(buildRightPanel(onSearch),  BorderLayout.CENTER);

        content.add(split, BorderLayout.CENTER);
        return content;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LEFT PANEL
    // ─────────────────────────────────────────────────────────────────────────

    private JScrollPane buildLeftPanel() {
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(BG2);

        // Search card
        JPanel searchCard = new JPanel();
        searchCard.setLayout(new BoxLayout(searchCard, BoxLayout.Y_AXIS));
        searchCard.setBackground(BG);
        searchCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_C), new EmptyBorder(10, 12, 10, 12)));
        searchCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JLabel identLabel = secLabel("Identificación");
        identLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchCard.add(identLabel);
        searchCard.add(Box.createVerticalStrut(10));

        JPanel searchRow = new JPanel(new BorderLayout(6, 0));
        searchRow.setOpaque(false);
        searchRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        searchPhoneField = new JTextField("Teléfono (10 dígitos)");
        searchPhoneField.setFont(new Font("SansSerif", Font.PLAIN, 12));
        searchPhoneField.setForeground(TEXT2);
        searchPhoneField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_C), new EmptyBorder(4, 8, 4, 8)));
        searchClientBtn = smallBtn("Buscar", BLUE, Color.WHITE);
        searchRow.add(searchPhoneField, BorderLayout.CENTER);
        searchRow.add(searchClientBtn,  BorderLayout.EAST);
        searchCard.add(searchRow);

        // Client info card (hidden until client found)
        clientInfoPanel = new JPanel();
        clientInfoPanel.setLayout(new BoxLayout(clientInfoPanel, BoxLayout.Y_AXIS));
        clientInfoPanel.setBackground(BG);
        clientInfoPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_C), new EmptyBorder(12, 14, 12, 14)));
        clientInfoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        clientInfoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        clientNameLabel = new JLabel("—");
        clientNameLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        clientNameLabel.setForeground(TEXT);

        clientAddressLabel = new JLabel("—");
        clientAddressLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        clientAddressLabel.setForeground(TEXT2);
        clientAddressLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        clientPhoneLabel = new JLabel("—");
        clientPhoneLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        clientPhoneLabel.setForeground(TEXT3);
        clientPhoneLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // FIX: etiqueta de tipo de cliente (Premium / Estándar)
        clientTipoLabel = new JLabel("—");
        clientTipoLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        clientTipoLabel.setForeground(BLUE);
        clientTipoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        clientInfoPanel.add(clientNameLabel);
        clientInfoPanel.add(Box.createVerticalStrut(4));
        clientInfoPanel.add(clientAddressLabel);
        clientInfoPanel.add(Box.createVerticalStrut(2));
        clientInfoPanel.add(clientPhoneLabel);
        clientInfoPanel.add(Box.createVerticalStrut(4));
        clientInfoPanel.add(clientTipoLabel);
        clientInfoPanel.setVisible(false);

        // Frequent orders (hidden until loaded)
        frequentOrdersSection = new JPanel();
        frequentOrdersSection.setLayout(new BoxLayout(frequentOrdersSection, BoxLayout.Y_AXIS));
        frequentOrdersSection.setOpaque(false);
        frequentOrdersSection.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel freqLabel = secLabel("Pedidos frecuentes");
        freqLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        frequentOrdersSection.add(freqLabel);
        frequentOrdersSection.add(Box.createVerticalStrut(6));

        frequentOrdersListPanel = new JPanel();
        frequentOrdersListPanel.setLayout(new BoxLayout(frequentOrdersListPanel, BoxLayout.Y_AXIS));
        frequentOrdersListPanel.setOpaque(false);
        frequentOrdersListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        frequentOrdersSection.add(frequentOrdersListPanel);
        frequentOrdersSection.setVisible(false);

        left.add(searchCard);
        left.add(Box.createVerticalStrut(10));
        left.add(clientInfoPanel);
        left.add(Box.createVerticalStrut(10));
        left.add(frequentOrdersSection);
        left.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(left);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG2);
        scroll.setPreferredSize(new Dimension(272, 0));
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    // FIX: ahora lee también la columna "Tipo" (índice 3) del clientTableModel
    private void refreshClientCard() {
        if (clientTableModel == null || clientTableModel.getRowCount() == 0) {
            clientInfoPanel.setVisible(false);
            return;
        }
        String name  = String.valueOf(clientTableModel.getValueAt(0, 0));
        String phone = String.valueOf(clientTableModel.getValueAt(0, 2));
        String tipo  = clientTableModel.getColumnCount() > 3
                ? String.valueOf(clientTableModel.getValueAt(0, 3)) : "";

        clientNameLabel.setText(name);
        clientPhoneLabel.setText(phone);

        if (!tipo.isBlank() && !tipo.equals("null")) {
            clientTipoLabel.setText(tipo);
            clientTipoLabel.setForeground(
                    tipo.toLowerCase().contains("premium") ? GREEN_FG : TEXT2);
            clientTipoLabel.setVisible(true);
        } else {
            clientTipoLabel.setVisible(false);
        }

        clientInfoPanel.setVisible(true);
        clientInfoPanel.revalidate();
        clientInfoPanel.repaint();
    }

    private void refreshFrequentOrders() {
        frequentOrdersListPanel.removeAll();
        int count = recentOrdersModel != null ? recentOrdersModel.getRowCount() : 0;
        for (int i = 0; i < count; i++) {
            String id    = String.valueOf(recentOrdersModel.getValueAt(i, 0));
            String items = String.valueOf(recentOrdersModel.getValueAt(i, 2));
            String total = String.valueOf(recentOrdersModel.getValueAt(i, 3));

            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setBackground(BG);
            row.setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(0, 0, 1, 0, BORDER_C),
                    new EmptyBorder(6, 8, 6, 8)));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel idLabel = new JLabel(id + "  ·  " + items + " ítem(s)");
            idLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
            idLabel.setForeground(TEXT2);

            JLabel totalLabel = new JLabel(total);
            totalLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
            totalLabel.setForeground(TEXT);

            row.add(idLabel,    BorderLayout.WEST);
            row.add(totalLabel, BorderLayout.EAST);
            frequentOrdersListPanel.add(row);
        }
        frequentOrdersSection.setVisible(count > 0);
        frequentOrdersSection.revalidate();
        frequentOrdersSection.repaint();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RIGHT PANEL
    // ─────────────────────────────────────────────────────────────────────────

    private JScrollPane buildRightPanel(Function<String, Void> onSearch) {
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBackground(BG2);

        right.add(buildProductsCard(onSearch));
        right.add(Box.createVerticalStrut(10));
        right.add(buildOrderCard());
        right.add(Box.createVerticalStrut(10));
        right.add(buildInvoiceCard());
        right.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(right);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG2);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    private JPanel buildProductsCard(Function<String, Void> onSearch) {
        JPanel card = sectionCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(secLabel("Menú"), BorderLayout.WEST);

        searchProductField = new JTextField("Buscar producto...");
        searchProductField.setFont(new Font("SansSerif", Font.PLAIN, 12));
        searchProductField.setForeground(TEXT2);
        searchProductField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_C), new EmptyBorder(4, 8, 4, 8)));
        searchProductField.setPreferredSize(new Dimension(160, 28));
        searchProductField.setMaximumSize(new Dimension(180, 28));

        searchProductBtn = smallBtn("Buscar", BG, TEXT);
        searchProductBtn.addActionListener(e -> onSearch.apply(searchProductField.getText().trim()));

        loadProductsBtn = smallBtn("Cargar productos", BLUE, Color.WHITE);

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        searchRow.setOpaque(false);
        searchRow.add(searchProductField);
        searchRow.add(searchProductBtn);
        searchRow.add(loadProductsBtn);
        header.add(searchRow, BorderLayout.EAST);

        productsTable = styledTable(productsTableModel);
        JScrollPane scroll = tableScroll(productsTable, 200);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(header);
        card.add(Box.createVerticalStrut(10));
        card.add(scroll);

        // Ayuda UX: doble clic muestra precio antes de agregar
        JLabel hint = new JLabel("  Doble clic en un producto para agregarlo al pedido");
        hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hint.setForeground(TEXT3);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(Box.createVerticalStrut(4));
        card.add(hint);

        return card;
    }

    private JPanel buildOrderCard() {
        JPanel card = sectionCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.add(secLabel("Pedido actual"));
        card.add(Box.createVerticalStrut(10));

        currentOrderTable = styledTable(currentOrderModel);
        JScrollPane scroll = tableScroll(currentOrderTable, 150);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(scroll);
        card.add(Box.createVerticalStrut(10));

        removeProductBtn   = smallBtn("Quitar",           BG, TEXT);
        changeQuantityBtn  = smallBtn("Cambiar cantidad",  BG, TEXT);
        clearOrderBtn      = smallBtn("Limpiar pedido",    BG, TEXT);
        generateInvoiceBtn = smallBtn("Generar factura",   BLUE, Color.WHITE);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btns.setOpaque(false);
        btns.setAlignmentX(Component.LEFT_ALIGNMENT);
        btns.add(removeProductBtn);
        btns.add(changeQuantityBtn);
        btns.add(clearOrderBtn);
        btns.add(generateInvoiceBtn);
        card.add(btns);
        return card;
    }

    private JPanel buildInvoiceCard() {
        JPanel card = sectionCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.add(secLabel("Factura"));
        card.add(Box.createVerticalStrut(12));

        invoiceClientVal    = new JLabel("—");
        invoiceTipoVal      = new JLabel("—");
        invoiceDireccionVal = new JLabel("—");
        invoiceCuadranteVal = new JLabel("—");
        invoiceSubtotalVal  = new JLabel("$0");
        invoiceIvaVal       = new JLabel("$0");
        invoiceDomicilioVal = new JLabel("$0");
        invoiceTotalVal     = new JLabel("$0");

        card.add(invRow("Cliente",   invoiceClientVal));
        card.add(Box.createVerticalStrut(4));
        card.add(invRow("Tipo",      invoiceTipoVal));
        card.add(Box.createVerticalStrut(4));
        card.add(invRow("Dirección", invoiceDireccionVal));
        card.add(Box.createVerticalStrut(4));
        card.add(invRow("Cuadrante", invoiceCuadranteVal));
        card.add(Box.createVerticalStrut(10));
        card.add(hLine());
        card.add(Box.createVerticalStrut(10));
        card.add(invRow("Subtotal",  invoiceSubtotalVal));
        card.add(Box.createVerticalStrut(4));
        card.add(invRow("IVA (19%)", invoiceIvaVal));
        card.add(Box.createVerticalStrut(4));
        card.add(invRow("Domicilio", invoiceDomicilioVal));
        card.add(Box.createVerticalStrut(8));
        card.add(invTotalRow());
        card.add(Box.createVerticalStrut(12));

        invoiceOrderTable = styledTable(invoiceOrderModel);
        JScrollPane invScroll = tableScroll(invoiceOrderTable, 110);
        invScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(invScroll);
        card.add(Box.createVerticalStrut(12));

        clearInvoiceOrderBtn = smallBtn("Limpiar factura", BG, TEXT);
        sendToKitchenBtn = new JButton("Enviar a cocina");
        sendToKitchenBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        sendToKitchenBtn.setBackground(GREEN);
        sendToKitchenBtn.setForeground(Color.WHITE);
        sendToKitchenBtn.setOpaque(true);
        sendToKitchenBtn.setBorderPainted(false);
        sendToKitchenBtn.setFocusPainted(false);
        sendToKitchenBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel sendRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        sendRow.setOpaque(false);
        sendRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        sendRow.add(clearInvoiceOrderBtn);
        sendRow.add(sendToKitchenBtn);
        card.add(sendRow);
        return card;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STATUS BAR
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG);
        bar.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, BORDER_C),
                new EmptyBorder(6, 20, 6, 20)
        ));
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT2);
        bar.add(statusLabel, BorderLayout.WEST);
        return bar;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel sectionCard() {
        JPanel p = new JPanel();
        p.setBackground(BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_C), new EmptyBorder(12, 14, 12, 14)));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    private JLabel secLabel(String text) {
        JLabel lbl = new JLabel(text.toUpperCase());
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl.setForeground(TEXT3);
        return lbl;
    }

    private JButton smallBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (bg.equals(BG)) {
            btn.setBorderPainted(true);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER_C), new EmptyBorder(4, 10, 4, 10)));
        } else {
            btn.setBorderPainted(false);
            btn.setBorder(new EmptyBorder(4, 10, 4, 10));
        }
        return btn;
    }

    private JPanel invRow(String labelText, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(TEXT2);
        valueLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        valueLabel.setForeground(TEXT);
        row.add(lbl,        BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);
        return row;
    }

    private JPanel invTotalRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel("Total");
        lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        lbl.setForeground(TEXT);
        invoiceTotalVal.setFont(new Font("SansSerif", Font.BOLD, 14));
        invoiceTotalVal.setForeground(TEXT);
        row.add(lbl,             BorderLayout.WEST);
        row.add(invoiceTotalVal, BorderLayout.EAST);
        return row;
    }

    private JPanel hLine() {
        JPanel line = new JPanel();
        line.setBackground(BORDER_C);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        return line;
    }

    private JTable styledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setForeground(TEXT);
        table.setBackground(BG);
        table.setRowHeight(28);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        table.getTableHeader().setForeground(TEXT2);
        table.getTableHeader().setBackground(BG2);
        table.getTableHeader().setBorder(new MatteBorder(0, 0, 1, 0, BORDER_C));
        table.setSelectionBackground(BLUE_BG);
        table.setSelectionForeground(TEXT);
        table.setBorder(null);
        return table;
    }

    private JScrollPane tableScroll(JTable table, int height) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_C));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        scroll.setPreferredSize(new Dimension(0, height));
        scroll.getViewport().setBackground(BG);
        return scroll;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC API
    // ─────────────────────────────────────────────────────────────────────────

    public void setMessage(String message) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(message));
    }

    public void setRegisterEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            if (searchProductBtn     != null) searchProductBtn.setEnabled(enabled);
            if (generateInvoiceBtn   != null) generateInvoiceBtn.setEnabled(enabled);
            if (clearOrderBtn        != null) clearOrderBtn.setEnabled(enabled);
            if (sendToKitchenBtn     != null) sendToKitchenBtn.setEnabled(enabled);
            if (clearInvoiceOrderBtn != null) clearInvoiceOrderBtn.setEnabled(enabled);
        });
    }

    // Tab 1
    public String            getSearchPhone()                      { return searchPhoneField.getText().trim(); }
    public void              addSearchClientListener(Runnable r)   { searchClientBtn.addActionListener(e -> r.run()); }
    public DefaultTableModel getClientTableModel()                 { return clientTableModel; }
    public DefaultTableModel getRecentOrdersModel()                { return recentOrdersModel; }
    public int               getSelectedClientRow()                { return 0; }

    // Tab 2
    public String            getSearchProduct()                    { return searchProductField.getText().trim(); }
    public void              addLoadProductsListener(Runnable r)   { loadProductsBtn.addActionListener(e -> r.run()); }
    public DefaultTableModel getProductsTableModel()               { return productsTableModel; }
    public JTable            getProductsTable()                    { return productsTable; }
    public DefaultTableModel getCurrentOrderModel()                { return currentOrderModel; }
    public int               getSelectedCurrentOrderRow()          { return currentOrderTable != null ? currentOrderTable.getSelectedRow() : -1; }
    public int               getSelectedProductRow()               { return productsTable != null ? productsTable.getSelectedRow() : -1; }
    public void              addGenerateInvoiceListener(Runnable r){ generateInvoiceBtn.addActionListener(e -> r.run()); }
    public void              addRemoveProductListener(Runnable r)  { removeProductBtn.addActionListener(e -> r.run()); }
    public void              addChangeQuantityListener(Runnable r) { changeQuantityBtn.addActionListener(e -> r.run()); }
    public void              addClearOrderListener(Runnable r)     { clearOrderBtn.addActionListener(e -> r.run()); }

    // Tab 3
    public void              setInvoiceClient(String v)             { SwingUtilities.invokeLater(() -> invoiceClientVal.setText(v)); }
    public void              setInvoiceTipo(String v)               { SwingUtilities.invokeLater(() -> invoiceTipoVal.setText(v)); }
    public void              setInvoiceDireccion(String v)          { SwingUtilities.invokeLater(() -> invoiceDireccionVal.setText(v)); }
    public void              setInvoiceCuadrante(String v)          { SwingUtilities.invokeLater(() -> invoiceCuadranteVal.setText(v)); }
    public void              setInvoiceSubtotal(String v)           { SwingUtilities.invokeLater(() -> invoiceSubtotalVal.setText(v)); }
    public void              setInvoiceIva(String v)                { SwingUtilities.invokeLater(() -> invoiceIvaVal.setText(v)); }
    public void              setInvoiceDomicilio(String v)          { SwingUtilities.invokeLater(() -> invoiceDomicilioVal.setText(v)); }
    public void              setInvoiceTotal(String v)              { SwingUtilities.invokeLater(() -> invoiceTotalVal.setText(v)); }
    public DefaultTableModel getInvoiceOrderModel()                 { return invoiceOrderModel; }
    public void              addClearInvoiceOrderListener(Runnable r){ clearInvoiceOrderBtn.addActionListener(e -> r.run()); }
    public void              addSendToKitchenListener(Runnable r)   { sendToKitchenBtn.addActionListener(e -> r.run()); }

    // Navigation / shared
    public void addLogoutListener(Runnable r) { logoutBtn.addActionListener(e -> r.run()); }
    public void showTab(int index)            { SwingUtilities.invokeLater(() -> applyStepStyle(index)); }
    public void showError(String msg)         { JOptionPane.showMessageDialog(this, msg, "Error",  JOptionPane.ERROR_MESSAGE); }
    public void showSuccess(String msg)       { JOptionPane.showMessageDialog(this, msg, "Éxito",  JOptionPane.INFORMATION_MESSAGE); }
    public boolean confirm(String msg)        { return JOptionPane.showConfirmDialog(this, msg, "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION; }
}