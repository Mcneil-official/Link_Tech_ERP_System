package erp.link_tech_erp.inventory;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;

public class AnalyticsPanel extends JPanel {

    public AnalyticsPanel() {
        setBackground(AppColors.PAGE_BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(24, 28, 24, 28));

        add(buildHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(buildMetricsRow());
        body.add(Box.createVerticalStrut(16));
        body.add(buildChartsRow());
        body.add(Box.createVerticalStrut(16));
        body.add(buildInventoryHealth());

        JScrollPane sp = new JScrollPane(body);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        add(sp, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Analytics & Reports");
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 28));
        title.setForeground(AppColors.TEXT_PRIMARY);

        JLabel sub = new JLabel("Monitor inventory trends, performance metrics, and business insights.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        sub.setForeground(AppColors.TEXT_SECONDARY);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(Box.createVerticalStrut(3));
        left.add(sub);

        p.add(left, BorderLayout.WEST);
        return p;
    }

    private JPanel buildMetricsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        row.add(makeMetricCard("Stock Turnover", "2.4x", "Products/Month", "#4CAF50"));
        row.add(makeMetricCard("Inventory Accuracy", "99.2%", "Count vs. System", "#2196F3"));
        row.add(makeMetricCard("Supplier Performance", "96.5%", "On-time Delivery", "#FF9800"));
        row.add(makeMetricCard("Inventory Cost", "$24.5k", "Monthly Holding", "#F44336"));

        return row;
    }

    private JPanel makeMetricCard(String label, String value, String subtitle, String colorHex) {
        UIComponents.RoundedPanel card = new UIComponents.RoundedPanel(12);
        card.setBackground(AppColors.CARD_BG);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(AppColors.TEXT_SECONDARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("Segoe UI Semibold", Font.BOLD, 32));
        valueLbl.setForeground(Color.decode(colorHex));
        valueLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLbl.setForeground(AppColors.TEXT_SECONDARY);
        subLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        inner.add(lbl);
        inner.add(Box.createVerticalStrut(8));
        inner.add(valueLbl);
        inner.add(Box.createVerticalStrut(8));
        inner.add(subLbl);
        card.add(inner);
        return card;
    }

    private JPanel buildChartsRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        row.add(buildCategoryDistribution());
        row.add(buildStockLevelChart());

