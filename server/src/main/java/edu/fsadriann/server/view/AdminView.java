package edu.fsadriann.server.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class AdminView {

    private JFrame frame;
    private DefaultTableModel tableModel;
    private JTable userTable;

    public AdminView(String adminEmail) {
        buildUI(adminEmail);
    }

    private void buildUI(String adminEmail) {
        frame = new JFrame("Food UPB — Panel de Administrador");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(960, 600);
        frame.setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIComponents.BG);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildMain(adminEmail), BorderLayout.CENTER);

        frame.setContentPane(root);
    }

    // ── SIDEBAR ───────────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JButton logout = UIComponents.roundBtn("Cerrar sesión", UIComponents.BG, UIComponents.TXT, UIComponents.BORDER);
        logout.addActionListener(e -> {
            frame.dispose();
            // TODO: volver al login
        });
        String[] items = {"Usuarios", "Productos", "Rutas", "Operadores", "Auditoría"};
        return UIComponents.sidebar("Food UPB", "Panel de administrador", items, 0, logout);
    }

    // ── MAIN ──────────────────────────────────────────────────────────────────

    private JPanel buildMain(String adminEmail) {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UIComponents.BG);

        JPanel topbar = UIComponents.topbar("Gestión de usuarios", adminEmail);

        // Content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UIComponents.BG);
        content.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Section bar
        JPanel sectionBar = new JPanel(new BorderLayout());
        sectionBar.setOpaque(false);
        sectionBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JLabel sectionLabel = new JLabel("Usuarios registrados");
        sectionLabel.setFont(UIComponents.fontPlain(12));
        sectionLabel.setForeground(UIComponents.TXT2);
        JButton addBtn = UIComponents.roundBtn("+ Añadir usuario", UIComponents.TXT, UIComponents.BG, null);
        sectionBar.add(sectionLabel, BorderLayout.WEST);
        sectionBar.add(addBtn, BorderLayout.EAST);
        addBtn.addActionListener(e -> agregarUsuario());

        content.add(sectionBar);
        content.add(Box.createVerticalStrut(12));
        content.add(buildTable());

        main.add(topbar, BorderLayout.NORTH);
        main.add(content, BorderLayout.CENTER);
        return main;
    }

    // ── TABLA ─────────────────────────────────────────────────────────────────

    private JScrollPane buildTable() {
        String[] cols = {"ID", "Nombre", "Correo", "Rol", "Acciones"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tableModel.addRow(new Object[]{"1", "Laura Martínez", "lmartinez@upb.com", "Operador", ""});
        tableModel.addRow(new Object[]{"2", "Carlos Ruiz", "cruiz@upb.com", "Admin", ""});
        tableModel.addRow(new Object[]{"3", "Ana Gómez", "agomez@upb.com", "Operador", ""});
        tableModel.addRow(new Object[]{"4", "", "", "", ""});
        tableModel.addRow(new Object[]{"5", "", "", "", ""});

        userTable = new JTable(tableModel);
        userTable.setFont(UIComponents.fontPlain(13));
        userTable.setRowHeight(42);
        userTable.setBackground(UIComponents.BG);
        userTable.setForeground(UIComponents.TXT);
        userTable.setGridColor(UIComponents.BORDER);
        userTable.setShowHorizontalLines(true);
        userTable.setShowVerticalLines(false);
        userTable.setSelectionBackground(UIComponents.BG2);
        userTable.setSelectionForeground(UIComponents.TXT);
        userTable.setFocusable(false);
        userTable.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = userTable.getTableHeader();
        header.setBackground(UIComponents.BG2);
        header.setForeground(UIComponents.TXT2);
        header.setFont(UIComponents.fontBold(11));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIComponents.BORDER));
        header.setReorderingAllowed(false);

        int[] widths = {40, 140, 180, 100, 130};
        for (int i = 0; i < widths.length; i++)
            userTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Renderer ID en gris
        userTable.getColumnModel().getColumn(0).setCellRenderer((t, v, s, f, r, c) -> {
            JLabel l = new JLabel(v == null ? "" : v.toString());
            l.setFont(UIComponents.fontPlain(12));
            l.setForeground(UIComponents.TXT3);
            l.setBorder(new EmptyBorder(0, 16, 0, 0));
            l.setOpaque(true);
            l.setBackground(s ? UIComponents.BG2 : UIComponents.BG);
            return l;
        });

        // Renderer correo en gris
        userTable.getColumnModel().getColumn(2).setCellRenderer((t, v, s, f, r, c) -> {
            JLabel l = new JLabel(v == null ? "" : v.toString());
            l.setFont(UIComponents.fontPlain(12));
            l.setForeground(UIComponents.TXT2);
            l.setBorder(new EmptyBorder(0, 16, 0, 0));
            l.setOpaque(true);
            l.setBackground(s ? UIComponents.BG2 : UIComponents.BG);
            return l;
        });

        // Renderer acciones
        userTable.getColumnModel().getColumn(4).setCellRenderer((t, v, s, f, row, c) -> {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
            p.setBackground(s ? UIComponents.BG2 : UIComponents.BG);
            boolean hasData = tableModel.getValueAt(row, 1) != null &&
                    !tableModel.getValueAt(row, 1).toString().isEmpty();
            if (hasData) {
                p.add(UIComponents.roundBtnSmall("Editar", UIComponents.BG2, UIComponents.TXT, UIComponents.BORDER));
                p.add(UIComponents.roundBtnSmall("Eliminar", UIComponents.BG, UIComponents.DANGER, new Color(220, 180, 180)));
            }
            return p;
        });

        userTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = userTable.rowAtPoint(e.getPoint());
                int col = userTable.columnAtPoint(e.getPoint());
                if (col == 4 && row >= 0) {
                    boolean hasData = tableModel.getValueAt(row, 1) != null &&
                            !tableModel.getValueAt(row, 1).toString().isEmpty();
                    if (!hasData) return;
                    Rectangle rect = userTable.getCellRect(row, col, true);
                    int x = e.getX() - rect.x;
                    if (x < 65) editarUsuario(row);
                    else eliminarUsuario(row);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(userTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIComponents.BORDER, 1));
        scroll.getViewport().setBackground(UIComponents.BG);
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 500));
        return scroll;
    }

    // ── ACCIONES ──────────────────────────────────────────────────────────────

    private void agregarUsuario() {
        // TODO: formulario nuevo usuario (RF-07)
        JOptionPane.showMessageDialog(frame, "Aquí abrirías el formulario de nuevo usuario.");
    }

    private void editarUsuario(int row) {
        String nombre = (String) tableModel.getValueAt(row, 1);
        // TODO: formulario edición (RF-07)
        JOptionPane.showMessageDialog(frame, "Editando: " + nombre);
    }

    private void eliminarUsuario(int row) {
        String nombre = (String) tableModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(frame,
                "¿Eliminar al usuario " + nombre + "?\nVerifica que no tenga pedidos activos.",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION)
            tableModel.removeRow(row);
    }

    // ── PÚBLICOS ──────────────────────────────────────────────────────────────

    public void show() {
        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }

    public void addRow(String id, String nombre, String correo, String rol) {
        tableModel.addRow(new Object[]{id, nombre, correo, rol, ""});
    }

    public void clearTable() {
        tableModel.setRowCount(0);
    }
}