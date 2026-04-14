package erp.link_tech_erp.inventory;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.function.Consumer;

public class TableRenderers {

    // ── Category Pill Renderer ─────────────────────────────────────────────────
    public static class CategoryRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            p.setOpaque(true);
            p.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 251, 252));
            if (t.isRowSelected(row)) p.setBackground(new Color(232, 240, 254));
            p.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

            String cat = val == null ? "" : val.toString();
            Color[] colors = pillColors(cat);
            UIComponents.PillLabel pill = new UIComponents.PillLabel(cat, colors[0], colors[1]);
            p.add(pill);
            return p;
        }

        private Color[] pillColors(String cat) {
            switch (cat.toLowerCase()) {
                case "monitor":
                    return new Color[]{AppColors.PILL_MONITOR_BG, AppColors.PILL_MONITOR_FG};
                case "laptop":
                    return new Color[]{AppColors.PILL_LAPTOP_BG, AppColors.PILL_LAPTOP_FG};
                case "phone":
                    return new Color[]{AppColors.PILL_PHONE_BG, AppColors.PILL_PHONE_FG};
                case "tablet":
                    return new Color[]{AppColors.PILL_TABLET_BG, AppColors.PILL_TABLET_FG};
                default:
                    return new Color[]{AppColors.PILL_ACCESSORY_BG, AppColors.PILL_ACCESSORY_FG};
            }
        }
    }

    // ── Stock Bar Renderer ─────────────────────────────────────────────────────
    public static class StockRenderer extends DefaultTableCellRenderer {
        private final int threshold;
        public StockRenderer(int threshold) { this.threshold = threshold; }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                }
            };
            p.setOpaque(true);
            p.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 251, 252));
            if (t.isRowSelected(row)) p.setBackground(new Color(232, 240, 254));
            p.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            p.setLayout(new GridBagLayout());

            int stock = val == null ? 0 : (int) val;
            Color barColor = UIComponents.stockColor(stock, threshold);
            int maxStock = 200;
            float pct = Math.min(1f, (float) stock / maxStock);

            JPanel barContainer = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(AppColors.STOCK_BAR_BG);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                    g2.setColor(barColor);
                    g2.fillRoundRect(0, 0, (int)(getWidth() * pct), getHeight(), getHeight(), getHeight());
                    g2.dispose();
                }
            };
            barContainer.setOpaque(false);
            barContainer.setPreferredSize(new Dimension(48, 4));

            JLabel stockLbl = new JLabel(String.valueOf(stock));
            stockLbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            stockLbl.setForeground(stock <= threshold ? AppColors.STOCK_CRITICAL : AppColors.TEXT_PRIMARY);
            if (stock <= threshold && stock > 0) stockLbl.setForeground(AppColors.STOCK_LOW);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 0, 0, 8);
            p.add(stockLbl, gbc);
            gbc.insets = new Insets(0, 0, 0, 0);
            p.add(barContainer, gbc);
            return p;
        }
    }

    // ── Price Renderer ─────────────────────────────────────────────────────────
    public static class PriceRenderer extends DefaultTableCellRenderer {
        PriceRenderer() { setHorizontalAlignment(LEFT); }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
            double price = val == null ? 0 : (double) val;
            Component c = super.getTableCellRendererComponent(t, String.format("$%.2f", price), sel, foc, row, col);
            c.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 251, 252));
            if (t.isRowSelected(row)) c.setBackground(new Color(232, 240, 254));
            ((JLabel)c).setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            return c;
        }
    }

    // ── ID Renderer ───────────────────────────────────────────────────────────
    public static class IdRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
            c.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            c.setForeground(AppColors.TEXT_SECONDARY);
            c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 251, 252));
            if (t.isRowSelected(row)) c.setBackground(new Color(232, 240, 254));
            ((JLabel)c).setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            return c;
        }
    }

    // ── Name Renderer ──────────────────────────────────────────────────────────
    public static class NameRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
            c.setFont(new Font("Segoe UI Semibold", Font.BOLD, 16));
            c.setForeground(AppColors.TEXT_PRIMARY);
            c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 251, 252));
            if (t.isRowSelected(row)) c.setBackground(new Color(232, 240, 254));
            ((JLabel)c).setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            return c;
        }
    }

    // ── Supplier Renderer ─────────────────────────────────────────────────────
    public static class SupplierRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
            c.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            c.setForeground(AppColors.TEXT_SECONDARY);
            c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 251, 252));
            if (t.isRowSelected(row)) c.setBackground(new Color(232, 240, 254));
            ((JLabel)c).setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            return c;
        }
    }

    // ── Actions Button Renderer + Editor ──────────────────────────────────────
    public static class ActionsRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
            return buildPanel();
        }
        private JPanel buildPanel() {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 8));
            p.setOpaque(true);
            p.setBackground(Color.WHITE);
            JButton edit = smallBtn("Edit", false);
            JButton del  = smallBtn("Del", true);
            p.add(edit);
            p.add(del);
            return p;
        }
        private JButton smallBtn(String text, boolean danger) {
            JButton b = new JButton(text);
            b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            b.setForeground(danger ? AppColors.BTN_DANGER_FG : AppColors.TEXT_SECONDARY);
            b.setBackground(Color.WHITE);
            b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.BORDER, 1, true),
                BorderFactory.createEmptyBorder(3, 9, 3, 9)
            ));
            b.setFocusPainted(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return b;
        }
    }

    public static class ActionsEditor extends DefaultCellEditor {
        private JPanel panel;
        private final Consumer<String> onEdit;
        private final Consumer<String> onDelete;
        private JTable table;

        public ActionsEditor(JCheckBox dummy, Consumer<String> onEdit, Consumer<String> onDelete) {
            super(dummy);
            this.onEdit = onEdit;
            this.onDelete = onDelete;
            setClickCountToStart(1);
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object val,
                boolean sel, int row, int col) {
            this.table = t;
            panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 8));
            panel.setOpaque(true);
            panel.setBackground(Color.WHITE);

            JButton edit = smallBtn("Edit", false);
            JButton del  = smallBtn("Del", true);

            edit.addActionListener(e -> {
                stopCellEditing();
                if (onEdit != null) onEdit.accept(String.valueOf(row));
            });
            del.addActionListener(e -> {
                stopCellEditing();
                if (onDelete != null) onDelete.accept(String.valueOf(row));
            });

            panel.add(edit);
            panel.add(del);
            return panel;
        }

        @Override public Object getCellEditorValue() { return "actions"; }

        private JButton smallBtn(String text, boolean danger) {
            JButton b = new JButton(text);
            b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            b.setForeground(danger ? AppColors.BTN_DANGER_FG : AppColors.TEXT_SECONDARY);
            b.setBackground(Color.WHITE);
            b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.BORDER, 1, true),
                BorderFactory.createEmptyBorder(3, 9, 3, 9)
            ));
            b.setFocusPainted(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return b;
        }
    }
}
