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

    private JPanel buildUsersSection() {
        JPanel card = card();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel t = new JLabel("Gestión de usuarios");
        t.setFont(new Font("SansSerif", Font.BOLD, 16));
        t.setForeground(TEXT);

        addUserBtn = new JButton("+ Nuevo usuario");
        addUserBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        addUserBtn.setBackground(BLUE);
        addUserBtn.setForeground(Color.WHITE);
        addUserBtn.setOpaque(true);
        addUserBtn.setBorderPainted(false);
        addUserBtn.setFocusPainted(false);
        addUserBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        header.add(t,          BorderLayout.WEST);
        header.add(addUserBtn, BorderLayout.EAST);

        String[] columns = {"Nombre", "Apellido", "Teléfono", "Correo", "Rol", "Dirección"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JScrollPane scroll = new JScrollPane(styledTable(tableModel));
        scroll.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_C));

        card.add(header, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildProductsSection() {
        JPanel card = card();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel t = new JLabel("Catálogo de productos");
        t.setFont(new Font("SansSerif", Font.BOLD, 16));
        t.setForeground(TEXT);

        String[] cols = {"Nombre", "Precio", "Preparación", "Tipo", "Estado"};
        DefaultTableModel pm = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JScrollPane ps = new JScrollPane(styledTable(pm));
        ps.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_C));

        card.add(t,  BorderLayout.NORTH);
        card.add(ps, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildCuadsSection() {
        JPanel card = card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel t = new JLabel("Cuadrantes de entrega");
        t.setFont(new Font("SansSerif", Font.BOLD, 16));
        t.setForeground(TEXT);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(t);
        card.add(Box.createVerticalStrut(14));

        JPanel grid = new JPanel(new GridLayout(2, 3, 10, 10));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[][] cuads = {
            {"A", "Centro · Cabecera",   "$2.500"},
            {"B", "Laureles · Cabecera", "$3.500"},
            {"C", "Floridablanca",       "$5.000"},
            {"D", "Girón",              "$6.500"},
            {"E", "Piedecuesta",        "$8.000"},
        };
        for (String[] c : cuads) grid.add(buildCuadCell(c[0], c[1], c[2]));

        JPanel ph = card();
        ph.setLayout(new BorderLayout());
        JLabel phLbl = new JLabel("+ Agregar cuadrante", SwingConstants.CENTER);
        phLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        phLbl.setForeground(TEXT3);
        ph.add(phLbl, BorderLayout.CENTER);
        grid.add(ph);

        card.add(grid);
        return card;
    }

    private JPanel buildCuadCell(String name, String sub, String tarifa) {
        JPanel c = card();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER_C), new EmptyBorder(10, 12, 10, 12)));
        JLabel nameLbl = new JLabel("Cuadrante " + name);
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        nameLbl.setForeground(TEXT);
        JLabel subLbl = new JLabel(sub);
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

        JLabel t = new JLabel("Bitácora de auditoría");
        t.setFont(new Font("SansSerif", Font.BOLD, 16));
        t.setForeground(TEXT);

        String[] cols = {"Fecha / hora", "Usuario", "Acción", "Detalle"};
        DefaultTableModel am = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JScrollPane as = new JScrollPane(styledTable(am));
        as.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_C));

        card.add(t,  BorderLayout.NORTH);
        card.add(as, BorderLayout.CENTER);
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

    // ── Public API ─────────────────────────────────────────────────────────────

    public void addLogoutListener(Runnable action) { logoutBtn.addActionListener(e -> action.run()); }

    public void addOpenUserFormListener(Runnable action) { addUserBtn.addActionListener(e -> action.run()); }

    public void showUserForm(Consumer<AdminUserFormData> onSave) {
        JDialog dialog = new JDialog(frame, "Nuevo usuario", true);
        dialog.setSize(760, 540);
        dialog.setLocationRelativeTo(frame);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG2);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel form = new JPanel(new GridLayout(0, 2, 12, 10));
        form.setOpaque(false);

        JTextField nombre    = field("Nombre");
        JTextField apellido  = field("Apellido");
        JTextField telefono  = field("Teléfono");
        JTextField correo    = field("Correo");
        JPasswordField clave = new JPasswordField();
        clave.setFont(new Font("SansSerif", Font.PLAIN, 13));
        clave.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_C), new EmptyBorder(6, 10, 6, 10)));
        JComboBox<String> rol = new JComboBox<>(
            new String[]{"CLIENTE", "OPERADOR", "ADMIN", "COCINA", "ENTREGA", "SERVER"});

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
        save.setFont(new Font("SansSerif", Font.BOLD, 13));
        save.setBackground(BLUE);
        save.setForeground(Color.WHITE);
        save.setOpaque(true);
        save.setBorderPainted(false);
        save.setFocusPainted(false);
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
