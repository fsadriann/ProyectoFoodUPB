package edu.fsadriann.view.operator;

import edu.fsadriann.server.model.user.Rol;
import edu.fsadriann.server.model.user.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.UUID;

public class RegisterClientDialog extends JDialog {

    private static final Color BG       = Color.WHITE;
    private static final Color BLUE     = new Color(24, 95, 165);
    private static final Color BORDER_C = new Color(220, 220, 220);
    private static final Color TEXT2    = new Color(100, 100, 100);
    private static final Color BG2      = new Color(245, 245, 245);

    private JTextField nombreField;
    private JTextField apellidoField;
    private JTextField telefonoField;
    private JTextField direccionField;

    private User result = null;

    public RegisterClientDialog(JFrame parent, String telefonoPrellenado) {
        super(parent, "Registrar nuevo cliente", true);
        setBackground(BG);
        buildUI(telefonoPrellenado);
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void buildUI(String telefonoPrellenado) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(20, 24, 16, 24));

        // ── Título
        JLabel title = new JLabel("Nuevo cliente");
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        title.setForeground(BLUE);
        title.setBorder(new EmptyBorder(0, 0, 16, 0));
        root.add(title, BorderLayout.NORTH);

        // ── Formulario
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG);

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor  = GridBagConstraints.WEST;
        lc.insets  = new Insets(5, 0, 5, 14);
        lc.gridx   = 0;

        GridBagConstraints fc = new GridBagConstraints();
        fc.fill    = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1;
        fc.insets  = new Insets(5, 0, 5, 0);
        fc.gridx   = 1;

        // Teléfono
        lc.gridy = fc.gridy = 0;
        form.add(fieldLabel("Teléfono *"), lc);
        telefonoField = styledField(
                telefonoPrellenado != null ? telefonoPrellenado : "", 20);
        if (telefonoPrellenado != null && !telefonoPrellenado.isBlank()) {
            telefonoField.setEditable(false);
            telefonoField.setBackground(BG2);
        }
        form.add(telefonoField, fc);

        // Nombre
        lc.gridy = fc.gridy = 1;
        form.add(fieldLabel("Nombre *"), lc);
        nombreField = styledField("", 20);
        form.add(nombreField, fc);

        // Apellido
        lc.gridy = fc.gridy = 2;
        form.add(fieldLabel("Apellido *"), lc);
        apellidoField = styledField("", 20);
        form.add(apellidoField, fc);

        // Dirección
        lc.gridy = fc.gridy = 3;
        form.add(fieldLabel("Dirección *"), lc);
        direccionField = styledField("", 20);
        form.add(direccionField, fc);

        root.add(form, BorderLayout.CENTER);

        // ── Botones
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setBackground(BG);
        btnRow.setBorder(new EmptyBorder(18, 0, 0, 0));

        JButton cancelBtn = new JButton("Cancelar");
        cancelBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> dispose());

        JButton registerBtn = new JButton("Registrar");
        registerBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        registerBtn.setBackground(BLUE);
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setOpaque(true);
        registerBtn.setBorderPainted(false);
        registerBtn.setFocusPainted(false);
        registerBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerBtn.addActionListener(e -> handleRegister());

        getRootPane().setDefaultButton(registerBtn);
        btnRow.add(cancelBtn);
        btnRow.add(registerBtn);
        root.add(btnRow, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void handleRegister() {
        String nombre    = nombreField.getText().trim();
        String apellido  = apellidoField.getText().trim();
        String telefono  = telefonoField.getText().trim();
        String direccion = direccionField.getText().trim();

        if (nombre.isBlank() || apellido.isBlank()
                || telefono.isBlank() || direccion.isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "Todos los campos son obligatorios.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!telefono.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this,
                    "El teléfono debe tener 10 dígitos.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            telefonoField.requestFocus();
            return;
        }

        // Cédula interna generada automáticamente (no se muestra al operador)
        String cedulaInterna = "CLI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        // Correo interno derivado del teléfono para cumplir el contrato de User
        String correoInterno = "tel_" + telefono + "@foodupb.local";

        result = new User(
                correoInterno,
                nombre,
                apellido,
                Rol.CLIENTE,
                cedulaInterna,
                true,
                telefono,
                direccion,
                null
        );
        dispose();
    }

    /** Retorna el User construido, o null si el operador canceló. */
    public User getResult() {
        return result;
    }

    // ── helpers de UI ────────────────────────────────────────────────────────

    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(TEXT2);
        return lbl;
    }

    private JTextField styledField(String value, int cols) {
        JTextField f = new JTextField(value, cols);
        f.setFont(new Font("SansSerif", Font.PLAIN, 12));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_C),
                new EmptyBorder(4, 8, 4, 8)));
        return f;
    }
}