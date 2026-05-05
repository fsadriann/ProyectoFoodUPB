package edu.fsadriann.view.admin;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.model.iterator.Iterator;
import edu.fsadriann.server.model.user.User;
import edu.fsadriann.server.model.cuadrante.Cuadrante;

public class AdminView extends JFrame {

    private static final Color BG       = Color.WHITE;
    private static final Color BG2      = new Color(245, 245, 245);
    private static final Color BLUE     = new Color(24, 95, 165);
    private static final Color BLUE_BG  = new Color(230, 241, 251);
    private static final Color TEXT     = new Color(30, 30, 30);
    private static final Color TEXT2    = new Color(100, 100, 100);
    private static final Color TEXT3    = new Color(150, 150, 150);
    private static final Color BORDER_C = new Color(220, 220, 220);
    private static final Color DANGER   = new Color(220, 53, 69);

    private static final String SEC_REPORTS  = "reports";
    private static final String SEC_USERS    = "users";
    private static final String SEC_PRODUCTS = "products";
    private static final String SEC_CUADS    = "cuadrants";
    private static final String SEC_AUDIT    = "audit";

    private DefaultTableModel auditTableModel;
    private JPanel cuadsGrid;
    private JButton addCuadBtn;
    private JButton connectCuadBtn;
    private final JFrame frame;
    private JLabel totalUsersValue;
    private JLabel statusLabel;
    private JButton logoutBtn;
    private JButton addUserBtn;
    private JButton editUserBtn;    // ← NUEVO
    private JButton deleteUserBtn;  // ← NUEVO
    private JTable usersTable;      // ← NUEVO
    private DefaultTableModel tableModel;
    private final CardLayout sectionLayout = new CardLayout();
    private JPanel sectionCards;
    private final Map<String, JButton> sectionTabs = new LinkedHashMap<>();
    private DefaultTableModel productsTableModel;
    private JTable productsTable;
    private JButton addProductBtn;
    private JButton editProductBtn;
    private JButton toggleProductBtn;


