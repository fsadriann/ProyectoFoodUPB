package edu.fsadriann.view.auth;

import edu.fsadriann.view.UIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginView extends JFrame {

    private JTextField userField;
    private JPasswordField passwordField;
    private JButton loginBtn;
    private JLabel statusLabel;

    public LoginView(String operadorEmail) {
        super("Food UPB — Acceso");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setBackground(UIComponents.BG2);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(UIComponents.BG2);
        root.add(buildCard());
        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel buildCard() {
        JPanel card = UIComponents.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(400, 350));
        card.setMaximumSize(new Dimension(400, 350));

        JLabel brand = new JLabel("Food UPB");
        brand.setFont(UIComponents.fontBold(20));
        brand.setForeground(UIComponents.ACCENT);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel brandSub = new JLabel("Sistema de gestión de pedidos");
        brandSub.setFont(UIComponents.fontPlain(11));
        brandSub.setForeground(UIComponents.TXT3);
        brandSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel sep = new JPanel();
        sep.setBackground(UIComponents.BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Iniciar sesión");
        title.setFont(UIComponents.fontBold(18));
        title.setForeground(UIComponents.TXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Ingresa con tus credenciales para continuar");
        subtitle.setFont(UIComponents.fontPlain(12));
        subtitle.setForeground(UIComponents.TXT2);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        Dimension fieldSize = new Dimension(Integer.MAX_VALUE, 42);

        userField = new JTextField();
        styleField(userField, "Usuario / correo", fieldSize);

        passwordField = new JPasswordField();
        styleField(passwordField, "Contraseña", fieldSize);

        loginBtn = UIComponents.roundBtn("Iniciar sesión", UIComponents.ACCENT, Color.WHITE, null);
        loginBtn.setFont(UIComponents.fontBold(13));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIComponents.fontPlain(12));
        statusLabel.setForeground(UIComponents.TXT3);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(brand);
        card.add(Box.createVerticalStrut(3));
        card.add(brandSub);
        card.add(Box.createVerticalStrut(16));
        card.add(sep);
        card.add(Box.createVerticalStrut(16));
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(16));
        card.add(userField);
        card.add(Box.createVerticalStrut(8));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(14));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(statusLabel);

        return card;
    }

    private void styleField(JTextField field, String placeholder, Dimension size) {
        field.setFont(UIComponents.fontPlain(13));
        field.setBackground(UIComponents.BG);
        field.setForeground(UIComponents.TXT3);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 1, 1, 1, UIComponents.BORDER),
            new EmptyBorder(9, 12, 9, 12)
        ));
        field.setMaximumSize(size);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setText(placeholder);

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(UIComponents.TXT);
                    if (field instanceof JPasswordField) ((JPasswordField) field).setEchoChar('•');
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(UIComponents.TXT3);
                    field.setText(placeholder);
                    if (field instanceof JPasswordField) ((JPasswordField) field).setEchoChar((char) 0);
                }
            }
        });

        if (field instanceof JPasswordField) ((JPasswordField) field).setEchoChar((char) 0);
    }

    // ── Controller API ─────────────────────────────────────────────────────────

    public void addLoginListener(Runnable r) {
        loginBtn.addActionListener(e -> r.run());
    }

    public String getUserText() {
        String t = userField.getText().trim();
        return "Usuario / correo".equals(t) ? "" : t;
    }

    public String getPasswordText() {
        String t = new String(passwordField.getPassword()).trim();
        return "Contraseña".equals(t) ? "" : t;
    }

    public void setLoginEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> loginBtn.setEnabled(enabled));
    }

    public void setMessage(String message) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(message));
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }
}
