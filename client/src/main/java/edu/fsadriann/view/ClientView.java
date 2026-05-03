package edu.fsadriann.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Function;

public class ClientView extends JFrame {

        // ── TABS ──────────────────────────────────────────────────────────────────
        private JTabbedPane tabbedPane;

        // ── TAB 1: Identificar cliente ────────────────────────────────────────────
        private JTextField        searchPhoneField;
        private JButton           searchClientBtn;
        private DefaultTableModel clientTableModel;
        private JTable            clientTable;
        private DefaultTableModel recentOrdersModel;

        // ── TAB 2: Registrar pedido ───────────────────────────────────────────────
        private JTextField        searchProductField;
        private JButton           searchProductBtn;
        private DefaultTableModel productsTableModel;
        private JTable            productsTable;
        private DefaultTableModel currentOrderModel;
        private JButton           generateInvoiceBtn;
        private JButton           clearOrderBtn;

        // ── TAB 3: Factura ────────────────────────────────────────────────────────
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

        // ── TOPBAR / STATUS ───────────────────────────────────────────────────────
        private JButton logoutBtn;
        private JLabel  statusLabel;

        public ClientView(String operadorEmail) {
                super("Food UPB — Operador/Cliente");
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                setMinimumSize(new Dimension(960, 600));
                setBackground(UIComponents.BG);
        }

        // ── Called by ClientController.init() ─────────────────────────────────────
        //    onRegister: receives the text input and delegates ALL logic to controller.
        public void initComponents(Function<String, Void> onRegister) {
                JPanel root = new JPanel(new BorderLayout());
                root.setBackground(UIComponents.BG2);

                root.add(buildTopbar(),        BorderLayout.NORTH);
                root.add(buildTabbedPane(onRegister), BorderLayout.CENTER);
                root.add(buildStatusBar(),     BorderLayout.SOUTH);

                setContentPane(root);
                pack();
                setLocationRelativeTo(null);
        }

        // ── TOPBAR ────────────────────────────────────────────────────────────────