    public AdminView(String userLabel) {
        super("Food UPB — Administrador");
        frame = this;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1220, 720));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG2);

        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.add(buildHeader(userLabel), BorderLayout.NORTH);
        main.add(buildContent(),         BorderLayout.CENTER);
        main.add(buildStatusBar(),       BorderLayout.SOUTH);

        root.add(main, BorderLayout.CENTER);
        setContentPane(root);
        pack();
    }

    private JPanel buildHeader(String userLabel) {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(BG);
        header.add(buildTopbar());
        header.add(buildTabBar());
        return header;
    }

    private JPanel buildTopbar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(BG);
        top.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER_C),
                new EmptyBorder(10, 20, 10, 20)
        ));
        JLabel title = new JLabel("Food UPB — Administrador");
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

    private JPanel buildTabBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bar.setBackground(BG);
        bar.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER_C),
                new EmptyBorder(0, 12, 0, 12)
        ));
        addTab(bar, SEC_REPORTS,  "Reportes",   true);
        addTab(bar, SEC_USERS,    "Usuarios",   false);
        addTab(bar, SEC_PRODUCTS, "Productos",  false);
        addTab(bar, SEC_CUADS,    "Cuadrantes", false);
        addTab(bar, SEC_AUDIT,    "Auditoría",  false);
        return bar;
    }

    private void addTab(JPanel bar, String key, String label, boolean active) {
        JButton btn = tabButton(label, active);
        btn.addActionListener(e -> showSection(key));
        sectionTabs.put(key, btn);
        bar.add(btn);
    }

    private JButton tabButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", active ? Font.BOLD : Font.PLAIN, 13));
        btn.setForeground(active ? BLUE : TEXT2);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, active ? 2 : 0, 0, BLUE),
                new EmptyBorder(10, 14, 10, 14)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setBorder(new EmptyBorder(16, 18, 16, 18));
        content.setBackground(BG2);
        content.add(buildSummary(),      BorderLayout.NORTH);
        content.add(buildSectionCards(), BorderLayout.CENTER);
        return content;
    }

    private JPanel buildSummary() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 0));
        panel.setOpaque(false);
        JPanel usersCard = card();
        usersCard.setLayout(new BoxLayout(usersCard, BoxLayout.Y_AXIS));
        usersCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_C), new EmptyBorder(10, 12, 10, 12)));
        JLabel uLabel = new JLabel("Usuarios");
        uLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        uLabel.setForeground(TEXT2);
        totalUsersValue = new JLabel("0");
        totalUsersValue.setFont(new Font("SansSerif", Font.BOLD, 28));
        totalUsersValue.setForeground(BLUE);
        usersCard.add(uLabel);
        usersCard.add(Box.createVerticalStrut(4));
        usersCard.add(totalUsersValue);
        usersCard.add(Box.createVerticalGlue());
        panel.add(usersCard);
        panel.add(metricTile("Pedidos",  "—"));
        panel.add(metricTile("Cocina",   "—"));
        panel.add(metricTile("Entregas", "—"));
        return panel;
    }

    private JPanel buildSectionCards() {
        sectionCards = new JPanel(sectionLayout);
        sectionCards.setOpaque(false);
        sectionCards.add(buildReportsSection(),  SEC_REPORTS);
        sectionCards.add(buildUsersSection(),    SEC_USERS);
        sectionCards.add(buildProductsSection(), SEC_PRODUCTS);
        sectionCards.add(buildCuadsSection(),    SEC_CUADS);
        sectionCards.add(buildAuditSection(),    SEC_AUDIT);
        showSection(SEC_REPORTS);
        return sectionCards;
    }

    private JPanel buildReportsSection() {
        JPanel card = card();
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        JPanel metrics = new JPanel(new GridLayout(1, 4, 10, 0));
        metrics.setOpaque(false);
        metrics.add(metricTile("Pedidos hoy",     "—"));
        metrics.add(metricTile("Ingresos hoy",    "—"));
        metrics.add(metricTile("Clientes nuevos", "—"));
        metrics.add(metricTile("Tiempo prom.",    "—"));
        String[] cols = {"Operador", "Pedidos", "Clientes atendidos", "Tiempo prom.", "Estado"};
        DefaultTableModel rm = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JScrollPane rs = new JScrollPane(styledTable(rm));
        rs.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_C));
        card.add(metrics, BorderLayout.NORTH);
        card.add(rs,      BorderLayout.CENTER);
        return card;
    }

    // ── USUARIOS — con botones Editar y Eliminar ──────────────────────────────
    private JPanel buildUsersSection() {
        JPanel card = card();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        // Header con título y botón nuevo
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel t = new JLabel("Gestión de usuarios");
        t.setFont(new Font("SansSerif", Font.BOLD, 16));
        t.setForeground(TEXT);
        addUserBtn = new JButton("+ Nuevo usuario");
        styleBtn(addUserBtn, BLUE, Color.WHITE);
        header.add(t,          BorderLayout.WEST);
        header.add(addUserBtn, BorderLayout.EAST);

        // Tabla
        String[] columns = {"Nombre", "Apellido", "Teléfono", "Correo", "Rol", "Dirección"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        usersTable = styledTable(tableModel);
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(usersTable);
        scroll.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_C));

        // Botones editar / eliminar
        editUserBtn   = new JButton("Editar");
        deleteUserBtn = new JButton("Eliminar");
        styleBtn(editUserBtn,   new Color(40, 167, 69), Color.WHITE);
        styleBtn(deleteUserBtn, DANGER, Color.WHITE);
        editUserBtn.setEnabled(false);
        deleteUserBtn.setEnabled(false);

        // Habilitar botones solo cuando hay fila seleccionada
        usersTable.getSelectionModel().addListSelectionListener(e -> {
            boolean selected = usersTable.getSelectedRow() >= 0;
            editUserBtn.setEnabled(selected);
            deleteUserBtn.setEnabled(selected);
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(editUserBtn);
        actions.add(deleteUserBtn);

        card.add(header,  BorderLayout.NORTH);
        card.add(scroll,  BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    // ── reemplaza buildProductsSection()
    private JPanel buildProductsSection() {
        JPanel card = card();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel t = new JLabel("Catálogo de productos");
        t.setFont(new Font("SansSerif", Font.BOLD, 16));
        t.setForeground(TEXT);
        addProductBtn = new JButton("+ Nuevo producto");
        styleBtn(addProductBtn, BLUE, Color.WHITE);
        header.add(t,             BorderLayout.WEST);
        header.add(addProductBtn, BorderLayout.EAST);

        // Tabla
        String[] cols = {"ID", "Nombre", "Categoría", "Precio", "Complejo", "Disponible"};
        productsTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        productsTable = styledTable(productsTableModel);
        productsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane ps = new JScrollPane(productsTable);
        ps.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_C));

        // Botones editar / disponibilidad
        editProductBtn   = new JButton("Editar");
        toggleProductBtn = new JButton("Activar / Desactivar");
        styleBtn(editProductBtn,   new Color(40, 167, 69), Color.WHITE);
        styleBtn(toggleProductBtn, new Color(255, 153, 0), Color.WHITE);
        editProductBtn.setEnabled(false);
        toggleProductBtn.setEnabled(false);

        productsTable.getSelectionModel().addListSelectionListener(e -> {
            boolean selected = productsTable.getSelectedRow() >= 0;
            editProductBtn.setEnabled(selected);
            toggleProductBtn.setEnabled(selected);
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(editProductBtn);
        actions.add(toggleProductBtn);

        card.add(header,  BorderLayout.NORTH);
        card.add(ps,      BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    public void setProducts(LinkedList<edu.fsadriann.server.model.product.Product> products) {
        SwingUtilities.invokeLater(() -> {
            productsTableModel.setRowCount(0);
            if (products == null) return;
            edu.fsadriann.model.iterator.Iterator<edu.fsadriann.server.model.product.Product> it = products.iterator();
            while (it.hasNext()) {
                edu.fsadriann.server.model.product.Product p = it.next();
                if (p == null) continue;
                productsTableModel.addRow(new Object[]{
                        p.getProductoId(),
                        p.getNombre(),
                        p.getCategoria(),
                        "$" + p.getPrecio(),
                        p.isComplejo() ? "Sí" : "No",
                        p.isDisponible() ? "Sí" : "No"
                });
            }
        });
    }

    public void addOpenProductFormListener(Runnable action) {
        addProductBtn.addActionListener(e -> action.run());
    }

    public void addEditProductListener(Runnable action) {
        editProductBtn.addActionListener(e -> action.run());
    }

    public void addToggleProductListener(Runnable action) {
        toggleProductBtn.addActionListener(e -> action.run());
    }

    public String getSelectedProductId() {
        int row = productsTable.getSelectedRow();
        if (row < 0) return null;
        return (String) productsTableModel.getValueAt(row, 0); // columna ID
    }

    public boolean getSelectedProductDisponible() {
        int row = productsTable.getSelectedRow();
        if (row < 0) return false;
        return "Sí".equals(productsTableModel.getValueAt(row, 5)); // columna Disponible
    }

    public void showProductForm(String[] prefill,
                                java.util.function.Consumer<String[]> onSave) {
        JDialog dialog = new JDialog(frame,
                prefill == null ? "Nuevo producto" : "Editar producto", true);
        dialog.setSize(480, 400);
        dialog.setLocationRelativeTo(frame);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG2);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel form = new JPanel(new GridLayout(0, 2, 12, 10));
        form.setOpaque(false);

        JTextField nombre      = field(prefill != null ? prefill[0] : "Nombre");
        JTextField descripcion = field(prefill != null ? prefill[1] : "Descripción");
        JTextField precio      = field(prefill != null ? prefill[2] : "Precio");
        JComboBox<String> categoria = new JComboBox<>(
                new String[]{"PLATO_PRINCIPAL", "BEBIDA", "ENTRADA", "POSTRE"});
        if (prefill != null && prefill[3] != null) categoria.setSelectedItem(prefill[3]);
        JComboBox<String> complejo = new JComboBox<>(new String[]{"No", "Sí"});
        if (prefill != null && prefill[4] != null) complejo.setSelectedItem(prefill[4]);

        form.add(labeled("Nombre",      nombre));
        form.add(labeled("Descripción", descripcion));
        form.add(labeled("Precio",      precio));
        form.add(labeled("Categoría",   categoria));
        form.add(labeled("¿Complejo?",  complejo));

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(10, 0, 0, 0));
        JLabel error = new JLabel(" ");
        error.setForeground(DANGER);
        error.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JButton save = new JButton("Guardar");
        styleBtn(save, BLUE, Color.WHITE);
        save.addActionListener(e -> {
            String n = nombre.getText().trim();
            String d = descripcion.getText().trim();
            String pr = precio.getText().trim();
            if (n.isBlank() || pr.isBlank()) {
                error.setText("Nombre y precio son obligatorios.");
                return;
            }
            try { Integer.parseInt(pr); }
            catch (NumberFormatException ex) {
                error.setText("El precio debe ser un número entero.");
                return;
            }
            onSave.accept(new String[]{
                    n, d, pr,
                    String.valueOf(categoria.getSelectedItem()),
                    String.valueOf(complejo.getSelectedItem())
            });
            dialog.dispose();
        });

        bottom.add(error, BorderLayout.CENTER);
        bottom.add(save,  BorderLayout.EAST);
        root.add(form,   BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    // ── reemplaza buildCuadsSection()
    private JPanel buildCuadsSection() {
        JPanel card = card();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel t = new JLabel("Cuadrantes de entrega");
        t.setFont(new Font("SansSerif", Font.BOLD, 16));
        t.setForeground(TEXT);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        addCuadBtn     = new JButton("+ Nuevo cuadrante");
        connectCuadBtn = new JButton("Conectar cuadrantes");
        styleBtn(addCuadBtn,     BLUE,                    Color.WHITE);
        styleBtn(connectCuadBtn, new Color(100, 100, 100), Color.WHITE);
        btnPanel.add(connectCuadBtn);
        btnPanel.add(addCuadBtn);

        header.add(t,        BorderLayout.WEST);
        header.add(btnPanel, BorderLayout.EAST);

        // Grid de cuadrantes
        cuadsGrid = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 10));
        cuadsGrid.setOpaque(false);
        JScrollPane scroll = new JScrollPane(cuadsGrid);
        scroll.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_C));
        scroll.getViewport().setOpaque(false);

        card.add(header, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildCuadCell(String name, String sub, String tarifa) {
        JPanel c = card();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_C), new EmptyBorder(10, 12, 10, 12)));
        JLabel nameLbl   = new JLabel("Cuadrante " + name);
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        nameLbl.setForeground(TEXT);
        JLabel subLbl    = new JLabel(sub);
        subLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subLbl.setForeground(TEXT3);
        JLabel tarifaLbl = new JLabel("Tarifa: " + tarifa);
        tarifaLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        tarifaLbl.setForeground(TEXT);
        c.add(nameLbl);
        c.add(Box.createVerticalStrut(2));
        c.add(subLbl);
        c.add(Box.createVerticalStrut(6));
        c.add(tarifaLbl);
        return c;
    }

    private JPanel buildAuditSection() {
        JPanel card = card();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel t = new JLabel("Bitácora de auditoría");
        t.setFont(new Font("SansSerif", Font.BOLD, 16));
        t.setForeground(TEXT);

        JButton refreshBtn = new JButton("↻ Actualizar");
        styleBtn(refreshBtn, new Color(100, 100, 100), Color.WHITE);
        header.add(t,          BorderLayout.WEST);
        header.add(refreshBtn, BorderLayout.EAST);

        String[] cols = {"Fecha / hora", "Evento"};
        auditTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(auditTableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(0).setMaxWidth(180);
        JScrollPane as = new JScrollPane(table);
        as.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_C));

        refreshBtn.setName("refreshAudit");

        card.add(header, BorderLayout.NORTH);
        card.add(as,     BorderLayout.CENTER);
        return card;
    }

    private void showSection(String key) {
        if (sectionCards != null) sectionLayout.show(sectionCards, key);
        sectionTabs.forEach((k, btn) -> {
            boolean active = k.equals(key);
            btn.setFont(new Font("SansSerif", active ? Font.BOLD : Font.PLAIN, 13));
            btn.setForeground(active ? BLUE : TEXT2);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(0, 0, active ? 2 : 0, 0, BLUE),
                    new EmptyBorder(10, 14, 10, 14)
            ));
        });
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG);
        bar.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, BORDER_C),
                new EmptyBorder(6, 18, 6, 18)
        ));
        statusLabel = new JLabel("Listo");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT2);
        bar.add(statusLabel, BorderLayout.WEST);
        return bar;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(BG);
        return p;
    }

    private JPanel metricTile(String label, String value) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_C), new EmptyBorder(10, 12, 10, 12)));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lbl.setForeground(TEXT2);
        JLabel val = new JLabel(value);
        val.setFont(new Font("SansSerif", Font.BOLD, 22));
        val.setForeground(TEXT);
        p.add(lbl);
        p.add(Box.createVerticalStrut(4));
        p.add(val);
        return p;
    }

    private JTable styledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setForeground(TEXT);
        table.setBackground(BG);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setForeground(TEXT2);
        table.getTableHeader().setBackground(BG2);
        table.getTableHeader().setBorder(new MatteBorder(0, 0, 1, 0, BORDER_C));
        table.setSelectionBackground(BLUE_BG);
        table.setSelectionForeground(TEXT);
        return table;
    }

    private void styleBtn(JButton btn, Color bg, Color fg) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private JTextField field(String placeholder) {
        JTextField f = new JTextField(placeholder);
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setForeground(TEXT2);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_C), new EmptyBorder(6, 10, 6, 10)));
        return f;
    }

    private JPanel labeled(String text, JComponent component) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(TEXT2);
        panel.add(label);
        panel.add(Box.createVerticalStrut(6));
        panel.add(component);
        return panel;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void addOpenCuadFormListener(Runnable action) {
        addCuadBtn.addActionListener(e -> action.run());
    }

    public void addConnectCuadListener(Runnable action) {
        connectCuadBtn.addActionListener(e -> action.run());
    }

    public void addRefreshAuditListener(Runnable action) {
        findRefreshAuditBtn(sectionCards, action);
    }

    private void findRefreshAuditBtn(java.awt.Container container, Runnable action) {
        for (java.awt.Component c : container.getComponents()) {
            if (c instanceof JButton && "refreshAudit".equals(c.getName())) {
                ((JButton) c).addActionListener(e -> action.run());
                return;
            }
            if (c instanceof java.awt.Container) {
                findRefreshAuditBtn((java.awt.Container) c, action);
            }
        }
    }

    public void setBitacora(LinkedList<String> eventos) {
        SwingUtilities.invokeLater(() -> {
            auditTableModel.setRowCount(0);
            if (eventos == null) return;
            edu.fsadriann.model.iterator.Iterator<String> it = eventos.iterator();
            while (it.hasNext()) {
                String e = it.next();
                if (e == null) continue;
                String[] parts = e.split(" \\| ", 2);
                if (parts.length == 2) {
                    auditTableModel.addRow(new Object[]{ parts[0], parts[1] });
                } else {
                    auditTableModel.addRow(new Object[]{ "—", e });
                }
            }
        });
    }

    public void setCuadrantes(LinkedList<Cuadrante> cuadrantes) {
        SwingUtilities.invokeLater(() -> {
            cuadsGrid.removeAll();
            if (cuadrantes == null) { cuadsGrid.revalidate(); cuadsGrid.repaint(); return; }
            edu.fsadriann.model.iterator.Iterator<Cuadrante> it = cuadrantes.iterator();
            while (it.hasNext()) {
                Cuadrante c = it.next();
                if (c != null) cuadsGrid.add(buildCuadCell(c));
            }
            cuadsGrid.revalidate();
            cuadsGrid.repaint();
        });
    }

    private JPanel buildCuadCell(Cuadrante c) {
        boolean esOrigen = "UPB".equals(c.getNombre());
        JPanel p = card();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setPreferredSize(new Dimension(180, 90));
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(esOrigen ? BLUE : (c.isDisponible() ? BORDER_C : DANGER)),
                new EmptyBorder(10, 12, 10, 12)));

        JLabel nombre = new JLabel(c.getNombre());
        nombre.setFont(new Font("SansSerif", Font.BOLD, 13));
        nombre.setForeground(esOrigen ? BLUE : TEXT);

        JLabel desc = new JLabel(c.getDescripcion() != null ? c.getDescripcion() : "—");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 11));
        desc.setForeground(TEXT3);

        JLabel estado = new JLabel(esOrigen ? "Origen fijo" :
                (c.isDisponible() ? "Disponible" : "No disponible"));
        estado.setFont(new Font("SansSerif", Font.PLAIN, 11));
        estado.setForeground(esOrigen ? BLUE :
                (c.isDisponible() ? new Color(40, 167, 69) : DANGER));

        p.add(nombre);
        p.add(Box.createVerticalStrut(2));
        p.add(desc);
        p.add(Box.createVerticalStrut(4));
        p.add(estado);
        return p;
    }

    public void showCuadForm(String[] prefill, java.util.function.Consumer<String[]> onSave) {
        JDialog dialog = new JDialog(frame,
                prefill == null ? "Nuevo cuadrante" : "Editar cuadrante", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(frame);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG2);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel form = new JPanel(new GridLayout(0, 2, 12, 10));
        form.setOpaque(false);

        JTextField nombre      = field(prefill != null ? prefill[0] : "Nombre");
        JTextField descripcion = field(prefill != null ? prefill[1] : "Descripción");
        JTextField latitud     = field(prefill != null ? prefill[2] : "Latitud (opcional)");
        JTextField longitud    = field(prefill != null ? prefill[3] : "Longitud (opcional)");

        form.add(labeled("Nombre",      nombre));
        form.add(labeled("Descripción", descripcion));
        form.add(labeled("Latitud",     latitud));
        form.add(labeled("Longitud",    longitud));

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(10, 0, 0, 0));
        JLabel error = new JLabel(" ");
        error.setForeground(DANGER);
        error.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JButton save = new JButton("Guardar");
        styleBtn(save, BLUE, Color.WHITE);
        save.addActionListener(e -> {
            String n = nombre.getText().trim();
            String d = descripcion.getText().trim();
            if (n.isBlank()) { error.setText("El nombre es obligatorio."); return; }
            onSave.accept(new String[]{ n, d,
                    latitud.getText().trim(), longitud.getText().trim() });
            dialog.dispose();
        });

        bottom.add(error, BorderLayout.CENTER);
        bottom.add(save,  BorderLayout.EAST);
        root.add(form,   BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    public void showConnectCuadForm(java.util.function.Consumer<String[]> onConnect) {
        JDialog dialog = new JDialog(frame, "Conectar cuadrantes", true);
        dialog.setSize(400, 220);
        dialog.setLocationRelativeTo(frame);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG2);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel form = new JPanel(new GridLayout(0, 2, 12, 10));
        form.setOpaque(false);

        JTextField cuadA     = field("Nombre cuadrante A");
        JTextField cuadB     = field("Nombre cuadrante B");
        JTextField distancia = field("Distancia (km)");

        form.add(labeled("Cuadrante A",    cuadA));
        form.add(labeled("Cuadrante B",    cuadB));
        form.add(labeled("Distancia (km)", distancia));

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(10, 0, 0, 0));
        JLabel error = new JLabel(" ");
        error.setForeground(DANGER);
        error.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JButton save = new JButton("Conectar");
        styleBtn(save, BLUE, Color.WHITE);
        save.addActionListener(e -> {
            String a = cuadA.getText().trim();
            String b = cuadB.getText().trim();
            String d = distancia.getText().trim();
            if (a.isBlank() || b.isBlank() || d.isBlank()) {
                error.setText("Todos los campos son obligatorios.");
                return;
            }
            try { Double.parseDouble(d); }
            catch (NumberFormatException ex) {
                error.setText("La distancia debe ser un número.");
                return;
            }
            onConnect.accept(new String[]{ a, b, d });
            dialog.dispose();
        });

        bottom.add(error, BorderLayout.CENTER);
        bottom.add(save,  BorderLayout.EAST);
        root.add(form,   BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    public void addLogoutListener(Runnable action) {
        logoutBtn.addActionListener(e -> action.run());
    }

    public void addOpenUserFormListener(Runnable action) {
        addUserBtn.addActionListener(e -> action.run());
    }

    public void addEditUserListener(Runnable action) {
        editUserBtn.addActionListener(e -> action.run());
    }

    public void addDeleteUserListener(Runnable action) {
        deleteUserBtn.addActionListener(e -> action.run());
    }

    public int getSelectedUserRow() {
        return usersTable.getSelectedRow();
    }

    public String getSelectedUserTelefono() {
        int row = usersTable.getSelectedRow();
        if (row < 0) return null;
        return (String) tableModel.getValueAt(row, 2); // columna Teléfono
    }

    public void showUserForm(Consumer<AdminUserFormData> onSave) {
        showUserFormInternal("Nuevo usuario", null, onSave);
    }

    public void showEditUserForm(String nombre, String apellido, String telefono,
                                 String correo, String rol, String direccion,
                                 Consumer<AdminUserFormData> onSave) {
        showUserFormInternal("Editar usuario",
                new String[]{nombre, apellido, telefono, correo, rol, direccion}, onSave);
    }

    private void showUserFormInternal(String title, String[] prefill,
                                      Consumer<AdminUserFormData> onSave) {
        JDialog dialog = new JDialog(frame, title, true);
        dialog.setSize(760, 540);
        dialog.setLocationRelativeTo(frame);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG2);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel form = new JPanel(new GridLayout(0, 2, 12, 10));
        form.setOpaque(false);

        JTextField nombre    = field(prefill != null ? prefill[0] : "Nombre");
        JTextField apellido  = field(prefill != null ? prefill[1] : "Apellido");
        JTextField telefono  = field(prefill != null ? prefill[2] : "Teléfono");
        JTextField correo    = field(prefill != null ? prefill[3] : "Correo");
        JPasswordField clave = new JPasswordField();
        clave.setFont(new Font("SansSerif", Font.PLAIN, 13));
        clave.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_C), new EmptyBorder(6, 10, 6, 10)));
        JComboBox<String> rol = new JComboBox<>(
                new String[]{"CLIENTE", "OPERADOR", "ADMIN", "COCINA", "ENTREGA", "SERVER"});
        if (prefill != null && prefill[4] != null) rol.setSelectedItem(prefill[4]);

        JTextField calle     = field("Calle");
        JTextField carrera   = field("Carrera/Avenida");
        JTextField numero    = field("Número");
        JTextField casa      = field("Casa/Apto");
        JTextField barrio    = field("Barrio");
        JTextField municipio = field("Municipio");

        JPanel addrPanel = new JPanel(new GridLayout(0, 2, 10, 8));
        addrPanel.setOpaque(false);
        addrPanel.setBorder(BorderFactory.createTitledBorder("Dirección (solo CLIENTE)"));
        addrPanel.add(calle);  addrPanel.add(carrera);
        addrPanel.add(numero); addrPanel.add(casa);
        addrPanel.add(barrio); addrPanel.add(municipio);

        rol.addActionListener(e -> {
            addrPanel.setVisible("CLIENTE".equals(String.valueOf(rol.getSelectedItem())));
            dialog.revalidate(); dialog.repaint();
        });

        form.add(labeled("Nombre",     nombre));
        form.add(labeled("Apellido",   apellido));
        form.add(labeled("Teléfono",   telefono));
        form.add(labeled("Correo",     correo));
        form.add(labeled("Contraseña", clave));
        form.add(labeled("Rol",        rol));

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(10, 0, 0, 0));
        JLabel error = new JLabel(" ");
        error.setForeground(DANGER);
        error.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JButton save = new JButton("Guardar");
        styleBtn(save, BLUE, Color.WHITE);
        save.addActionListener(e -> {
            String n = nombre.getText().trim();
            String a = apellido.getText().trim();
            String te = telefono.getText().trim();
            String c = correo.getText().trim();
            String p = new String(clave.getPassword()).trim();
            String r = String.valueOf(rol.getSelectedItem());
            if (n.isBlank() || a.isBlank() || te.isBlank() || c.isBlank()) {
                error.setText("Nombre, apellido, teléfono y correo son obligatorios.");
                return;
            }
            String direccion = "";
            if ("CLIENTE".equals(r)) {
                direccion = String.format("%s %s %s %s, %s, %s",
                        calle.getText().trim(), carrera.getText().trim(),
                        numero.getText().trim(), casa.getText().trim(),
                        barrio.getText().trim(), municipio.getText().trim()).trim();
            }
            onSave.accept(new AdminUserFormData(n, a, te, c, p, r, direccion));
            dialog.dispose();
        });

        bottom.add(error, BorderLayout.CENTER);
        bottom.add(save,  BorderLayout.EAST);

        root.add(form,      BorderLayout.NORTH);
        root.add(addrPanel, BorderLayout.CENTER);
        root.add(bottom,    BorderLayout.SOUTH);
        dialog.setContentPane(root);
        addrPanel.setVisible("CLIENTE".equals(String.valueOf(rol.getSelectedItem())));
        dialog.setVisible(true);
    }

    public void addUserRow(AdminUserFormData data) {
        tableModel.addRow(new Object[]{
                data.getNombre(), data.getApellido(), data.getTelefono(),
                data.getCorreo(), data.getRol(),
                data.getDireccionCompleta().isBlank() ? "—" : data.getDireccionCompleta()
        });
    }

    public void setUsers(LinkedList<User> users) {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            if (users == null) return;
            Iterator<User> it = users.iterator();
            while (it.hasNext()) {
                User u = it.next();
                if (u == null) continue;
                tableModel.addRow(new Object[]{
                        u.getNombres(), u.getApellidos(),
                        u.getTelefono(), u.getId(),
                        u.getRol() != null ? u.getRol().name() : "—",
                        u.getDireccion() == null || u.getDireccion().isBlank() ? "—" : u.getDireccion()
                });
            }
        });
    }

    public void setTotalUsers(int n) {
        SwingUtilities.invokeLater(() -> {
            if (totalUsersValue != null) totalUsersValue.setText(String.valueOf(n));
        });
    }

    public void setMessage(String message) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(message));
    }

    public void showView() { setVisible(true); toFront(); }
    public void hideView() { setVisible(false); }
    public JFrame getFrame() { return frame; }

    private static class WrapLayout extends FlowLayout {
        public WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }
        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }
        @Override
        public Dimension minimumLayoutSize(Container target) {
            return layoutSize(target, false);
        }
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
                    if (x + d.width > width && x != start) {
                        y += rowH + getVgap(); rowH = 0; x = start;
                    }
                    x += d.width + getHgap();
                    rowH = Math.max(rowH, d.height);
                }
                y += rowH + getVgap() + insets.top + insets.bottom;
                return new Dimension(width, y);
            }
        }
    }
}