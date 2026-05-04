package edu.fsadriann.server.view.admin;

import edu.fsadriann.server.view.UIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AdminView {

    private JFrame frame;
    private DefaultTableModel tableModel;
    private JTable userTable;

    private JLabel statTotal;
    private JLabel statOperadores;
    private JLabel statPremium;
    private JLabel statAdmins;

    private static final Color BG        = Color.WHITE;
    private static final Color BG2       = new Color(247, 247, 245);
    private static final Color BORDER    = new Color(225, 225, 222);
    private static final Color TXT       = new Color(20, 20, 20);
    private static final Color TXT2      = new Color(110, 110, 108);
    private static final Color TXT3      = new Color(170, 170, 168);
    private static final Color DANGER    = new Color(180, 40, 40);
    private static final Color DANGER_BG = new Color(255, 240, 240);

    public AdminView(String adminEmail) {
        buildUI(adminEmail);
    }

    private void buildUI(String adminEmail) {
        frame = new JFrame("Food UPB — Panel de Administrador");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1100, 680);
        frame.setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG2);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildMain(adminEmail), BorderLayout.CENTER);
        frame.setContentPane(root);
    }

    private JPanel buildSidebar() {
        JButton logout = UIComponents.roundBtn("Cerrar sesión", BG, TXT, BORDER);
        logout.addActionListener(e -> frame.dispose());
        String[] items = {"Usuarios", "Cocina", "Entregas", "Rutas", "Auditoría"};
        return UIComponents.sidebar("Food UPB", "Panel de administrador", items, 0, logout);
    }

    private JPanel buildMain(String adminEmail) {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG2);
        main.add(UIComponents.topbar("Gestión de usuarios", adminEmail), BorderLayout.NORTH);
        main.add(buildContent(), BorderLayout.CENTER);
        return main;
    }

    private JPanel buildContent() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG2);
        content.setBorder(new EmptyBorder(28, 36, 28, 36));
        content.add(buildStats());
        content.add(Box.createVerticalStrut(24));
        content.add(buildTableCard());
        return content;
    }

    // ── STATS ────────────────────────────────────────────────────────────────

    private JPanel buildStats() {
        JPanel stats = new JPanel(new GridLayout(1, 4, 12, 0));
        stats.setOpaque(false);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 86));
        stats.setAlignmentX(Component.LEFT_ALIGNMENT);

        statTotal      = new JLabel("0");
        statOperadores = new JLabel("0");
        statPremium    = new JLabel("0");
        statAdmins     = new JLabel("0");

        stats.add(statCard("Total usuarios", statTotal,      TXT));
        stats.add(statCard("Operadores",     statOperadores, new Color(30, 100, 200)));
        stats.add(statCard("Premium",        statPremium,    new Color(160, 100, 0)));
        stats.add(statCard("Admins",         statAdmins,     DANGER));

        return stats;
    }

    private JPanel statCard(String label, JLabel valLabel, Color valueColor) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 18, 14, 18));

        valLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        valLabel.setForeground(valueColor);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lbl.setForeground(TXT3);

        card.add(valLabel);
        card.add(Box.createVerticalStrut(3));
        card.add(lbl);
        return card;
    }

    private void refreshStats() {
        int total = tableModel.getRowCount();
        int operadores = 0, premium = 0, admins = 0;

        for (int i = 0; i < total; i++) {
            String rol  = tableModel.getValueAt(i, 5).toString();
            String tipo = tableModel.getValueAt(i, 4).toString();
            if ("Operador".equalsIgnoreCase(rol)) operadores++;
            if ("Admin".equalsIgnoreCase(rol))    admins++;
            if ("Premium".equals(tipo))           premium++;
        }

        statTotal.setText(String.valueOf(total));
        statOperadores.setText(String.valueOf(operadores));
        statPremium.setText(String.valueOf(premium));
        statAdmins.setText(String.valueOf(admins));
    }

    // ── TABLE CARD ───────────────────────────────────────────────────────────

    private JPanel buildTableCard() {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout());
        card.setOpaque(false);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(14, 20, 14, 20)
        ));

        JLabel title = new JLabel("Usuarios registrados");
        title.setFont(new Font("SansSerif", Font.BOLD, 13));
        title.setForeground(TXT2);

        JButton addBtn = addButton("+ Nuevo usuario");
        addBtn.addActionListener(e -> openUserForm());

        header.add(title, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);
        card.add(buildTable(), BorderLayout.CENTER);
        return card;
    }

    // ── TABLE ────────────────────────────────────────────────────────────────

    private JScrollPane buildTable() {
        String[] cols = {"ID", "Nombres", "Correo", "Teléfono", "Tipo", "Rol", "Acciones"};

        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        userTable = new JTable(tableModel);
        userTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        userTable.setRowHeight(50);
        userTable.setBackground(BG);
        userTable.setForeground(TXT);
        userTable.setGridColor(BORDER);
        userTable.setShowHorizontalLines(true);
        userTable.setShowVerticalLines(false);
        userTable.setSelectionBackground(BG2);
        userTable.setSelectionForeground(TXT);
        userTable.setFocusable(false);
        userTable.setIntercellSpacing(new Dimension(0, 1));
        userTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader header = userTable.getTableHeader();
        header.setBackground(BG2);
        header.setForeground(TXT2);
        header.setFont(new Font("SansSerif", Font.BOLD, 11));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);
        header.setPreferredSize(new Dimension(0, 38));

        int[] widths = {38, 148, 188, 110, 88, 76, 162};
        for (int i = 0; i < widths.length; i++)
            userTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // ID
        userTable.getColumnModel().getColumn(0).setCellRenderer((t, v, s, f, r, c) -> {
            JLabel l = new JLabel(v == null ? "" : v.toString(), SwingConstants.CENTER);
            l.setFont(new Font("SansSerif", Font.PLAIN, 12));
            l.setForeground(TXT3);
            l.setOpaque(true);
            l.setBackground(s ? BG2 : BG);
            return l;
        });

        // Nombres
        userTable.getColumnModel().getColumn(1).setCellRenderer((t, v, s, f, r, c) -> {
            JLabel l = new JLabel(v == null ? "" : v.toString());
            l.setFont(new Font("SansSerif", Font.BOLD, 13));
            l.setForeground(TXT);
            l.setBorder(new EmptyBorder(0, 8, 0, 0));
            l.setOpaque(true);
            l.setBackground(s ? BG2 : BG);
            return l;
        });

        // Correo
        userTable.getColumnModel().getColumn(2).setCellRenderer((t, v, s, f, r, c) -> {
            JLabel l = new JLabel(v == null ? "" : v.toString());
            l.setFont(new Font("SansSerif", Font.PLAIN, 12));
            l.setForeground(TXT2);
            l.setBorder(new EmptyBorder(0, 8, 0, 0));
            l.setOpaque(true);
            l.setBackground(s ? BG2 : BG);
            return l;
        });

        // Teléfono
        userTable.getColumnModel().getColumn(3).setCellRenderer((t, v, s, f, r, c) -> {
            JLabel l = new JLabel(v == null ? "" : v.toString());
            l.setFont(new Font("SansSerif", Font.PLAIN, 13));
            l.setForeground(TXT);
            l.setBorder(new EmptyBorder(0, 8, 0, 0));
            l.setOpaque(true);
            l.setBackground(s ? BG2 : BG);
            return l;
        });

        // Tipo badge
        userTable.getColumnModel().getColumn(4).setCellRenderer((t, v, s, f, row, c) -> {
            String val = v == null ? "" : v.toString();
            boolean premium  = val.equals("Premium");
            boolean estandar = val.equals("Estándar");

            JPanel p = new JPanel(new GridBagLayout());
            p.setBackground(s ? BG2 : BG);

            if (premium || estandar) {
                JLabel badge = new JLabel(val) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(getBackground());
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                badge.setOpaque(false);
                badge.setFont(new Font("SansSerif", Font.BOLD, 11));
                badge.setForeground(premium ? new Color(120, 70, 0) : TXT2);
                badge.setBackground(premium ? new Color(255, 238, 180) : new Color(235, 235, 232));
                badge.setBorder(new EmptyBorder(4, 10, 4, 10));
                p.add(badge);
            } else {
                JLabel dash = new JLabel("—");
                dash.setFont(new Font("SansSerif", Font.PLAIN, 13));
                dash.setForeground(TXT3);
                p.add(dash);
            }
            return p;
        });

        // Rol
        userTable.getColumnModel().getColumn(5).setCellRenderer((t, v, s, f, r, c) -> {
            JLabel l = new JLabel(v == null ? "" : v.toString());
            l.setFont(new Font("SansSerif", Font.PLAIN, 13));
            l.setForeground(TXT2);
            l.setBorder(new EmptyBorder(0, 8, 0, 0));
            l.setOpaque(true);
            l.setBackground(s ? BG2 : BG);
            return l;
        });

        // Acciones
        userTable.getColumnModel().getColumn(6).setCellRenderer((t, v, s, f, row, c) -> {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 9));
            p.setBackground(s ? BG2 : BG);
            boolean ok = tableModel.getValueAt(row, 1) != null &&
                    !tableModel.getValueAt(row, 1).toString().isEmpty();
            if (ok) {
                p.add(smallBtn("Editar",   BG2,      TXT,    BORDER));
                p.add(smallBtn("Eliminar", DANGER_BG, DANGER, new Color(230, 190, 190)));
            }
            return p;
        });

        userTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = userTable.rowAtPoint(e.getPoint());
                int col = userTable.columnAtPoint(e.getPoint());
                if (col != 6 || row < 0) return;

                boolean hasData = tableModel.getValueAt(row, 1) != null &&
                        !tableModel.getValueAt(row, 1).toString().isEmpty();
                if (!hasData) return;

                Rectangle cellRect = userTable.getCellRect(row, col, true);
                int cellW  = cellRect.width;
                int cellH  = cellRect.height;
                int localX = e.getX() - cellRect.x;
                int localY = e.getY() - cellRect.y;

                int btnW   = 62;
                int gap    = 5;
                int totalW = btnW * 2 + gap;
                int startX = (cellW - totalW) / 2;
                int btnY   = (cellH - 28) / 2;

                Rectangle editRect = new Rectangle(startX, btnY, btnW, 28);
                Rectangle delRect  = new Rectangle(startX + btnW + gap, btnY, btnW, 28);

                if (editRect.contains(localX, localY))     editarUsuario(row);
                else if (delRect.contains(localX, localY)) eliminarUsuario(row);
            }

            @Override public void mouseMoved(MouseEvent e) {
                int col = userTable.columnAtPoint(e.getPoint());
                userTable.setCursor(col == 6
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
            }
        });

        JScrollPane scroll = new JScrollPane(userTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(BG);
        return scroll;
    }

    // ── FORMULARIO ───────────────────────────────────────────────────────────

    private void openUserForm() {
        JDialog dialog = new JDialog(frame, "Nuevo usuario", true);
        dialog.setSize(720, 540);
        dialog.setLocationRelativeTo(frame);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG2);

        // Topbar
        JPanel topbar = new JPanel(new BorderLayout());
        topbar.setBackground(BG);
        topbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(14, 24, 14, 24)
        ));
        JLabel dTitle = new JLabel("Food UPB — Nuevo usuario");
        dTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        dTitle.setForeground(TXT);
        JLabel dSub = new JLabel("Administrador");
        dSub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        dSub.setForeground(TXT3);
        topbar.add(dTitle, BorderLayout.WEST);
        topbar.add(dSub,   BorderLayout.EAST);

        // Body
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(BG2);
        body.setBorder(new EmptyBorder(20, 28, 20, 28));

        // 2-column grid
        JPanel grid = new JPanel(new GridLayout(0, 2, 16, 10));
        grid.setOpaque(false);

        JTextField nombres   = roundField("Nombres completos");
        JTextField correo    = roundField("Correo electrónico");
        JTextField telefono  = roundField("Número de teléfono");
        JTextField direccion = roundField("Dirección completa");
        JPasswordField clave = roundPasswordField("Contraseña");
        JComboBox<String> rol  = styledCombo(new String[]{"CLIENTE", "OPERADOR", "ADMIN"});
        JComboBox<String> tipo = styledCombo(new String[]{"Estándar", "Premium"});

        // Tipo panel
        JPanel tipoPanel = new JPanel();
        tipoPanel.setLayout(new BoxLayout(tipoPanel, BoxLayout.Y_AXIS));
        tipoPanel.setOpaque(false);
        tipoPanel.add(formLabel("Tipo de cliente"));
        tipoPanel.add(Box.createVerticalStrut(6));
        tipo.setAlignmentX(Component.LEFT_ALIGNMENT);
        tipoPanel.add(tipo);

        JPanel tipoPlaceholder = new JPanel();
        tipoPlaceholder.setOpaque(false);

        grid.add(labeledField("Nombre",    nombres));
        grid.add(labeledField("Correo",    correo));
        grid.add(labeledField("Teléfono",  telefono));
        grid.add(labeledField("Dirección", direccion));
        grid.add(labeledField("Contraseña", clave));
        grid.add(labeledField("Rol",       rol));
        grid.add(tipoPanel);
        grid.add(tipoPlaceholder);

        boolean initialCliente = "CLIENTE".equals(rol.getSelectedItem());
        tipoPanel.setVisible(initialCliente);
        tipoPlaceholder.setVisible(!initialCliente);

        rol.addActionListener(e -> {
            boolean esCliente = "CLIENTE".equals(rol.getSelectedItem());
            tipoPanel.setVisible(esCliente);
            tipoPlaceholder.setVisible(!esCliente);
            dialog.revalidate();
            dialog.repaint();
        });

        // Bottom
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel status = new JLabel(" ");
        status.setFont(new Font("SansSerif", Font.PLAIN, 12));
        status.setForeground(DANGER);
        status.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton save = saveButton("Añadir usuario");
        save.setAlignmentX(Component.LEFT_ALIGNMENT);
        save.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        save.addActionListener(e -> {
            String n = nombres.getText().trim();
            String c = correo.getText().trim();
            String t = telefono.getText().trim();
            String r = (String) rol.getSelectedItem();

            if (isPlaceholder(nombres, "Nombres completos") || n.isEmpty()
                    || isPlaceholder(correo, "Correo electrónico")   || c.isEmpty()
                    || isPlaceholder(telefono, "Número de teléfono") || t.isEmpty()) {
                status.setText("Nombres, correo y teléfono son obligatorios.");
                return;
            }
            boolean esCliente = "CLIENTE".equals(r);
            String tp = esCliente ? (String) tipo.getSelectedItem() : "—";
            addRow(String.valueOf(tableModel.getRowCount() + 1), n, c, t, tp, r);
            dialog.dispose();
        });

        bottom.add(status);
        bottom.add(Box.createVerticalStrut(6));
        bottom.add(save);

        body.add(grid,   BorderLayout.CENTER);
        body.add(bottom, BorderLayout.SOUTH);

        root.add(topbar, BorderLayout.NORTH);
        root.add(body,   BorderLayout.CENTER);
        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    // ── FORM HELPERS ─────────────────────────────────────────────────────────

    private JPanel labeledField(String labelText, JComponent field) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel lbl = formLabel(labelText);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        p.add(lbl);
        p.add(Box.createVerticalStrut(6));
        p.add(field);
        return p;
    }

    private JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(TXT2);
        return l;
    }

    private JTextField roundField(String placeholder) {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        f.setFont(new Font("SansSerif", Font.PLAIN, 14));
        f.setForeground(TXT3);
        f.setBackground(BG);
        f.setOpaque(false);
        f.setBorder(new EmptyBorder(12, 14, 12, 14));
        f.setText(placeholder);

        f.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (f.getText().equals(placeholder)) { f.setText(""); f.setForeground(TXT); }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (f.getText().isEmpty()) { f.setForeground(TXT3); f.setText(placeholder); }
            }
        });
        return f;
    }

    private JPasswordField roundPasswordField(String placeholder) {
        JPasswordField f = new JPasswordField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        f.setFont(new Font("SansSerif", Font.PLAIN, 14));
        f.setForeground(TXT3);
        f.setBackground(BG);
        f.setOpaque(false);
        f.setBorder(new EmptyBorder(12, 14, 12, 14));
        f.setText(placeholder);
        f.setEchoChar((char) 0);

        f.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (new String(f.getPassword()).equals(placeholder)) {
                    f.setText(""); f.setForeground(TXT); f.setEchoChar('•');
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (f.getPassword().length == 0) {
                    f.setForeground(TXT3); f.setText(placeholder); f.setEchoChar((char) 0);
                }
            }
        });
        return f;
    }

    private JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(new Font("SansSerif", Font.PLAIN, 14));
        cb.setBackground(BG);
        cb.setForeground(TXT);
        return cb;
    }

    private JButton saveButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setBackground(TXT);
        btn.setForeground(BG);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(14, 0, 14, 0));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton addButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setBackground(TXT);
        btn.setForeground(BG);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton smallBtn(String text, Color bg, Color fg, Color border) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(5, 12, 5, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── UTILS ────────────────────────────────────────────────────────────────

    private boolean isPlaceholder(JTextField f, String placeholder) {
        return f.getText().equals(placeholder);
    }

    // ── ACCIONES ─────────────────────────────────────────────────────────────

    private void addRow(String id, String nombre, String correo, String telefono, String tipo, String rol) {
        tableModel.addRow(new Object[]{id, nombre, correo, telefono, tipo, rol, ""});
        refreshStats();
    }

    private void editarUsuario(int row) {
        String nombre = (String) tableModel.getValueAt(row, 1);
        JOptionPane.showMessageDialog(frame, "Editar: " + nombre);
    }

    private void eliminarUsuario(int row) {
        String nombre = (String) tableModel.getValueAt(row, 1);
        int ok = JOptionPane.showConfirmDialog(
                frame,
                "¿Eliminar usuario " + nombre + "?\nVerifica que no tenga pedidos activos.",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION
        );
        if (ok == JOptionPane.YES_OPTION) {
            tableModel.removeRow(row);
            refreshStats();
        }
    }

    // ── API PÚBLICA ──────────────────────────────────────────────────────────

    public void show() {
        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }

    public void clearTable() {
        tableModel.setRowCount(0);
        refreshStats();
    }
}