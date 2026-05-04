package edu.fsadriann.server.view;

import edu.fsadriann.server.model.history.History;
import edu.fsadriann.server.model.observer.Observer;
import edu.fsadriann.server.model.observer.Subject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.UnaryOperator;

public class ServerView extends Observer {
  private String title;
  private JFrame frame;
  private JButton startButton;
  private JButton stopButton;

  private JTextArea consoleArea;
  private JLabel statusDot;
  private JLabel statusLabel;

  public ServerView(String title, Subject subject) {
    super(subject);
    this.title = title;
    this.frame = new JFrame(title);
  }

  public void initComponents(UnaryOperator<Void> onStart, UnaryOperator<Void> onStop) {
    if (GraphicsEnvironment.isHeadless()) return;

    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBackground(new Color(20, 20, 20));

    // ── HEADER ──────────────────────────────────────
    JPanel header = new JPanel(new BorderLayout());
    header.setBackground(new Color(28, 28, 28));
    header.setBorder(new EmptyBorder(18, 24, 18, 24));

    JLabel titleLabel = new JLabel("Server FoodUPB");
    titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
    titleLabel.setForeground(Color.WHITE);

    JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
    statusPanel.setOpaque(false);
    statusDot = new JLabel("●");
    statusDot.setFont(new Font("SansSerif", Font.PLAIN, 14));
    statusDot.setForeground(new Color(180, 180, 180));
    statusLabel = new JLabel("Detenido");
    statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
    statusLabel.setForeground(new Color(180, 180, 180));
    statusPanel.add(statusDot);
    statusPanel.add(statusLabel);

    header.add(titleLabel, BorderLayout.WEST);
    header.add(statusPanel, BorderLayout.EAST);

    // ── CONSOLA ──────────────────────────────────────
    consoleArea = new JTextArea();
    consoleArea.setEditable(false);
    consoleArea.setBackground(new Color(12, 12, 12));
    consoleArea.setForeground(new Color(80, 220, 120));
    consoleArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
    consoleArea.setBorder(new EmptyBorder(12, 16, 12, 16));
    consoleArea.setLineWrap(true);
    consoleArea.setWrapStyleWord(true);
    consoleArea.setText("> Consola lista.\n> Esperando inicio del servidor...\n");

    JScrollPane scrollPane = new JScrollPane(consoleArea);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());

    JPanel consoleWrapper = new JPanel(new BorderLayout());
    consoleWrapper.setBackground(new Color(20, 20, 20));
    consoleWrapper.setBorder(new EmptyBorder(12, 16, 0, 16));

    JLabel consoleTitle = new JLabel("CONSOLA");
    consoleTitle.setFont(new Font("Monospaced", Font.BOLD, 11));
    consoleTitle.setForeground(new Color(100, 100, 100));
    consoleTitle.setBorder(new EmptyBorder(0, 0, 6, 0));
    consoleWrapper.add(consoleTitle, BorderLayout.NORTH);
    consoleWrapper.add(scrollPane, BorderLayout.CENTER);

    // ── BOTONES ───────────────────────────────────────
    startButton = makeButton("▶  Iniciar Servidor", new Color(34, 197, 94));
    stopButton  = makeButton("■  Detener Servidor", new Color(220, 60, 60));
    stopButton.setEnabled(false);

    startButton.addActionListener(e -> onStart.apply(null));
    stopButton.addActionListener(e -> onStop.apply(null));

    JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
    buttonPanel.setBackground(new Color(20, 20, 20));
    buttonPanel.setBorder(new EmptyBorder(16, 16, 20, 16));
    buttonPanel.add(startButton);
    buttonPanel.add(stopButton);

    mainPanel.add(header, BorderLayout.NORTH);
    mainPanel.add(consoleWrapper, BorderLayout.CENTER);
    mainPanel.add(buttonPanel, BorderLayout.SOUTH);

    frame.setContentPane(mainPanel);
    frame.setSize(620, 440);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setResizable(false);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  private JButton makeButton(String text, Color color) {
    JButton btn = new JButton(text) {
      @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(isEnabled() ? color : new Color(50, 50, 50));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
        g2.dispose();
        super.paintComponent(g);
      }
    };
    btn.setForeground(Color.WHITE);
    btn.setFont(new Font("SansSerif", Font.BOLD, 13));
    btn.setOpaque(false);
    btn.setContentAreaFilled(false);
    btn.setBorderPainted(false);
    btn.setFocusPainted(false);
    btn.setPreferredSize(new Dimension(0, 46));
    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    return btn;
  }

  public void startStatus(String status) {
    startButton.setEnabled(false);
    stopButton.setEnabled(true);
    statusDot.setForeground(new Color(34, 197, 94));
    statusLabel.setForeground(new Color(34, 197, 94));
    statusLabel.setText("Activo");
    getHistory().addAction(status);
    appendConsole(status);
  }

  public void stopStatus(String status) {
    startButton.setEnabled(true);
    stopButton.setEnabled(false);
    statusDot.setForeground(new Color(180, 180, 180));
    statusLabel.setForeground(new Color(180, 180, 180));
    statusLabel.setText("Detenido");
    getHistory().addAction(status);
    appendConsole(status);
  }

  @Override
  public void update() {
    appendConsole(getHistory().getLastAction());
  }

  public void setMessage(String msg) {
    getHistory().addAction(msg);
    appendConsole(msg);
  }

  private void appendConsole(String msg) {
    SwingUtilities.invokeLater(() -> {
      consoleArea.append("> " + msg + "\n");
      consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
    });
  }

  public History getHistory() {
    return (History) subject;
  }
}