        private JPanel buildTopbar() {
                JPanel topbar = new JPanel(new BorderLayout());
                topbar.setBackground(UIComponents.BG);
                topbar.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, UIComponents.BORDER),
                        new EmptyBorder(14, 24, 14, 24)
                ));

                JLabel title = new JLabel("Food UPB - Operador");
                title.setFont(UIComponents.fontBold(17));
                title.setForeground(UIComponents.TXT);

                JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
                right.setOpaque(false);
                JLabel roleLabel = new JLabel("Operador");
                roleLabel.setFont(UIComponents.fontPlain(12));
                roleLabel.setForeground(UIComponents.TXT3);

                logoutBtn = UIComponents.roundBtnSmall("Cerrar Sesión", UIComponents.BG, UIComponents.TXT, UIComponents.BORDER);
                right.add(roleLabel);
                right.add(logoutBtn);

                topbar.add(title, BorderLayout.WEST);
                topbar.add(right, BorderLayout.EAST);
                return topbar;
        }

        // ── STATUS BAR ────────────────────────────────────────────────────────────

        private JPanel buildStatusBar() {
                JPanel bar = new JPanel(new BorderLayout());
                bar.setBackground(UIComponents.BG);
                bar.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(1, 0, 0, 0, UIComponents.BORDER),
                        new EmptyBorder(6, 24, 6, 24)
                ));
                statusLabel = new JLabel(" ");
                statusLabel.setFont(UIComponents.fontPlain(12));
                statusLabel.setForeground(UIComponents.TXT2);
                bar.add(statusLabel, BorderLayout.WEST);
                return bar;
        }

        // ── TABBED PANE ───────────────────────────────────────────────────────────

        private JTabbedPane buildTabbedPane(Function<String, Void> onRegister) {
                tabbedPane = new JTabbedPane(JTabbedPane.TOP) {
                        @Override public void updateUI() {
                                setUI(new StyledTabbedPaneUI());
                        }
                };

                tabbedPane.setFont(UIComponents.fontPlain(13));
                tabbedPane.setBackground(UIComponents.BG2);
                tabbedPane.setForeground(UIComponents.TXT2);
                tabbedPane.setOpaque(true);

                // Remove default focus border
                tabbedPane.setFocusable(false);

                tabbedPane.addTab("Identificar cliente", buildTab1());
                tabbedPane.addTab("Registrar pedido",    buildTab2(onRegister));
                tabbedPane.addTab("Factura",             buildTab3());

                return tabbedPane;
        }

        private static class StyledTabbedPaneUI extends BasicTabbedPaneUI {

                private static final int TAB_H      = 42;
                private static final int ARC        = 10;
                private static final int INDICATOR  = 2;   // bottom active indicator thickness

                @Override
                protected void installDefaults() {
                        super.installDefaults();
                        tabInsets          = new Insets(0, 20, 0, 20);
                        selectedTabPadInsets = new Insets(0, 0, 0, 0);
                        contentBorderInsets  = new Insets(0, 0, 0, 0);
                        tabAreaInsets        = new Insets(0, 0, 0, 0);
                        highlight            = UIComponents.BG;
                        lightHighlight       = UIComponents.BG;
                        shadow               = UIComponents.BORDER;
                        darkShadow           = UIComponents.BORDER;
                        focus                = UIComponents.BG;
                }

                // ── Tab area background ───────────────────────────────────────────────

                @Override
                protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        // Full tab strip background
                        g2.setColor(UIComponents.BG);
                        g2.fillRect(0, 0, tabPane.getWidth(), TAB_H);

                        // Bottom border of the tab strip
                        g2.setColor(UIComponents.BORDER);
                        g2.fillRect(0, TAB_H - 1, tabPane.getWidth(), 1);

                        g2.dispose();
                        super.paintTabArea(g, tabPlacement, selectedIndex);
                }

                // ── Individual tab background ─────────────────────────────────────────

                @Override
                protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                                                  int x, int y, int w, int h, boolean isSelected) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        if (isSelected) {
                                // Selected: white pill with bottom indicator
                                g2.setColor(UIComponents.BG);
                                g2.fillRoundRect(x + 4, y + 6, w - 8, h - 6, ARC, ARC);

                                // Active indicator bar at bottom
                                g2.setColor(UIComponents.TXT);
                                g2.fillRoundRect(x + 12, y + h - INDICATOR - 1, w - 24, INDICATOR + 1, 2, 2);
                        } else {
                                // Inactive: transparent
                                g2.setColor(UIComponents.BG);
                                g2.fillRect(x, y, w, h);
                        }

                        g2.dispose();
                }

                // ── Tab border — suppress default ────────────────────────────────────

                @Override
                protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                                              int x, int y, int w, int h, boolean isSelected) {
                        // No border drawn — indicator line handles selection cue
                }

                // ── Content border — suppress default ────────────────────────────────

                @Override
                protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                        // No content border — clean look
                }

                // ── Focus indicator — suppress ────────────────────────────────────────

                @Override
                protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects,
                                                   int tabIndex, Rectangle iconRect, Rectangle textRect,
                                                   boolean isSelected) {
                        // No focus ring
                }

                // ── Tab text ──────────────────────────────────────────────────────────

                @Override
                protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics,
                                         int tabIndex, String title, Rectangle textRect, boolean isSelected) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                        g2.setFont(isSelected
                                ? UIComponents.fontBold(13)
                                : UIComponents.fontPlain(13));
                        g2.setColor(isSelected ? UIComponents.TXT : UIComponents.TXT2);

                        FontMetrics fm = g2.getFontMetrics();
                        int x = textRect.x + (textRect.width  - fm.stringWidth(title)) / 2;
                        int y = textRect.y + (textRect.height  + fm.getAscent() - fm.getDescent()) / 2;
                        g2.drawString(title, x, y);
                        g2.dispose();
                }

                // ── Tab height ────────────────────────────────────────────────────────

                @Override
                protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
                        return TAB_H;
                }

                @Override
                protected int calculateMaxTabHeight(int tabPlacement) {
                        return TAB_H;
                }

                // ── Mouse hover repaint ───────────────────────────────────────────────

                @Override
                protected void installListeners() {
                        super.installListeners();
                        tabPane.addMouseMotionListener(new MouseAdapter() {
                                @Override public void mouseMoved(MouseEvent e) { tabPane.repaint(); }
                        });
                        tabPane.addMouseListener(new MouseAdapter() {
                                @Override public void mouseExited(MouseEvent e) { tabPane.repaint(); }
                        });
                }

                // ── Hover tint — handled by repaint on mouse move above ──────────────
        }

        // ─────────────────────────────────────────────────────────────────────────
        // TAB 1 — Identificar cliente
        // ─────────────────────────────────────────────────────────────────────────

        private JPanel buildTab1() {
                JPanel outer = new JPanel();
                outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
                outer.setBackground(UIComponents.BG2);
                outer.setBorder(new EmptyBorder(20, 24, 20, 24));

                // Search card
                JPanel searchCard = UIComponents.card();
                searchCard.setLayout(new BoxLayout(searchCard, BoxLayout.Y_AXIS));
                searchCard.setAlignmentX(Component.LEFT_ALIGNMENT);
                searchCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

                JLabel searchTitle = new JLabel("Buscar cliente");
                searchTitle.setFont(UIComponents.fontBold(14));
                searchTitle.setForeground(UIComponents.TXT);
                searchTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
                searchCard.add(searchTitle);
                searchCard.add(Box.createVerticalStrut(12));

                JPanel searchRow = new JPanel(new BorderLayout(10, 0));
                searchRow.setOpaque(false);
                searchRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                searchPhoneField = UIComponents.styledField("Número telefónico");
                searchClientBtn  = UIComponents.roundBtn("Buscar", UIComponents.TXT, UIComponents.BG, null);
                searchRow.add(searchPhoneField, BorderLayout.CENTER);
                searchRow.add(searchClientBtn,  BorderLayout.EAST);
                searchCard.add(searchRow);

                // Clients table card
                JPanel clientCard = UIComponents.card();
                clientCard.setLayout(new BoxLayout(clientCard, BoxLayout.Y_AXIS));
                clientCard.setAlignmentX(Component.LEFT_ALIGNMENT);

                clientTableModel = new DefaultTableModel(new Object[]{"Nombres", "Correo", "Teléfono"}, 0) {
                        @Override public boolean isCellEditable(int r, int c) { return false; }
                };
                clientTable = styledTable(clientTableModel);
                JScrollPane clientScroll = tableScroll(clientTable, 90);
                clientScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
                clientCard.add(clientScroll);

                // Recent orders card
                JPanel recentCard = UIComponents.card();
                recentCard.setLayout(new BoxLayout(recentCard, BoxLayout.Y_AXIS));
                recentCard.setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel recentTitle = new JLabel("Pedidos Recientes");
                recentTitle.setFont(UIComponents.fontBold(13));
                recentTitle.setForeground(UIComponents.TXT);
                recentTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

                recentOrdersModel = new DefaultTableModel(new Object[]{"Producto", "Precio", "Cantidad"}, 0) {
                        @Override public boolean isCellEditable(int r, int c) { return false; }
                };
                JScrollPane recentScroll = tableScroll(styledTable(recentOrdersModel), 130);
                recentScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

                recentCard.add(recentTitle);
                recentCard.add(Box.createVerticalStrut(10));
                recentCard.add(recentScroll);

                outer.add(searchCard);
                outer.add(Box.createVerticalStrut(14));
                outer.add(clientCard);
                outer.add(Box.createVerticalStrut(14));
                outer.add(recentCard);

                JScrollPane scrollWrapper = new JScrollPane(outer);
                scrollWrapper.setBorder(null);
                scrollWrapper.getViewport().setBackground(UIComponents.BG2);

                JPanel wrapper = new JPanel(new BorderLayout());
                wrapper.setBackground(UIComponents.BG2);
                wrapper.add(scrollWrapper, BorderLayout.CENTER);
                return wrapper;
        }

        // ─────────────────────────────────────────────────────────────────────────
        // TAB 2 — Registrar pedido  (onRegister callback wired here)
        // ─────────────────────────────────────────────────────────────────────────

        private JPanel buildTab2(Function<String, Void> onRegister) {
                JPanel outer = new JPanel(new BorderLayout(14, 0));
                outer.setBackground(UIComponents.BG2);
                outer.setBorder(new EmptyBorder(20, 24, 20, 24));

                // Left: search + products
                JPanel left = new JPanel();
                left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
                left.setOpaque(false);

                JPanel searchCard = UIComponents.card();
                searchCard.setLayout(new BoxLayout(searchCard, BoxLayout.Y_AXIS));
                searchCard.setAlignmentX(Component.LEFT_ALIGNMENT);
                searchCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

                JLabel searchTitle = new JLabel("Buscar producto");
                searchTitle.setFont(UIComponents.fontBold(14));
                searchTitle.setForeground(UIComponents.TXT);
                searchTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
                searchCard.add(searchTitle);
                searchCard.add(Box.createVerticalStrut(12));

                JPanel searchRow = new JPanel(new BorderLayout(10, 0));
                searchRow.setOpaque(false);
                searchRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                searchProductField = UIComponents.styledField("Nombre producto");

                // View fires callback → controller handles all logic
                searchProductBtn = UIComponents.roundBtn("Buscar", UIComponents.TXT, UIComponents.BG, null);
                searchProductBtn.addActionListener(e -> onRegister.apply(searchProductField.getText().trim()));

                searchRow.add(searchProductField, BorderLayout.CENTER);
                searchRow.add(searchProductBtn,   BorderLayout.EAST);
                searchCard.add(searchRow);

                JPanel productsCard = UIComponents.card();
                productsCard.setLayout(new BoxLayout(productsCard, BoxLayout.Y_AXIS));
                productsCard.setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel productsTitle = new JLabel("Productos disponibles");
                productsTitle.setFont(UIComponents.fontBold(13));
                productsTitle.setForeground(UIComponents.TXT);
                productsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

                productsTableModel = new DefaultTableModel(new Object[]{"Producto", "Precio", ""}, 0) {
                        @Override public boolean isCellEditable(int r, int c) { return false; }
                };
                productsTable = styledTable(productsTableModel);
                JScrollPane productsScroll = tableScroll(productsTable, 200);
                productsScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

                productsCard.add(productsTitle);
                productsCard.add(Box.createVerticalStrut(10));
                productsCard.add(productsScroll);

                left.add(searchCard);
                left.add(Box.createVerticalStrut(14));
                left.add(productsCard);

                outer.add(left, BorderLayout.CENTER);
                outer.add(buildCurrentOrderPanel(false), BorderLayout.EAST);
                return outer;
        }

        // ─────────────────────────────────────────────────────────────────────────
        // TAB 3 — Factura
        // ─────────────────────────────────────────────────────────────────────────

        private JPanel buildTab3() {
                JPanel outer = new JPanel(new BorderLayout(14, 0));
                outer.setBackground(UIComponents.BG2);
                outer.setBorder(new EmptyBorder(20, 24, 20, 24));

                JPanel invoiceCard = UIComponents.card();
                invoiceCard.setLayout(new BoxLayout(invoiceCard, BoxLayout.Y_AXIS));
                invoiceCard.setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel invoiceTitle = new JLabel("Factura");
                invoiceTitle.setFont(UIComponents.fontBold(14));
                invoiceTitle.setForeground(UIComponents.TXT);
                invoiceTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
                invoiceCard.add(invoiceTitle);
                invoiceCard.add(Box.createVerticalStrut(16));

                invoiceClientVal    = new JLabel("—");
                invoiceTipoVal      = new JLabel("—");
                invoiceDireccionVal = new JLabel("—");
                invoiceCuadranteVal = new JLabel("—");
                invoiceSubtotalVal  = new JLabel("$0");
                invoiceIvaVal       = new JLabel("$0");
                invoiceDomicilioVal = new JLabel("$0");
                invoiceTotalVal     = new JLabel("$0");

                invoiceCard.add(invoiceRow("Cliente",   invoiceClientVal));
                invoiceCard.add(Box.createVerticalStrut(6));
                invoiceCard.add(invoiceRow("Tipo",      invoiceTipoVal));
                invoiceCard.add(Box.createVerticalStrut(6));
                invoiceCard.add(invoiceRow("Dirección", invoiceDireccionVal));
                invoiceCard.add(Box.createVerticalStrut(6));
                invoiceCard.add(invoiceRow("Cuadrante", invoiceCuadranteVal));
                invoiceCard.add(Box.createVerticalStrut(14));
                invoiceCard.add(new JSeparator());
                invoiceCard.add(Box.createVerticalStrut(14));
                invoiceCard.add(invoiceRow("Subtotal",  invoiceSubtotalVal));
                invoiceCard.add(Box.createVerticalStrut(6));
                invoiceCard.add(invoiceRow("IVA",       invoiceIvaVal));
                invoiceCard.add(Box.createVerticalStrut(6));
                invoiceCard.add(invoiceRow("Domicilio", invoiceDomicilioVal));
                invoiceCard.add(Box.createVerticalStrut(10));
                invoiceCard.add(invoiceTotalRow());

                outer.add(invoiceCard, BorderLayout.CENTER);
                outer.add(buildCurrentOrderPanel(true), BorderLayout.EAST);
                return outer;
        }

        // ── Invoice row helpers ───────────────────────────────────────────────────

        private JPanel invoiceRow(String label, JLabel valueLabel) {
                JPanel row = new JPanel(new BorderLayout());
                row.setOpaque(false);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
                row.setAlignmentX(Component.LEFT_ALIGNMENT);
                JLabel lbl = new JLabel(label);
                lbl.setFont(UIComponents.fontPlain(13));
                lbl.setForeground(UIComponents.TXT2);
                valueLabel.setFont(UIComponents.fontPlain(13));
                valueLabel.setForeground(UIComponents.TXT);
                row.add(lbl, BorderLayout.WEST);
                row.add(valueLabel, BorderLayout.EAST);
                return row;
        }

        private JPanel invoiceTotalRow() {
                JPanel row = new JPanel(new BorderLayout());
                row.setOpaque(false);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
                row.setAlignmentX(Component.LEFT_ALIGNMENT);
                JLabel lbl = new JLabel("Total");
                lbl.setFont(UIComponents.fontBold(14));
                lbl.setForeground(UIComponents.TXT);
                invoiceTotalVal.setFont(UIComponents.fontBold(14));
                invoiceTotalVal.setForeground(UIComponents.TXT);
                row.add(lbl, BorderLayout.WEST);
                row.add(invoiceTotalVal, BorderLayout.EAST);
                return row;
        }

        // ── Shared "Pedido actual" right panel ────────────────────────────────────

        private JPanel buildCurrentOrderPanel(boolean isInvoiceTab) {
                JPanel panel = UIComponents.card();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                panel.setPreferredSize(new Dimension(260, 0));

                JLabel orderTitle = new JLabel("Pedido actual");
                orderTitle.setFont(UIComponents.fontBold(13));
                orderTitle.setForeground(UIComponents.TXT);
                orderTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
                panel.add(orderTitle);
                panel.add(Box.createVerticalStrut(12));

                if (isInvoiceTab) {
                        invoiceOrderModel = new DefaultTableModel(new Object[]{"Producto", "Cant."}, 0) {
                                @Override public boolean isCellEditable(int r, int c) { return false; }
                        };
                        invoiceOrderTable = styledTable(invoiceOrderModel);
                        JScrollPane scroll = tableScroll(invoiceOrderTable, 160);
                        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
                        panel.add(scroll);
                        panel.add(Box.createVerticalStrut(12));

                        clearInvoiceOrderBtn = UIComponents.roundBtn("Limpiar pedido", UIComponents.BG, UIComponents.TXT, UIComponents.BORDER);
                        clearInvoiceOrderBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
                        clearInvoiceOrderBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
                        panel.add(clearInvoiceOrderBtn);
                        panel.add(Box.createVerticalStrut(8));

                        sendToKitchenBtn = UIComponents.roundBtn("Enviar a cocina", UIComponents.TXT, UIComponents.BG, null);
                        sendToKitchenBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
                        sendToKitchenBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
                        panel.add(sendToKitchenBtn);
                } else {
                        currentOrderModel = new DefaultTableModel(new Object[]{"Producto", "Cant."}, 0) {
                                @Override public boolean isCellEditable(int r, int c) { return false; }
                        };
                        JScrollPane scroll = tableScroll(styledTable(currentOrderModel), 160);
                        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
                        panel.add(scroll);
                        panel.add(Box.createVerticalStrut(12));

                        generateInvoiceBtn = UIComponents.roundBtn("Generar factura", UIComponents.TXT, UIComponents.BG, null);
                        generateInvoiceBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
                        generateInvoiceBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
                        panel.add(generateInvoiceBtn);
                        panel.add(Box.createVerticalStrut(8));

                        clearOrderBtn = UIComponents.roundBtn("Limpiar pedido", UIComponents.BG, UIComponents.TXT, UIComponents.BORDER);
                        clearOrderBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
                        clearOrderBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
                        panel.add(clearOrderBtn);
                }

                return panel;
        }

        // ── TABLE HELPERS ─────────────────────────────────────────────────────────

        private JTable styledTable(DefaultTableModel model) {
                JTable table = new JTable(model);
                table.setFont(UIComponents.fontPlain(13));
                table.setForeground(UIComponents.TXT);
                table.setBackground(UIComponents.BG);
                table.setRowHeight(30);
                table.setShowGrid(false);
                table.setIntercellSpacing(new Dimension(0, 0));
                table.getTableHeader().setFont(UIComponents.fontBold(12));
                table.getTableHeader().setForeground(UIComponents.TXT2);
                table.getTableHeader().setBackground(UIComponents.BG2);
                table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIComponents.BORDER));
                table.setSelectionBackground(UIComponents.BG2);
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
        // MVC CONTRACT — methods called by ClientController
        // ─────────────────────────────────────────────────────────────────────────

        /** Shows a status/connection message (called by controller after init). */
        public void setMessage(String message) {
                SwingUtilities.invokeLater(() -> statusLabel.setText(message));
        }

        /** Enables or disables register-related controls based on server connection. */
        public void setRegisterEnabled(boolean enabled) {
                SwingUtilities.invokeLater(() -> {
                        if (searchProductBtn     != null) searchProductBtn.setEnabled(enabled);
                        if (generateInvoiceBtn   != null) generateInvoiceBtn.setEnabled(enabled);
                        if (clearOrderBtn        != null) clearOrderBtn.setEnabled(enabled);
                        if (sendToKitchenBtn     != null) sendToKitchenBtn.setEnabled(enabled);
                        if (clearInvoiceOrderBtn != null) clearInvoiceOrderBtn.setEnabled(enabled);
                });
        }

        // ── Additional getters / listener wiring for controller ──────────────────

        // Tab 1
        public String            getSearchPhone()                      { return searchPhoneField.getText().trim(); }
        public void              addSearchClientListener(Runnable r)   { searchClientBtn.addActionListener(e -> r.run()); }
        public DefaultTableModel getClientTableModel()                 { return clientTableModel; }
        public DefaultTableModel getRecentOrdersModel()                { return recentOrdersModel; }
        public int               getSelectedClientRow()                { return clientTable.getSelectedRow(); }

        // Tab 2
        public String            getSearchProduct()                    { return searchProductField.getText().trim(); }
        public DefaultTableModel getProductsTableModel()               { return productsTableModel; }
        public DefaultTableModel getCurrentOrderModel()                { return currentOrderModel; }
        public int               getSelectedProductRow()               { return productsTable.getSelectedRow(); }
        public void              addGenerateInvoiceListener(Runnable r){ generateInvoiceBtn.addActionListener(e -> r.run()); }
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
        public void              addLogoutListener(Runnable r)         { logoutBtn.addActionListener(e -> r.run()); }
        public void              showTab(int index)                    { tabbedPane.setSelectedIndex(index); }
        public void              showError(String msg)                 { JOptionPane.showMessageDialog(this, msg, "Error",      JOptionPane.ERROR_MESSAGE); }
        public void              showSuccess(String msg)               { JOptionPane.showMessageDialog(this, msg, "Éxito",      JOptionPane.INFORMATION_MESSAGE); }
        public boolean           confirm(String msg)                   { return JOptionPane.showConfirmDialog(this, msg, "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION; }
}