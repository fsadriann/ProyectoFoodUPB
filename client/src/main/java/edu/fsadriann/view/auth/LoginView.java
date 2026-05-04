package edu.fsadriann.view.auth;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class LoginView extends JFrame {

    private static final Color BG      = Color.WHITE;
    private static final Color BG2     = new Color(245, 245, 245);
    private static final Color BLUE    = new Color(24, 95, 165);
    private static final Color TEXT    = new Color(30, 30, 30);
    private static final Color TEXT2   = new Color(100, 100, 100);
    private static final Color BORDER_C = new Color(220, 220, 220);

    private JTextField     userField;
    private JPasswordField passwordField;
    private JButton        loginBtn;
    private JLabel         statusLabel;

    public LoginView(String operadorEmail) {
        super("Food UPB — Acceso");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(BG2);
        root.add(buildCard());
        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel buildCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_C), new EmptyBorder(28, 28, 28, 28)
        ));
        card.setPreferredSize(new Dimension(380, 340));

        JLabel brand = new JLabel("Food UPB");
        brand.setFont(new Font("SansSerif", Font.BOLD, 20));
        brand.setForeground(BLUE);
        brand.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Sistema de gestión de pedidos");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        sub.setForeground(TEXT2);
        sub.setAlignmentX(LEFT_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(BORDER_C);
        sep.setAlignmentX(LEFT_ALIGNMENT);

        JLabel title = new JLabel("Iniciar sesión");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(TEXT);
        title.setAlignmentX(LEFT_ALIGNMENT);

        Dimension fieldSize = new Dimension(Integer.MAX_VALUE, 38);

        userField = new JTextField();
        setupField(userField, "Usuario / correo", fieldSize);

        passwordField = new JPasswordField();
        setupField(passwordField, "Contraseña", fieldSize);

        loginBtn = new JButton("Iniciar sesión");
        loginBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        loginBtn.setBackground(BLUE);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setOpaque(true);
        loginBtn.setBorderPainted(false);
        loginBtn.setFocusPainted(false);
        loginBtn.setMaximumSize(fieldSize);
        loginBtn.setAlignmentX(LEFT_ALIGNMENT);
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusLabel.setForeground(TEXT2);
        statusLabel.setAlignmentX(LEFT_ALIGNMENT);

        card.add(brand);
        card.add(Box.createVerticalStrut(3));
        card.add(sub);
        card.add(Box.createVerticalStrut(14));
        card.add(sep);
        card.add(Box.createVerticalStrut(14));
        card.add(title);
        card.add(Box.createVerticalStrut(14));
        card.add(userField);
        card.add(Box.createVerticalStrut(8));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(12));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(8));
        card.add(statusLabel);
        return card;
    }

    private void setupField(JTextField f, String placeholder, Dimension size) {
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setForeground(TEXT2);
        f.setBackground(BG);
        f.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_C), new EmptyBorder(6, 10, 6, 10)
        ));
        f.setMaximumSize(size);
        f.setAlignmentX(LEFT_ALIGNMENT);
        f.setText(placeholder);
        if (f instanceof JPasswordField) ((JPasswordField) f).setEchoChar((char) 0);

        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (f.getText().equals(placeholder)) {
                    f.setText("");
                    f.setForeground(TEXT);
                    if (f instanceof JPasswordField) ((JPasswordField) f).setEchoChar('•');
                }
            }
            public void focusLost(FocusEvent e) {
                if (f.getText().isEmpty()) {
                    f.setForeground(TEXT2);
                    f.setText(placeholder);
                    if (f instanceof JPasswordField) ((JPasswordField) f).setEchoChar((char) 0);
                }
            }
        });
    }

    // ── Public API ─────────────────────────────────────────────────────────────

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
