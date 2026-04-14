package erp.link_tech_erp.inventory;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class MainFrame extends JFrame {

    private JPanel contentArea;
    private CardLayout cardLayout;

    public MainFrame() {
        setTitle("LinkTech Company Dashboard - Electronics");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1280, 760);
        setMinimumSize(new Dimension(960, 600));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppColors.PAGE_BG);

        // Sidebar
        SidebarPanel sidebar = new SidebarPanel(this::onNavSelect);

        // Topbar
        JPanel topbar = buildTopbar();

        // Content area with card layout
        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(AppColors.PAGE_BG);
        contentArea.add(new InventoryPanel(), "inventory");
        contentArea.add(new SupplierPanel(), "suppliers");
        contentArea.add(new PurchaseOrderPanel(), "orders");
        contentArea.add(new AnalyticsPanel(), "analytics");

        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(AppColors.PAGE_BG);
        mainArea.add(topbar, BorderLayout.NORTH);
        mainArea.add(contentArea, BorderLayout.CENTER);

        root.add(sidebar, BorderLayout.WEST);
        root.add(mainArea, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel buildTopbar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(AppColors.TOPBAR_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppColors.BORDER),
            new EmptyBorder(0, 28, 0, 28)
        ));
        p.setPreferredSize(new Dimension(0, 66));

        // Breadcrumb
        JPanel breadcrumb = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        breadcrumb.setOpaque(false);
        JLabel home = new JLabel("Home");
        home.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        home.setForeground(AppColors.TEXT_SECONDARY);
        JLabel sep = new JLabel("/");
        sep.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        sep.setForeground(new Color(200, 200, 200));
        JLabel page = new JLabel("Inventory");
        page.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        page.setForeground(AppColors.TEXT_PRIMARY);
        breadcrumb.add(home); breadcrumb.add(sep); breadcrumb.add(page);

        p.add(breadcrumb, BorderLayout.WEST);
        return p;
    }

    private void onNavSelect(int index) {
        String[] cards = {"inventory", "suppliers", "orders", "analytics"};
        if (index < cards.length) cardLayout.show(contentArea, cards[index]);
    }
}
