package edu.fsadriann.view.operator;

import edu.fsadriann.view.UIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.function.Function;

public class OperatorView extends JFrame {

    // ── Step indicator ────────────────────────────────────────────────────────
    private JLabel[] stepLabels;

    // ── Left panel — identification ───────────────────────────────────────────
    private JTextField   searchPhoneField;
    private JButton      searchClientBtn;

    // Client card (hidden until a client is found)
    private JPanel  clientInfoPanel;
    private JLabel  clientNameLabel;
    private JLabel  clientBadgeLabel;
    private JLabel  clientAddressLabel;
    private JLabel  clientPhoneLabel;

    // Frequent orders (hidden until loaded)
    private JPanel  frequentOrdersSection;
    private JPanel  frequentOrdersListPanel;

    // Backing models for left-panel data (controller writes here, listeners update UI)
    private DefaultTableModel clientTableModel;
    private DefaultTableModel recentOrdersModel;

    // ── Right panel — products ────────────────────────────────────────────────
    private JTextField        searchProductField;
    private JButton           searchProductBtn;
    private DefaultTableModel productsTableModel;
    private JTable            productsTable;

    // ── Right panel — current order ───────────────────────────────────────────
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
        setMinimumSize(new Dimension(1100, 680));
        setBackground(UIComponents.BG);
    }

    // Called by OperatorController.init() ─────────────────────────────────────
    public void initComponents(Function<String, Void> onSearch) {
        initModels();
        wireModelListeners();

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIComponents.BG2);
        root.add(buildTopbar(),                BorderLayout.NORTH);
        root.add(buildContent(onSearch),       BorderLayout.CENTER);
        root.add(buildStatusBar(),             BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MODEL INIT
    // ─────────────────────────────────────────────────────────────────────────

    private void initModels() {
        clientTableModel = new DefaultTableModel(new Object[]{"Nombres", "Correo", "Teléfono"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        recentOrdersModel = new DefaultTableModel(new Object[]{"Pedido", "Estado", "Ítems", "Total"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        productsTableModel = new DefaultTableModel(new Object[]{"Producto", "Precio", "Disponibilidad"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        currentOrderModel = new DefaultTableModel(new Object[]{"Producto", "Cant."}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        invoiceOrderModel = new DefaultTableModel(new Object[]{"Producto", "Cant."}, 0) {
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
        JLabel section = new JLabel("Operador");
        section.setFont(UIComponents.fontBold(14));
        section.setForeground(UIComponents.ACCENT);
        section.setBorder(new EmptyBorder(0, 12, 0, 0));
        left.add(brand);
        left.add(sep);
        left.add(section);

        logoutBtn = UIComponents.roundBtnSmall("Cerrar sesión", UIComponents.BG, UIComponents.TXT, UIComponents.BORDER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(UIComponents.badge("Operador", UIComponents.INFO_BG, UIComponents.INFO_FG));
        right.add(logoutBtn);

        top.add(left,  BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);
        return top;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STEP INDICATOR
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildStepIndicator() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setBackground(UIComponents.BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIComponents.BORDER),
            new EmptyBorder(8, 20, 8, 20)
        ));

        String[] steps = {"1  Identificar", "2  Pedido", "3  Factura", "4  Confirmar"};
        stepLabels = new JLabel[4];
        for (int i = 0; i < steps.length; i++) {
            if (i > 0) {
                JLabel arrow = new JLabel("  →  ");
                arrow.setFont(UIComponents.fontPlain(11));
                arrow.setForeground(UIComponents.TXT3);
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
                lbl.setFont(UIComponents.fontBold(11));
                lbl.setForeground(UIComponents.SUCCESS_FG);
                lbl.setBackground(UIComponents.SUCCESS_BG);
                lbl.setOpaque(true);
            } else if (i == activeIndex) {
                lbl.setFont(UIComponents.fontBold(11));
                lbl.setForeground(UIComponents.INFO_FG);
                lbl.setBackground(UIComponents.INFO_BG);
                lbl.setOpaque(true);
            } else {
                lbl.setFont(UIComponents.fontPlain(11));
                lbl.setForeground(UIComponents.TXT3);
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
        content.setBackground(UIComponents.BG2);
        content.add(buildStepIndicator(), BorderLayout.NORTH);

        JPanel split = new JPanel(new BorderLayout(12, 0));
        split.setBackground(UIComponents.BG2);
        split.setBorder(new EmptyBorder(14, 16, 14, 16));
        split.add(buildLeftPanel(),            BorderLayout.WEST);
        split.add(buildRightPanel(onSearch),   BorderLayout.CENTER);

        content.add(split, BorderLayout.CENTER);
        return content;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LEFT PANEL — Identificación + cliente + frecuentes
    // ─────────────────────────────────────────────────────────────────────────

    private JScrollPane buildLeftPanel() {
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(UIComponents.BG2);

        // ── Search section ─────────────────────────────────────────────────
        JPanel searchCard = UIComponents.card();
        searchCard.setLayout(new BoxLayout(searchCard, BoxLayout.Y_AXIS));
        searchCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JLabel identLabel = secLabel("Identificación");
        identLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchCard.add(identLabel);
        searchCard.add(Box.createVerticalStrut(10));

        JPanel searchRow = new JPanel(new BorderLayout(6, 0));
        searchRow.setOpaque(false);
        searchRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        searchPhoneField = UIComponents.styledField("Teléfono (10 dígitos)");
        searchClientBtn  = UIComponents.roundBtnSmall("Buscar", UIComponents.ACCENT, Color.WHITE, null);
        searchRow.add(searchPhoneField, BorderLayout.CENTER);
        searchRow.add(searchClientBtn,  BorderLayout.EAST);
        searchCard.add(searchRow);

        // ── Client info card (hidden by default) ───────────────────────────
        clientInfoPanel = new JPanel();
        clientInfoPanel.setLayout(new BoxLayout(clientInfoPanel, BoxLayout.Y_AXIS));
        clientInfoPanel.setBackground(UIComponents.BG);
        clientInfoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 1, 1, 1, UIComponents.BORDER),
            new EmptyBorder(12, 14, 12, 14)
        ));
        clientInfoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        clientInfoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JPanel nameRow = new JPanel(new BorderLayout(6, 0));
        nameRow.setOpaque(false);
        nameRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        clientNameLabel  = new JLabel("—");
        clientNameLabel.setFont(UIComponents.fontBold(13));
        clientNameLabel.setForeground(UIComponents.TXT);
        clientBadgeLabel = UIComponents.badge("Estándar", UIComponents.INFO_BG, UIComponents.INFO_FG);
        nameRow.add(clientNameLabel,  BorderLayout.WEST);
        nameRow.add(clientBadgeLabel, BorderLayout.EAST);

        clientAddressLabel = new JLabel("—");
        clientAddressLabel.setFont(UIComponents.fontPlain(12));
        clientAddressLabel.setForeground(UIComponents.TXT2);
        clientAddressLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        clientPhoneLabel = new JLabel("—");
        clientPhoneLabel.setFont(UIComponents.fontPlain(11));
        clientPhoneLabel.setForeground(UIComponents.TXT3);
        clientPhoneLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        clientInfoPanel.add(nameRow);
        clientInfoPanel.add(Box.createVerticalStrut(4));
        clientInfoPanel.add(clientAddressLabel);
        clientInfoPanel.add(Box.createVerticalStrut(2));
        clientInfoPanel.add(clientPhoneLabel);
        clientInfoPanel.setVisible(false);

        // ── Frequent orders section (hidden by default) ────────────────────
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
        scroll.getViewport().setBackground(UIComponents.BG2);
        scroll.setPreferredSize(new Dimension(272, 0));
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    private void refreshClientCard() {
        if (clientTableModel == null || clientTableModel.getRowCount() == 0) {
            clientInfoPanel.setVisible(false);
            return;
        }
        String name  = String.valueOf(clientTableModel.getValueAt(0, 0));
        String phone = String.valueOf(clientTableModel.getValueAt(0, 2));
        clientNameLabel.setText(name);
        clientPhoneLabel.setText(phone);
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
            row.setBackground(UIComponents.BG);
            row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIComponents.BORDER),
                new EmptyBorder(6, 8, 6, 8)
            ));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel idLabel = new JLabel(id + "  ·  " + items + " ítem(s)");
            idLabel.setFont(UIComponents.fontPlain(11));
            idLabel.setForeground(UIComponents.TXT2);

            JLabel totalLabel = new JLabel(total);
            totalLabel.setFont(UIComponents.fontBold(11));
            totalLabel.setForeground(UIComponents.TXT);

            row.add(idLabel,    BorderLayout.WEST);
            row.add(totalLabel, BorderLayout.EAST);
            frequentOrdersListPanel.add(row);
        }
        frequentOrdersSection.setVisible(count > 0);
        frequentOrdersSection.revalidate();
        frequentOrdersSection.repaint();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RIGHT PANEL — Menú + Pedido + Factura
    // ─────────────────────────────────────────────────────────────────────────

    private JScrollPane buildRightPanel(Function<String, Void> onSearch) {
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBackground(UIComponents.BG2);
        right.setBorder(new EmptyBorder(0, 0, 0, 0));

        right.add(buildProductsCard(onSearch));
        right.add(Box.createVerticalStrut(10));
        right.add(buildOrderCard());
        right.add(Box.createVerticalStrut(10));
        right.add(buildInvoiceCard());
        right.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(right);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIComponents.BG2);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    // ── Products card ─────────────────────────────────────────────────────────

    private JPanel buildProductsCard(Function<String, Void> onSearch) {
        JPanel card = UIComponents.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(secLabel("Menú"), BorderLayout.WEST);

        searchProductField = UIComponents.styledField("Buscar producto...");
        searchProductField.setPreferredSize(new Dimension(160, 32));
        searchProductField.setMaximumSize(new Dimension(180, 32));
        searchProductBtn = UIComponents.roundBtnSmall("Buscar", UIComponents.BG, UIComponents.TXT, UIComponents.BORDER);
        searchProductBtn.addActionListener(e -> onSearch.apply(searchProductField.getText().trim()));

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        searchRow.setOpaque(false);
        searchRow.add(searchProductField);
        searchRow.add(searchProductBtn);
        header.add(searchRow, BorderLayout.EAST);

        productsTable = styledTable(productsTableModel);
        JScrollPane scroll = tableScroll(productsTable, 200);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(header);
        card.add(Box.createVerticalStrut(10));
        card.add(scroll);
        return card;
    }

    // ── Order card ────────────────────────────────────────────────────────────

    private JPanel buildOrderCard() {
        JPanel card = UIComponents.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(secLabel("Pedido actual"));
        card.add(Box.createVerticalStrut(10));

        currentOrderTable = styledTable(currentOrderModel);
        JScrollPane scroll = tableScroll(currentOrderTable, 130);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(scroll);
        card.add(Box.createVerticalStrut(10));

        removeProductBtn  = UIComponents.roundBtnSmall("Quitar",           UIComponents.BG,     UIComponents.TXT, UIComponents.BORDER);
        changeQuantityBtn = UIComponents.roundBtnSmall("Cambiar cantidad",  UIComponents.BG,     UIComponents.TXT, UIComponents.BORDER);
        clearOrderBtn     = UIComponents.roundBtnSmall("Limpiar pedido",    UIComponents.BG,     UIComponents.TXT, UIComponents.BORDER);
        generateInvoiceBtn= UIComponents.roundBtnSmall("Generar factura",   UIComponents.ACCENT, Color.WHITE,      null);

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

    // ── Invoice card ──────────────────────────────────────────────────────────

    private JPanel buildInvoiceCard() {
        JPanel card = UIComponents.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

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
        JScrollPane invScroll = tableScroll(invoiceOrderTable, 90);
        invScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(invScroll);
        card.add(Box.createVerticalStrut(12));

        clearInvoiceOrderBtn = UIComponents.roundBtnSmall("Limpiar factura", UIComponents.BG,      UIComponents.TXT, UIComponents.BORDER);
        sendToKitchenBtn     = UIComponents.roundBtn("Enviar a cocina",       UIComponents.SUCCESS, Color.WHITE,      null);
        sendToKitchenBtn.setFont(UIComponents.fontBold(13));

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
        bar.setBackground(UIComponents.BG);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, UIComponents.BORDER),
            new EmptyBorder(6, 20, 6, 20)
        ));
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIComponents.fontPlain(12));
        statusLabel.setForeground(UIComponents.TXT2);
        bar.add(statusLabel, BorderLayout.WEST);
        return bar;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private JLabel secLabel(String text) {
        JLabel lbl = new JLabel(text.toUpperCase());
        lbl.setFont(UIComponents.fontBold(10));
        lbl.setForeground(UIComponents.TXT3);
        return lbl;
    }

    private JPanel invRow(String labelText, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UIComponents.fontPlain(12));
        lbl.setForeground(UIComponents.TXT2);
        valueLabel.setFont(UIComponents.fontPlain(12));
        valueLabel.setForeground(UIComponents.TXT);
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
        lbl.setFont(UIComponents.fontBold(14));
        lbl.setForeground(UIComponents.TXT);
        invoiceTotalVal.setFont(UIComponents.fontBold(14));
        invoiceTotalVal.setForeground(UIComponents.TXT);
        row.add(lbl,            BorderLayout.WEST);
        row.add(invoiceTotalVal, BorderLayout.EAST);
        return row;
    }

    private JPanel hLine() {
        JPanel line = new JPanel();
        line.setBackground(UIComponents.BORDER);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        return line;
    }

    private JTable styledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(UIComponents.fontPlain(12));
        table.setForeground(UIComponents.TXT);
        table.setBackground(UIComponents.BG);
        table.setRowHeight(28);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(UIComponents.fontBold(11));
        table.getTableHeader().setForeground(UIComponents.TXT2);
        table.getTableHeader().setBackground(UIComponents.BG2);
        table.getTableHeader().setBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIComponents.BORDER));
        table.setSelectionBackground(UIComponents.INFO_BG);
        table.setSelectionForeground(UIComponents.TXT);
        table.setBorder(null);
        return table;
    }

    private JScrollPane tableScroll(JTable table, int height) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIComponents.BORDER));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        scroll.setPreferredSize(new Dimension(0, height));
        scroll.getViewport().setBackground(UIComponents.BG);
        return scroll;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MVC CONTRACT — same public API as before
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
    public void              setInvoiceClient(String v)            { SwingUtilities.invokeLater(() -> invoiceClientVal.setText(v)); }
    public void              setInvoiceTipo(String v)              { SwingUtilities.invokeLater(() -> invoiceTipoVal.setText(v)); }
    public void              setInvoiceDireccion(String v)         { SwingUtilities.invokeLater(() -> invoiceDireccionVal.setText(v)); }
    public void              setInvoiceCuadrante(String v)         { SwingUtilities.invokeLater(() -> invoiceCuadranteVal.setText(v)); }
    public void              setInvoiceSubtotal(String v)          { SwingUtilities.invokeLater(() -> invoiceSubtotalVal.setText(v)); }
    public void              setInvoiceIva(String v)               { SwingUtilities.invokeLater(() -> invoiceIvaVal.setText(v)); }
    public void              setInvoiceDomicilio(String v)         { SwingUtilities.invokeLater(() -> invoiceDomicilioVal.setText(v)); }
    public void              setInvoiceTotal(String v)             { SwingUtilities.invokeLater(() -> invoiceTotalVal.setText(v)); }
    public DefaultTableModel getInvoiceOrderModel()                { return invoiceOrderModel; }
    public void              addClearInvoiceOrderListener(Runnable r){ clearInvoiceOrderBtn.addActionListener(e -> r.run()); }
    public void              addSendToKitchenListener(Runnable r)  { sendToKitchenBtn.addActionListener(e -> r.run()); }

    // Navigation / shared
    public void addLogoutListener(Runnable r) { logoutBtn.addActionListener(e -> r.run()); }
    public void showTab(int index)            { SwingUtilities.invokeLater(() -> applyStepStyle(index)); }
    public void showError(String msg)         { JOptionPane.showMessageDialog(this, msg, "Error",     JOptionPane.ERROR_MESSAGE); }
    public void showSuccess(String msg)       { JOptionPane.showMessageDialog(this, msg, "Éxito",     JOptionPane.INFORMATION_MESSAGE); }
    public boolean confirm(String msg)        { return JOptionPane.showConfirmDialog(this, msg, "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION; }
}
