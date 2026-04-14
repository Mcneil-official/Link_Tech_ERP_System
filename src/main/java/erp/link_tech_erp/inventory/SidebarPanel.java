package erp.link_tech_erp.inventory;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class SidebarPanel extends JPanel {

    private final String[] menuItems  = {"Inventory", "Supplier Management", "Purchase Orders", "Analytics"};
    private final String[] menuIcons  = {"grid", "user", "bag", "chart"};
    private int activeIndex = 0;
    private Consumer<Integer> onSelect;

    public SidebarPanel(Consumer<Integer> onSelect) {
        this.onSelect = onSelect;
        setPreferredSize(new Dimension(300, 0));
        setBackground(AppColors.SIDEBAR_BG);
        setLayout(new BorderLayout());

        add(buildLogo(), BorderLayout.NORTH);
        add(buildNav(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    private JPanel buildLogo() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(24, 20, 20, 20));

        JLabel title = new JLabel("LinkTech ERP");
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("PROCUREMENT & INVENTORY");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(AppColors.SIDEBAR_TEXT_MUTED);
        sub.setBorder(new EmptyBorder(3, 0, 0, 0));

        JPanel branding = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        branding.setOpaque(false);
        branding.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel textWrap = new JPanel();
        textWrap.setOpaque(false);
        textWrap.setLayout(new BoxLayout(textWrap, BoxLayout.Y_AXIS));
        textWrap.add(title);
        textWrap.add(sub);

        branding.add(textWrap);

        p.add(branding);

        // Divider
        JPanel divider = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(AppColors.SIDEBAR_DIVIDER);
                g.fillRect(0, getHeight()-1, getWidth(), 1);
            }
        };
        divider.setOpaque(false);
        divider.setPreferredSize(new Dimension(0, 1));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(p, BorderLayout.CENTER);
        wrapper.add(divider, BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel buildNav() {
        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(16, 0, 0, 0));

        addSectionLabel(nav, "Modules");
        addNavItem(nav, 0, menuItems[0], menuIcons[0]);
        addNavItem(nav, 1, menuItems[1], menuIcons[1]);
        addNavItem(nav, 2, menuItems[2], menuIcons[2]);
        nav.add(Box.createVerticalStrut(12));
        addSectionLabel(nav, "Reports");
        addNavItem(nav, 3, menuItems[3], menuIcons[3]);

        JScrollPane sp = new JScrollPane(nav);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(null);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(sp);
        return wrapper;
    }

    private void addSectionLabel(JPanel parent, String text) {
        JLabel lbl = new JLabel(text.toUpperCase());
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(new Color(255, 255, 255, 64));
        lbl.setBorder(new EmptyBorder(8, 20, 6, 20));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(lbl);
    }

    private void addNavItem(JPanel parent, int index, String text, String icon) {
        NavItem item = new NavItem(index, text, icon);
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        parent.add(item);
    }

    private JPanel buildFooter() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JPanel divider = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(AppColors.SIDEBAR_DIVIDER);
                g.fillRect(0, 0, getWidth(), 1);
            }
        };
        divider.setOpaque(false);
        divider.setPreferredSize(new Dimension(0, 1));

        JPanel content = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 14));
        content.setOpaque(false);

        // Avatar circle
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(77, 144, 254), getWidth(), getHeight(), new Color(167, 139, 250));
                g2.setPaint(gp);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                String t = "A";
                g2.drawString(t, (getWidth()-fm.stringWidth(t))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(36, 36));

        JPanel userInfo = new JPanel();
        userInfo.setOpaque(false);
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
        JLabel name = new JLabel("Admin");
        name.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        name.setForeground(new Color(255, 255, 255, 204));
        JLabel role = new JLabel("Administrator");
        role.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        role.setForeground(new Color(255, 255, 255, 90));
        userInfo.add(name);
        userInfo.add(role);

        MouseAdapter userMenuListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showUserMenu((Component) e.getSource(), e.getX(), e.getY());
            }
        };

        avatar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        userInfo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        name.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        role.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        avatar.addMouseListener(userMenuListener);
        userInfo.addMouseListener(userMenuListener);
        name.addMouseListener(userMenuListener);
        role.addMouseListener(userMenuListener);

        content.add(avatar);
        content.add(userInfo);

        p.add(divider, BorderLayout.NORTH);
        p.add(content, BorderLayout.CENTER);
        return p;
    }

    private void showUserMenu(Component source, int x, int y) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem logoutItem = new JMenuItem("Log out");
        logoutItem.addActionListener(e -> performLogout());
        menu.add(logoutItem);
        menu.show(source, x, y);
    }

    private void performLogout() {
        Window window = SwingUtilities.getWindowAncestor(this);
        int result = JOptionPane.showConfirmDialog(
            window,
            "Do you want to log out now?",
            "Log out",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        if (window != null) {
            window.dispose();
        }

        erp.link_tech_erp.integration.GlobalLoginFrame.launch();
    }

    // ── Inner NavItem ──────────────────────────────────────────────────────────
    private class NavItem extends JPanel {
        private final int index;
        private boolean hovered = false;

        NavItem(int index, String text, String iconType) {
            this.index = index;
            setOpaque(false);
            setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
            setPreferredSize(new Dimension(300, 52));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                @Override
                public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                @Override
                public void mouseClicked(MouseEvent e) {
                    activeIndex = index;
                    repaint();
                    if (onSelect != null) onSelect.accept(index);
                    // Repaint siblings
                    if (getParent() != null) getParent().repaint();
                }
            });

            setLayout(new BorderLayout());
            JLabel lbl = new JLabel("  " + text);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 17));
            lbl.setForeground(AppColors.SIDEBAR_TEXT);
            lbl.setBorder(new EmptyBorder(0, 44, 0, 0));
            add(lbl, BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            boolean isActive = (activeIndex == index);
            if (isActive) {
                g2.setColor(AppColors.SIDEBAR_ACTIVE_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(AppColors.SIDEBAR_ACTIVE_BORDER);
                g2.fillRect(0, 0, 3, getHeight());
            } else if (hovered) {
                g2.setColor(new Color(255, 255, 255, 13));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
