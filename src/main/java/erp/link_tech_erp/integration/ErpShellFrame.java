package erp.link_tech_erp.integration;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.formdev.flatlaf.FlatLightLaf;

import erp.link_tech_erp.integration.auth.GlobalAuthorizationService;
import erp.link_tech_erp.integration.auth.GlobalSession;
import erp.link_tech_erp.integration.auth.GlobalSessionContext;
import erp.link_tech_erp.integration.auth.ModuleAccess;

public class ErpShellFrame extends JFrame {

    // Finance-inspired shell palette.
    private static final Color BACKGROUND = new Color(245, 247, 252);
    private static final Color SURFACE_TINT = new Color(248, 251, 255);
    private static final Color CARD_BACKGROUND = Color.WHITE;
    private static final Color PRIMARY = new Color(25, 55, 135);
    private static final Color PRIMARY_HOVER = new Color(20, 47, 119);
    private static final Color PRIMARY_PRESSED = new Color(16, 39, 101);
    private static final Color TEXT_PRIMARY = new Color(35, 43, 67);
    private static final Color TEXT_SECONDARY = new Color(100, 113, 145);
    private static final Color BORDER_SOFT = new Color(224, 231, 244);
    private static final Color HEADER_ACCENT = new Color(34, 62, 134);
    private final GlobalAuthorizationService authorizationService = new GlobalAuthorizationService();

