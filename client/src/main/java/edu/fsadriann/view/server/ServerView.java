package edu.fsadriann.view.server;

import edu.fsadriann.view.UIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ServerView extends JFrame {

	private final JFrame frame;
	private JLabel statusLabel;
	private JButton logoutBtn;
	private JTextArea consoleArea;

	public ServerView(String userLabel) {
		super("Food UPB — Panel Operativo");
		frame = this;
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(1080, 700));
		setLocationRelativeTo(null);

		JPanel root = new JPanel(new BorderLayout());
		root.setBackground(UIComponents.BG2);
		root.add(buildTopbar(userLabel), BorderLayout.NORTH);
		root.add(buildContent(), BorderLayout.CENTER);
		root.add(buildStatusBar(), BorderLayout.SOUTH);
		setContentPane(root);
		pack();
	}

	private JPanel buildTopbar(String userLabel) {
		JPanel top = UIComponents.topbar("Food UPB - Panel Operativo", userLabel == null ? "Servidor" : userLabel);
		logoutBtn = UIComponents.roundBtnSmall("Cerrar sesión", UIComponents.BG, UIComponents.TXT, UIComponents.BORDER);
		JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		right.setOpaque(false);
		right.add(logoutBtn);
		top.add(right, BorderLayout.EAST);
		return top;
	}

	private JPanel buildContent() {
		JPanel content = new JPanel(new GridLayout(2, 2, 16, 16));
		content.setBorder(new EmptyBorder(18, 18, 18, 18));
		content.setBackground(UIComponents.BG2);

		content.add(card("Monitoreo del sistema", "Estado general y disponibilidad"));
		content.add(card("Cocina", "Cola activa y fogones"));
		content.add(card("Pedidos activos", "Seguimiento operativo"));
		content.add(buildConsoleCard());
		return content;
	}

	private JPanel card(String title, String subtitle) {
		JPanel card = UIComponents.card();
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		JLabel t = new JLabel(title);
		t.setFont(UIComponents.fontBold(18));
		t.setForeground(UIComponents.TXT);
		JLabel s = new JLabel(subtitle);
		s.setFont(UIComponents.fontPlain(13));
		s.setForeground(UIComponents.TXT2);
		card.add(t);
		card.add(Box.createVerticalStrut(8));
		card.add(s);
		return card;
	}

	private JPanel buildConsoleCard() {
		JPanel card = UIComponents.card();
		card.setLayout(new BorderLayout());
		JLabel title = new JLabel("Logs del sistema");
		title.setFont(UIComponents.fontBold(18));
		title.setForeground(UIComponents.TXT);
		card.add(title, BorderLayout.NORTH);

		consoleArea = new JTextArea();
		consoleArea.setEditable(false);
		consoleArea.setLineWrap(true);
		consoleArea.setWrapStyleWord(true);
		consoleArea.setBorder(new EmptyBorder(12, 12, 12, 12));
		consoleArea.setBackground(Color.WHITE);
		consoleArea.setText("Sistema listo.\n");
		card.add(new JScrollPane(consoleArea), BorderLayout.CENTER);
		return card;
	}

	private JPanel buildStatusBar() {
		JPanel bar = new JPanel(new BorderLayout());
		bar.setBackground(UIComponents.BG);
		bar.setBorder(new EmptyBorder(8, 18, 8, 18));
		statusLabel = new JLabel("Listo");
		statusLabel.setFont(UIComponents.fontPlain(12));
		statusLabel.setForeground(UIComponents.TXT2);
		bar.add(statusLabel, BorderLayout.WEST);
		return bar;
	}

	public void addLogoutListener(Runnable action) {
		logoutBtn.addActionListener(e -> action.run());
	}

	public void setMessage(String message) {
		SwingUtilities.invokeLater(() -> {
			statusLabel.setText(message);
			consoleArea.append(message + "\n");
		});
	}

	public void showView() {
		setVisible(true);
		toFront();
	}

	public void hideView() {
		setVisible(false);
	}

	public JFrame getFrame() {
		return frame;
	}
}