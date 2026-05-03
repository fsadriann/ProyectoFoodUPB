package edu.fsadriann.server.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class UIComponents {

    // ── COLORES GLOBALES ──────────────────────────────────────────────────────

    public static final Color BG     = Color.WHITE;
    public static final Color BG2    = new Color(247, 247, 245);
    public static final Color BORDER = new Color(225, 225, 222);
    public static final Color TXT    = new Color(20, 20, 20);
    public static final Color TXT2   = new Color(110, 110, 108);
    public static final Color TXT3   = new Color(170, 170, 168);
    public static final Color DANGER = new Color(180, 40, 40);

    // ── FUENTES ───────────────────────────────────────────────────────────────

    public static Font fontBold(int size)  { return new Font("SansSerif", Font.BOLD,  size); }
    public static Font fontPlain(int size) { return new Font("SansSerif", Font.PLAIN, size); }

    // ── CARD ──────────────────────────────────────────────────────────────────

    public static JPanel card() {
        JPanel panel = new JPanel() {
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
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(18, 20, 18, 20));
        return panel;
    }

    // ── ROUND BUTTON ──────────────────────────────────────────────────────────

    public static JButton roundBtn(String text, Color bg, Color fg, Color border) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                if (border != null) {
                    g2.setColor(border);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(fontBold(13));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton roundBtnSmall(String text, Color bg, Color fg, Color border) {
        JButton btn = roundBtn(text, bg, fg, border);
        btn.setFont(fontBold(11));
        btn.setBorder(new EmptyBorder(5, 11, 5, 11));
        return btn;
    }

    // ── STYLED FIELD ─────────────────────────────────────────────────────────

    public static JTextField styledField(String placeholder) {
        JTextField field = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        field.setFont(fontPlain(13));
        field.setForeground(TXT);
        field.setBackground(BG);
        field.setCaretColor(TXT);
        field.setOpaque(false);
        field.setBorder(new EmptyBorder(9, 13, 9, 13));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        if (placeholder != null && !placeholder.isEmpty()) {
            field.setToolTipText(placeholder);
        }
        return field;
    }

    // ── FIELD LABEL ───────────────────────────────────────────────────────────

    public static JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(fontBold(12));
        label.setForeground(TXT2);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    // ── TOPBAR ────────────────────────────────────────────────────────────────

    public static JPanel topbar(String titleText, String rightText) {
        JPanel topbar = new JPanel(new BorderLayout());
        topbar.setBackground(BG);
        topbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(16, 24, 16, 24)
        ));
        JLabel title = new JLabel(titleText);
        title.setFont(fontBold(17));
        title.setForeground(TXT);
        JLabel right = new JLabel(rightText);
        right.setFont(fontPlain(12));
        right.setForeground(TXT3);
        topbar.add(title, BorderLayout.WEST);
        topbar.add(right, BorderLayout.EAST);
        return topbar;
    }

    // ── SIDEBAR ───────────────────────────────────────────────────────────────

    public static JPanel sidebar(String brandName, String brandSub, String[] navItems, int activeIndex, JButton footerBtn) {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBackground(BG);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER));

        // Brand
        JPanel brand = new JPanel();
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBackground(BG);
        brand.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(20, 18, 16, 18)
        ));
        JLabel name = new JLabel(brandName);
        name.setFont(fontBold(15));
        name.setForeground(TXT);
        JLabel sub = new JLabel(brandSub);
        sub.setFont(fontPlain(11));
        sub.setForeground(TXT3);
        brand.add(name);
        brand.add(Box.createVerticalStrut(3));
        brand.add(sub);

        // Nav
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(BG);
        nav.setBorder(new EmptyBorder(10, 10, 10, 10));
        for (int i = 0; i < navItems.length; i++) {
            nav.add(navItem(navItems[i], i == activeIndex));
        }

        // Footer
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(BG);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                new EmptyBorder(12, 10, 12, 10)
        ));
        footer.add(footerBtn, BorderLayout.CENTER);

        sidebar.add(brand, BorderLayout.NORTH);
        sidebar.add(nav, BorderLayout.CENTER);
        sidebar.add(footer, BorderLayout.SOUTH);
        return sidebar;
    }

    public static JPanel navItem(String text, boolean active) {
        JPanel item = new JPanel(new BorderLayout());
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.setBorder(new EmptyBorder(0, 0, 2, 0));
        item.setOpaque(false);

        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", active ? Font.BOLD : Font.PLAIN, 13));
        label.setBorder(new EmptyBorder(8, 14, 8, 14));

        JPanel wrap = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
        };
        wrap.setOpaque(false);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.add(label, BorderLayout.CENTER);
        wrap.setBackground(active ? TXT : BG);

        if (active) {
            label.setForeground(BG);
        } else {
            label.setForeground(TXT2);
            wrap.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    wrap.setBackground(BG2);
                    label.setForeground(TXT);
                    wrap.repaint();
                }
                public void mouseExited(java.awt.event.MouseEvent e) {
                    wrap.setBackground(BG);
                    label.setForeground(TXT2);
                    wrap.repaint();
                }
            });
        }

        item.add(wrap, BorderLayout.CENTER);
        return item;
    }
}