    public ErpShellFrame() {
        setTitle("LinkTech ERP - Integrated Shell");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 620);
        setMinimumSize(new Dimension(900, 560));
        setLocationRelativeTo(null);
        setContentPane(buildContent());
    }

    private JPanel buildContent() {
        GlobalSession session = GlobalSessionContext.get();
        if (session == null) {
            SwingUtilities.invokeLater(GlobalLoginFrame::launch);
            return new JPanel();
        }

        JPanel root = new GradientPanel();
        root.setLayout(new BorderLayout(0, 14));
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        root.setBackground(BACKGROUND);

        JLabel title = new JLabel(session.activeModule().getDisplayName() + " Workspace", SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_PRIMARY);

        JLabel subtitle = new JLabel(
            "Signed in as " + session.displayName() + " • " + session.activeModule().getDisplayName()
                + " access only.",
            SwingConstants.LEFT);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_SECONDARY);

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setOpaque(false);
        header.add(title);
        header.add(subtitle);

        JPanel headerCard = new JPanel(new BorderLayout());
        headerCard.setBackground(CARD_BACKGROUND);
        headerCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_SOFT),
            new EmptyBorder(14, 16, 14, 16)
        ));

        JPanel accentBar = new JPanel();
        accentBar.setPreferredSize(new Dimension(0, 4));
        accentBar.setBackground(HEADER_ACCENT);

        JPanel helperChip = new JPanel();
        helperChip.setBackground(new Color(233, 237, 247));
        helperChip.setBorder(new EmptyBorder(5, 9, 5, 9));
        JLabel helperLabel = new JLabel(session.activeModule().getDisplayName() + " account");
        helperLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        helperLabel.setForeground(PRIMARY);
        helperChip.add(helperLabel);

        JButton logoutButton = new JButton("Sign out");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        logoutButton.setForeground(PRIMARY);
        logoutButton.setBackground(new Color(233, 237, 247));
        logoutButton.setBorder(BorderFactory.createEmptyBorder(7, 11, 7, 11));
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        addPrimaryButtonStates(logoutButton);
        logoutButton.addActionListener(event -> performLogout());

        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerActions.setOpaque(false);
        headerActions.add(helperChip);
        headerActions.add(logoutButton);

        JPanel headerBody = new JPanel(new BorderLayout());
        headerBody.setOpaque(false);
        headerBody.add(header, BorderLayout.CENTER);
        headerBody.add(headerActions, BorderLayout.EAST);

        headerCard.add(accentBar, BorderLayout.NORTH);
        headerCard.add(headerBody, BorderLayout.CENTER);

        List<ErpModule> modules = List.of(
            new ErpModule() {
                @Override
                public String getName() {
                    return "Finance";
                }

                @Override
                public ModuleAccess getAccess() {
                    return ModuleAccess.FINANCE;
                }

                @Override
                public String getDescription() {
                    return "Launches the finance workspace.";
                }

                @Override
                public List<String> getConfigurationIssues() {
                    return ModuleConfigPreflight.financeIssues();
                }

                @Override
                public void launch() {
                    FinanceModuleLauncher.launch();
                }
            },
            new ErpModule() {
                @Override
                public String getName() {
                    return "Inventory";
                }

                @Override
                public ModuleAccess getAccess() {
                    return ModuleAccess.INVENTORY;
                }

                @Override
                public String getDescription() {
                    return "Launches the inventory workspace.";
                }

                @Override
                public List<String> getConfigurationIssues() {
                    return ModuleConfigPreflight.inventoryIssues();
                }

                @Override
                public void launch() {
                    erp.link_tech_erp.integration.InventoryModuleLauncher.launch();
                }
            },
            new ErpModule() {
                @Override
                public String getName() {
                    return "HRM";
                }

                @Override
                public ModuleAccess getAccess() {
                    return ModuleAccess.HRM;
                }

                @Override
                public String getDescription() {
                    return "Launches the HRM workspace.";
                }

                @Override
                public List<String> getConfigurationIssues() {
                    return ModuleConfigPreflight.hrmIssues();
                }

                @Override
                public void launch() {
                    erp.link_tech_erp.integration.HrmModuleLauncher.launch();
                }
            },
            new ErpModule() {
                @Override
                public String getName() {
                    return "Sales";
                }

                @Override
                public ModuleAccess getAccess() {
                    return ModuleAccess.SALES;
                }

                @Override
                public String getDescription() {
                    return "Launches the sales workspace.";
                }

                @Override
                public List<String> getConfigurationIssues() {
                    return ModuleConfigPreflight.salesIssues();
                }

                @Override
                public void launch() {
                    erp.link_tech_erp.integration.SalesModuleLauncher.launch();
                }
            }
        );

        ErpModule activeModule = null;
        for (ErpModule module : modules) {
            if (module.getAccess() == session.activeModule()) {
                activeModule = module;
                break;
            }
        }

        if (activeModule == null) {
            JLabel emptyState = new JLabel("Your account is not linked to a launchable module.", SwingConstants.CENTER);
            emptyState.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            emptyState.setForeground(TEXT_SECONDARY);
            root.add(emptyState, BorderLayout.CENTER);
            return root;
        }

        ErpModule moduleToLaunch = activeModule;
        JLabel openingState = new JLabel(
            "Opening " + moduleToLaunch.getName() + " workspace...",
            SwingConstants.CENTER
        );
        openingState.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        openingState.setForeground(TEXT_SECONDARY);
        root.add(openingState, BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> {
            try {
                List<String> configIssues = moduleToLaunch.getConfigurationIssues();
                if (!configIssues.isEmpty()) {
                    String details = String.join("\n- ", configIssues);
                    throw new IllegalStateException(
                        "Module cannot start because required configuration is missing:\n\n- " + details
                            + "\n\nSet these in environment variables, .env.local, or .env in the project root."
                    );
                }
                authorizationService.requireAccess(moduleToLaunch.getAccess());
                dispose();
                moduleToLaunch.launch();
            } catch (RuntimeException exception) {
                JOptionPane.showMessageDialog(
                    this,
                    exception.getMessage(),
                    moduleToLaunch.getName() + " Launch Error",
                    JOptionPane.ERROR_MESSAGE
                );
                GlobalLoginFrame.launch();
                dispose();
            }
        });

        root.add(headerCard, BorderLayout.NORTH);
        return root;
    }

    private void addPrimaryButtonStates(JButton button) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                button.setBackground(PRIMARY_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent event) {
                button.setBackground(PRIMARY);
            }

            @Override
            public void mousePressed(MouseEvent event) {
                button.setBackground(PRIMARY_PRESSED);
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                button.setBackground(button.contains(event.getPoint()) ? PRIMARY_HOVER : PRIMARY);
            }
        });
    }

    private static final class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint paint = new GradientPaint(
                0,
                0,
                SURFACE_TINT,
                getWidth(),
                getHeight(),
                BACKGROUND
            );
            g2.setPaint(paint);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    public static void installLookAndFeel() {
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.width", 8);
            UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14));
        } catch (Exception exception) {
            System.err.println("Look and feel setup failed: " + exception.getMessage());
        }
    }

    public static void launchShell() {
        installLookAndFeel();
        if (!GlobalSessionContext.isAuthenticated()) {
            GlobalLoginFrame.launch();
            return;
        }
        SwingUtilities.invokeLater(() -> {
            ErpShellFrame frame = new ErpShellFrame();
            frame.setVisible(true);
        });
    }

    private void performLogout() {
        GlobalSessionContext.clear();
        dispose();
        GlobalLoginFrame.launch();
    }
}
