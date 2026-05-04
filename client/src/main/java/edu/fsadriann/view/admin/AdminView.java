package edu.fsadriann.view.admin;

import edu.fsadriann.view.UIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.model.iterator.Iterator;
import edu.fsadriann.server.model.user.User;

public class AdminView extends JFrame {

    private static final String SEC_REPORTS  = "reports";
    private static final String SEC_USERS    = "users";
    private static final String SEC_PRODUCTS = "products";
    private static final String SEC_CUADS    = "cuadrants";
    private static final String SEC_AUDIT    = "audit";

    private final JFrame frame;
    private JLabel totalUsersValue;
    private JLabel statusLabel;
    private JButton logoutBtn;
    private JButton addUserBtn;
    private DefaultTableModel tableModel;
    private final CardLayout sectionLayout = new CardLayout();
    private JPanel sectionCards;
    private final Map<String, JButton> sectionTabs = new LinkedHashMap<>();

    public AdminView(String userLabel) {
        super("Food UPB — Administrador");
        frame = this;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1220, 720));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIComponents.BG2);

        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.add(buildHeader(userLabel), BorderLayout.NORTH);
        main.add(buildContent(),         BorderLayout.CENTER);
        main.add(buildStatusBar(),       BorderLayout.SOUTH);

        root.add(main, BorderLayout.CENTER);
        setContentPane(root);
        pack();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HEADER  (topbar + tab bar)
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildHeader(String userLabel) {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(UIComponents.BG);
        header.add(buildTopbar(userLabel));
        header.add(buildTabBar());
        return header;
    }

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
        JLabel section = new JLabel("Administrador");
        section.setFont(UIComponents.fontBold(14));
        section.setForeground(UIComponents.ACCENT);
        section.setBorder(new EmptyBorder(0, 12, 0, 0));
        left.add(brand);
        left.add(sep);
        left.add(section);

        logoutBtn = UIComponents.roundBtnSmall("Cerrar sesión", UIComponents.BG, UIComponents.TXT, UIComponents.BORDER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(UIComponents.badge("Administrador", UIComponents.INFO_BG, UIComponents.INFO_FG));
        right.add(logoutBtn);

        top.add(left,  BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);
        return top;
    }

    private JPanel buildTabBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bar.setBackground(UIComponents.BG);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIComponents.BORDER),
            new EmptyBorder(0, 12, 0, 12)
        ));

        addTab(bar, SEC_REPORTS,  "Reportes",    true);
        addTab(bar, SEC_USERS,    "Usuarios",    false);
        addTab(bar, SEC_PRODUCTS, "Productos",   false);
        addTab(bar, SEC_CUADS,    "Cuadrantes",  false);
        addTab(bar, SEC_AUDIT,    "Auditoría",   false);
        return bar;
    }

    private void addTab(JPanel bar, String key, String label, boolean active) {
        JButton btn = tabButton(label, active);
        btn.addActionListener(e -> showSection(key));
        sectionTabs.put(key, btn);
        bar.add(btn);
    }

    private JButton tabButton(String text, boolean active) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                // No custom bg — transparent
                super.paintComponent(g);
            }
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFont(active ? UIComponents.fontBold(13) : UIComponents.fontPlain(13));
        btn.setForeground(active ? UIComponents.ACCENT : UIComponents.TXT2);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, active ? 2 : 0, 0, UIComponents.ACCENT),
            new EmptyBorder(10, 14, 10, 14)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!btn.getForeground().equals(UIComponents.ACCENT)) {
                    btn.setForeground(UIComponents.TXT);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!btn.getForeground().equals(UIComponents.ACCENT)) {
                    btn.setForeground(UIComponents.TXT2);
                }
            }
        });
        return btn;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CONTENT
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setBorder(new EmptyBorder(16, 18, 16, 18));
        content.setBackground(UIComponents.BG2);
        content.add(buildSummary(),      BorderLayout.NORTH);
        content.add(buildSectionCards(), BorderLayout.CENTER);
        return content;
    }

    private JPanel buildSummary() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 0));
        panel.setOpaque(false);

        // Users counter card
        JPanel usersCard = UIComponents.card();
        usersCard.setLayout(new BoxLayout(usersCard, BoxLayout.Y_AXIS));
        JLabel uLabel = new JLabel("Usuarios");
        uLabel.setFont(UIComponents.fontPlain(12));
        uLabel.setForeground(UIComponents.TXT2);
        totalUsersValue = new JLabel("0");
        totalUsersValue.setFont(UIComponents.fontBold(28));
        totalUsersValue.setForeground(UIComponents.ACCENT);
        usersCard.add(uLabel);
        usersCard.add(Box.createVerticalStrut(4));
        usersCard.add(totalUsersValue);
        usersCard.add(Box.createVerticalGlue());

        panel.add(usersCard);
        panel.add(UIComponents.metricCard("Pedidos",   "—", "Estado de operación"));
        panel.add(UIComponents.metricCard("Cocina",    "—", "Cola y preparación"));
        panel.add(UIComponents.metricCard("Entregas",  "—", "Rutas activas"));
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

    // ── Section: Reports ──────────────────────────────────────────────────────

    private JPanel buildReportsSection() {
        JPanel card = UIComponents.card();
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel metrics = new JPanel(new GridLayout(1, 4, 10, 0));
        metrics.setOpaque(false);
        metrics.add(UIComponents.metricCard("Pedidos hoy",     "—", ""));
        metrics.add(UIComponents.metricCard("Ingresos hoy",    "—", ""));
        metrics.add(UIComponents.metricCard("Clientes nuevos", "—", ""));
        metrics.add(UIComponents.metricCard("Tiempo prom.",    "—", ""));

        JPanel filtersCard = UIComponents.card();
        filtersCard.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtersCard.add(new JLabel("Filtros:") {{ setFont(UIComponents.fontBold(12)); setForeground(UIComponents.TXT2); }});
        JComboBox<String> ops = new JComboBox<>(new String[]{"Todos los operadores", "Operador 1", "Operador 2"});
        ops.setFont(UIComponents.fontPlain(12));
        JComboBox<String> states = new JComboBox<>(new String[]{"Todos los estados", "Pendiente", "Entregado"});
        states.setFont(UIComponents.fontPlain(12));
        JButton filterBtn = UIComponents.roundBtnSmall("Filtrar", UIComponents.ACCENT, Color.WHITE, null);
        filtersCard.add(ops);
        filtersCard.add(states);
        filtersCard.add(filterBtn);

        String[] cols = {"Operador", "Pedidos", "Clientes atendidos", "Tiempo prom.", "Estado"};
        DefaultTableModel rm = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable rt = styledTable(rm);
        JScrollPane rs = new JScrollPane(rt);
        rs.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIComponents.BORDER));

        card.add(metrics,     BorderLayout.NORTH);
        card.add(filtersCard, BorderLayout.CENTER);
        card.add(rs,          BorderLayout.SOUTH);
        return card;
    }

    // ── Section: Users ────────────────────────────────────────────────────────

    private JPanel buildUsersSection() {
        JPanel card = UIComponents.card();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel labels = new JPanel();
        labels.setOpaque(false);
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
        JLabel t = new JLabel("Gestión de usuarios");
        t.setFont(UIComponents.fontBold(16));
        t.setForeground(UIComponents.TXT);
        JLabel s = new JLabel("Alta, consulta y control administrativo");
        s.setFont(UIComponents.fontPlain(11));
        s.setForeground(UIComponents.TXT3);
        labels.add(t);
        labels.add(Box.createVerticalStrut(2));
        labels.add(s);

        addUserBtn = UIComponents.roundBtn("+ Nuevo usuario", UIComponents.ACCENT, Color.WHITE, null);

        header.add(labels,    BorderLayout.WEST);
        header.add(addUserBtn, BorderLayout.EAST);

        String[] columns = {"Nombre", "Apellido", "Teléfono", "Correo", "Rol", "Dirección"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(tableModel);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIComponents.BORDER));

        card.add(header, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // ── Section: Products ─────────────────────────────────────────────────────

    private JPanel buildProductsSection() {
        JPanel card = UIComponents.card();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel t = new JLabel("Catálogo de productos");
        t.setFont(UIComponents.fontBold(16));
        t.setForeground(UIComponents.TXT);
        JButton addProd = UIComponents.roundBtn("+ Nuevo producto", UIComponents.ACCENT, Color.WHITE, null);
        header.add(t,       BorderLayout.WEST);
        header.add(addProd, BorderLayout.EAST);

        String[] cols = {"Nombre", "Precio", "Preparación", "Tipo", "Estado"};
        DefaultTableModel pm = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable pt = styledTable(pm);
        JScrollPane ps = new JScrollPane(pt);
        ps.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIComponents.BORDER));

        card.add(header, BorderLayout.NORTH);
        card.add(ps,     BorderLayout.CENTER);
        return card;
    }

    // ── Section: Cuadrantes ───────────────────────────────────────────────────

    private JPanel buildCuadsSection() {
        JPanel card = UIComponents.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel t = new JLabel("Cuadrantes de entrega");
        t.setFont(UIComponents.fontBold(16));
        t.setForeground(UIComponents.TXT);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(t);
        card.add(Box.createVerticalStrut(14));

        JPanel grid = new JPanel(new GridLayout(2, 3, 10, 10));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[][] cuads = {
            {"A", "Centro · Cabecera",    "$2.500"},
            {"B", "Laureles · Cabecera",  "$3.500"},
            {"C", "Floridablanca",        "$5.000"},
            {"D", "Girón",               "$6.500"},
            {"E", "Piedecuesta",         "$8.000"},
        };
        for (String[] c : cuads) {
            grid.add(buildCuadCard(c[0], c[1], c[2]));
        }
        JPanel addCell = UIComponents.card();
        addCell.setLayout(new BorderLayout());
        addCell.setBorder(BorderFactory.createDashedBorder(UIComponents.BORDER, 4, 2));
        JLabel addLbl = new JLabel("+ Agregar cuadrante", SwingConstants.CENTER);
        addLbl.setFont(UIComponents.fontPlain(12));
        addLbl.setForeground(UIComponents.TXT3);
        addCell.add(addLbl, BorderLayout.CENTER);
        grid.add(addCell);

        card.add(grid);
        return card;
    }

    private JPanel buildCuadCard(String name, String sub, String tarifa) {
        JPanel c = UIComponents.card();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        JLabel nameLbl = new JLabel("Cuadrante " + name);
        nameLbl.setFont(UIComponents.fontBold(13));
        nameLbl.setForeground(UIComponents.TXT);
        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(UIComponents.fontPlain(11));
        subLbl.setForeground(UIComponents.TXT3);
        JLabel tarifaLbl = new JLabel("Tarifa: " + tarifa);
        tarifaLbl.setFont(UIComponents.fontBold(12));
        tarifaLbl.setForeground(UIComponents.TXT);
        JButton editBtn = UIComponents.roundBtnSmall("Editar", UIComponents.BG, UIComponents.TXT, UIComponents.BORDER);
        editBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.add(nameLbl);
        c.add(Box.createVerticalStrut(2));
        c.add(subLbl);
        c.add(Box.createVerticalStrut(6));
        c.add(tarifaLbl);
        c.add(Box.createVerticalStrut(8));
        c.add(editBtn);
        return c;
    }

    // ── Section: Audit ────────────────────────────────────────────────────────

    private JPanel buildAuditSection() {
        JPanel card = UIComponents.card();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel t = new JLabel("Bitácora de auditoría");
        t.setFont(UIComponents.fontBold(16));
        t.setForeground(UIComponents.TXT);

        String[] cols = {"Fecha / hora", "Usuario", "Acción", "Detalle"};
        DefaultTableModel am = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable at = styledTable(am);
        JScrollPane as = new JScrollPane(at);
        as.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIComponents.BORDER));

        card.add(t,  BorderLayout.NORTH);
        card.add(as, BorderLayout.CENTER);
        return card;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SECTION SWITCHING
    // ─────────────────────────────────────────────────────────────────────────

    private void showSection(String key) {
        if (sectionCards != null) sectionLayout.show(sectionCards, key);
        sectionTabs.forEach((k, btn) -> {
            boolean active = k.equals(key);
            btn.setFont(active ? UIComponents.fontBold(13) : UIComponents.fontPlain(13));
            btn.setForeground(active ? UIComponents.ACCENT : UIComponents.TXT2);
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, active ? 2 : 0, 0, UIComponents.ACCENT),
                new EmptyBorder(10, 14, 10, 14)
            ));
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STATUS BAR
    // ─────────────────────────────────────────────────────────────────────────

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
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private JTable styledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(UIComponents.fontPlain(12));
        table.setForeground(UIComponents.TXT);
        table.setBackground(UIComponents.BG);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(UIComponents.fontBold(12));
        table.getTableHeader().setForeground(UIComponents.TXT2);
        table.getTableHeader().setBackground(UIComponents.BG2);
        table.getTableHeader().setBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIComponents.BORDER));
        table.setSelectionBackground(UIComponents.INFO_BG);
        table.setSelectionForeground(UIComponents.TXT);
        return table;
    }

    private JPanel labeled(String text, JComponent component) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        JLabel label = new JLabel(text);
        label.setFont(UIComponents.fontBold(12));
        label.setForeground(UIComponents.TXT2);
        panel.add(label);
        panel.add(Box.createVerticalStrut(6));
        panel.add(component);
        return panel;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC API (same contract as before)
    // ─────────────────────────────────────────────────────────────────────────

    public void addLogoutListener(Runnable action) { logoutBtn.addActionListener(e -> action.run()); }

    public void addOpenUserFormListener(Runnable action) { addUserBtn.addActionListener(e -> action.run()); }

    public void showUserForm(Consumer<AdminUserFormData> onSave) {
        JDialog dialog = new JDialog(frame, "Nuevo usuario", true);
        dialog.setSize(760, 540);
        dialog.setLocationRelativeTo(frame);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIComponents.BG2);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel form = new JPanel(new GridLayout(0, 2, 12, 10));
        form.setOpaque(false);

        JTextField nombre     = UIComponents.styledField("Nombre");
        JTextField apellido   = UIComponents.styledField("Apellido");
        JTextField telefono   = UIComponents.styledField("Teléfono");
        JTextField correo     = UIComponents.styledField("Correo");
        JPasswordField clave  = new JPasswordField();
        clave.setFont(UIComponents.fontPlain(13));
        clave.setBorder(new EmptyBorder(8, 10, 8, 10));
        JComboBox<String> rol = new JComboBox<>(
            new String[]{"CLIENTE", "OPERADOR", "ADMIN", "COCINA", "ENTREGA", "SERVER"});

        JTextField calle     = UIComponents.styledField("Calle");
        JTextField carrera   = UIComponents.styledField("Carrera/Avenida");
        JTextField numero    = UIComponents.styledField("Número");
        JTextField casa      = UIComponents.styledField("Casa/Apto");
        JTextField barrio    = UIComponents.styledField("Barrio");
        JTextField municipio = UIComponents.styledField("Municipio");

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
        error.setForeground(UIComponents.DANGER);
        error.setFont(UIComponents.fontPlain(12));

        JButton save = UIComponents.roundBtn("Guardar", UIComponents.ACCENT, Color.WHITE, null);
        save.addActionListener(e -> {
            String n = nombre.getText().trim();
            String a = apellido.getText().trim();
            String t = telefono.getText().trim();
            String c = correo.getText().trim();
            String p = new String(clave.getPassword()).trim();
            String r = String.valueOf(rol.getSelectedItem());

            if (n.isBlank() || a.isBlank() || t.isBlank() || c.isBlank() || p.isBlank()) {
                error.setText("Nombre, apellido, teléfono, correo y contraseña son obligatorios.");
                return;
            }
            String direccion = "";
            if ("CLIENTE".equals(r)) {
                direccion = String.format("%s %s %s %s, %s, %s",
                    calle.getText().trim(), carrera.getText().trim(),
                    numero.getText().trim(), casa.getText().trim(),
                    barrio.getText().trim(), municipio.getText().trim()).trim();
            }
            onSave.accept(new AdminUserFormData(n, a, t, c, p, r, direccion));
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
                    String.valueOf(u.getTelefono()), u.getId(),
                    u.getRol() != null ? u.getRol().name() : "—",
                    u.getDireccion() == null || u.getDireccion().isBlank() ? "—" : u.getDireccion()
                });
            }
        });
    }

    public void setTotalUsers(int n) {
        SwingUtilities.invokeLater(() -> { if (totalUsersValue != null) totalUsersValue.setText(String.valueOf(n)); });
    }

    public void setMessage(String message) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(message));
    }

    public void showView() { setVisible(true); toFront(); }
    public void hideView() { setVisible(false); }
    public JFrame getFrame() { return frame; }
}
