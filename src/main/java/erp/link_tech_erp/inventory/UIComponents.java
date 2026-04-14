package erp.link_tech_erp.inventory;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class UIComponents {

    // ── Rounded Panel ─────────────────────────────────────────────────────────
    public static class RoundedPanel extends JPanel {
        private final int radius;
        private Color borderColor;
        public RoundedPanel(int radius) {
            this(radius, AppColors.BORDER);
        }
        public RoundedPanel(int radius, Color borderColor) {
            this.radius = radius;
            this.borderColor = borderColor;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));
            g2.dispose();
        }
        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, radius, radius));
            g2.dispose();
        }
        @Override
        public boolean isOpaque() { return false; }
    }

    // ── Pill / Badge label ─────────────────────────────────────────────────────
    public static class PillLabel extends JLabel {
        private Color bg, fg;
        public PillLabel(String text, Color bg, Color fg) {
            super(text);
            this.bg = bg;
            this.fg = fg;
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setForeground(fg);
            setBorder(new EmptyBorder(3, 10, 3, 10));
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ── Stat Card ──────────────────────────────────────────────────────────────
    public static JPanel createStatCard(String label, String value, String badge, Color badgeBg, Color badgeFg) {
        RoundedPanel card = new RoundedPanel(12);
        card.setBackground(AppColors.CARD_BG);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(AppColors.TEXT_SECONDARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI Semibold", Font.BOLD, 22));
        val.setForeground(AppColors.TEXT_PRIMARY);
        val.setAlignmentX(Component.LEFT_ALIGNMENT);

        PillLabel badgeLbl = new PillLabel(badge, badgeBg, badgeFg);
        badgeLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        inner.add(lbl);
        inner.add(Box.createVerticalStrut(6));
        inner.add(val);
        inner.add(Box.createVerticalStrut(6));
        inner.add(badgeLbl);

        card.add(inner);
        return card;
    }

    // ── Modern Button ──────────────────────────────────────────────────────────
    public static JButton createButton(String text, Color bg, Color fg, Color border) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? bg.darker() : getModel().isRollover() ? bg.darker() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(border);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(7, 14, 7, 14));
        btn.setOpaque(false);
        return btn;
    }

    public static JButton createPrimaryButton(String text) {
        return createButton(text, AppColors.ACCENT, Color.WHITE, AppColors.ACCENT);
    }

    public static JButton createSecondaryButton(String text) {
        return createButton(text, AppColors.CARD_BG, AppColors.TEXT_PRIMARY, AppColors.BORDER);
    }

    public static JButton createDangerButton(String text) {
        return createButton(text, AppColors.BTN_DANGER_BG, AppColors.BTN_DANGER_FG, AppColors.BTN_DANGER_BORDER);
    }

    // ── Styled TextField ───────────────────────────────────────────────────────
    public static JTextField createTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.setBackground(AppColors.INPUT_BG);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppColors.BORDER, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        return field;
    }

    // ── Styled ComboBox ────────────────────────────────────────────────────────
    public static JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setBackground(AppColors.INPUT_BG);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        combo.setBorder(BorderFactory.createLineBorder(AppColors.BORDER, 1, true));
        return combo;
    }

    // ── Section label ──────────────────────────────────────────────────────────
    public static JLabel createSectionLabel(String text) {
        JLabel lbl = new JLabel(text.toUpperCase());
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(AppColors.TEXT_SECONDARY);
        return lbl;
    }

    // ── Stock bar renderer helper ──────────────────────────────────────────────
    public static Color stockColor(int stock, int threshold) {
        if (stock == 0) return AppColors.STOCK_CRITICAL;
        if (stock <= threshold) return AppColors.STOCK_LOW;
        return AppColors.STOCK_OK;
    }
}