        return row;
    }

    private JPanel buildCategoryDistribution() {
        UIComponents.RoundedPanel card = new UIComponents.RoundedPanel(12);
        card.setBackground(AppColors.CARD_BG);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel title = new JLabel("Category Distribution");
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 18));
        title.setForeground(AppColors.TEXT_PRIMARY);

        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawCategoryChart(g);
            }
        };
        chartPanel.setOpaque(false);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setOpaque(false);
        header.add(title);

        card.add(header, BorderLayout.NORTH);
        card.add(chartPanel, BorderLayout.CENTER);
        return card;
    }

    private void drawCategoryChart(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = 200, h = 150, cx = 120, cy = 110;
        String[] categories = {"Monitor", "Laptop", "Phone", "Tablet", "Accessory"};
        int[] values = {50, 20, 8, 22, 200};
        int[] colors = {0x2196F3, 0x4CAF50, 0xFF9800, 0xF44336, 0x9C27B0};
        int total = 0;
        for (int v : values) total += v;

        double angle = 0;
        for (int i = 0; i < categories.length; i++) {
            double pct = (double) values[i] / total;
            int arcAngle = (int) (pct * 360);
            g2.setColor(new Color(colors[i]));
            g2.fillArc(cx - 70, cy - 70, 140, 140, (int) angle, arcAngle);
            angle += arcAngle;
        }

        // Legend
        int legendY = 30;
        for (int i = 0; i < categories.length; i++) {
            g2.setColor(new Color(colors[i]));
            g2.fillRect(300, legendY + i * 25, 15, 15);
            g2.setColor(AppColors.TEXT_PRIMARY);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            g2.drawString(categories[i] + ": " + values[i], 325, legendY + i * 25 + 12);
        }
    }

    private JPanel buildStockLevelChart() {
        UIComponents.RoundedPanel card = new UIComponents.RoundedPanel(12);
        card.setBackground(AppColors.CARD_BG);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel title = new JLabel("Stock Level Trend (Last 6 Months)");
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 18));
        title.setForeground(AppColors.TEXT_PRIMARY);

        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawStockTrendChart(g);
            }
        };
        chartPanel.setOpaque(false);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setOpaque(false);
        header.add(title);

        card.add(header, BorderLayout.NORTH);
        card.add(chartPanel, BorderLayout.CENTER);
        return card;
    }

    private void drawStockTrendChart(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int[] data = {85, 92, 88, 95, 90, 98};
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun"};
        int chartWidth = 450, chartHeight = 150, startX = 50, startY = 130;

        // Draw axes
        g2.setColor(AppColors.BORDER);
        g2.setStroke(new BasicStroke(1));
        g2.drawLine(startX, startY, startX + chartWidth, startY);
        g2.drawLine(startX, startY, startX, startY - chartHeight);

        // Draw grid lines and labels
        g2.setColor(AppColors.TEXT_SECONDARY);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        for (int i = 0; i <= 5; i++) {
            int y = startY - (chartHeight * i / 5);
            g2.setColor(new Color(200, 200, 200));
            g2.setStroke(new BasicStroke(1));
            g2.drawLine(startX, y, startX + chartWidth, y);
            g2.setColor(AppColors.TEXT_SECONDARY);
            g2.drawString((i * 20) + "%", startX - 35, y + 4);
        }

        // Draw data line and points
        g2.setColor(new Color(0x2196F3));
        g2.setStroke(new BasicStroke(3));
        for (int i = 0; i < data.length - 1; i++) {
            int x1 = startX + (i * chartWidth / 5);
            int y1 = startY - (data[i] * chartHeight / 100);
            int x2 = startX + ((i + 1) * chartWidth / 5);
            int y2 = startY - (data[i + 1] * chartHeight / 100);
            g2.drawLine(x1, y1, x2, y2);
        }

        // Draw points
        g2.setColor(new Color(0x2196F3));
        for (int i = 0; i < data.length; i++) {
            int x = startX + (i * chartWidth / 5);
            int y = startY - (data[i] * chartHeight / 100);
            g2.fillOval(x - 4, y - 4, 8, 8);
            g2.setColor(AppColors.TEXT_SECONDARY);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2.drawString(months[i], x - 12, startY + 20);
            g2.setColor(new Color(0x2196F3));
        }
    }

    private JPanel buildInventoryHealth() {
        UIComponents.RoundedPanel card = new UIComponents.RoundedPanel(12);
        card.setBackground(AppColors.CARD_BG);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel title = new JLabel("Inventory Health Overview");
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 18));
        title.setForeground(AppColors.TEXT_PRIMARY);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(12, 0, 12, 0));

        content.add(buildHealthBar("Well-Stocked Items", 245, 300, 0x4CAF50));
        content.add(Box.createVerticalStrut(12));
        content.add(buildHealthBar("Low Stock Items", 45, 300, 0xFF9800));
        content.add(Box.createVerticalStrut(12));
        content.add(buildHealthBar("Out of Stock", 10, 300, 0xF44336));
        content.add(Box.createVerticalStrut(12));
        content.add(buildHealthBar("Overstock Items", 15, 300, 0x9C27B0));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setOpaque(false);
        header.add(title);

        card.add(header, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildHealthBar(String label, int value, int max, int colorHex) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(AppColors.TEXT_PRIMARY);
        lbl.setPreferredSize(new Dimension(150, 30));

        JPanel bar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                int barWidth = (value * getWidth() / max);
                g2.setColor(new Color(colorHex));
                g2.fillRoundRect(0, 5, barWidth, 20, 8, 8);
                g2.setColor(AppColors.BORDER);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 5, getWidth() - 1, 20, 8, 8);
                g2.setColor(AppColors.TEXT_PRIMARY);
                g2.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
                g2.drawString(value + "", barWidth / 2 - 6, 22);
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(280, 30));

        p.add(lbl, BorderLayout.WEST);
        p.add(bar, BorderLayout.CENTER);
        return p;
    }
}
