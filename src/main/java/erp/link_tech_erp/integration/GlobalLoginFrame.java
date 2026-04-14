package erp.link_tech_erp.integration;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.formdev.flatlaf.FlatLightLaf;

import erp.link_tech_erp.integration.auth.GlobalAuthenticationService;
import erp.link_tech_erp.integration.auth.GlobalSession;
import erp.link_tech_erp.integration.auth.GlobalSessionContext;

public class GlobalLoginFrame extends JFrame {
    private static final Color BACKGROUND = new Color(244, 247, 252);
    private static final Color CARD_BACKGROUND = Color.WHITE;
    private static final Color PRIMARY = new Color(25, 55, 135);
    private static final Color PRIMARY_HOVER = new Color(20, 47, 119);
    private static final Color TEXT_PRIMARY = new Color(35, 43, 67);
    private static final Color TEXT_SECONDARY = new Color(100, 113, 145);
    private static final Color BORDER_SOFT = new Color(223, 231, 245);

    private final GlobalAuthenticationService authenticationService;
    private final JTextField identifierField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();

    public GlobalLoginFrame() {
        this(new GlobalAuthenticationService());
    }

    public GlobalLoginFrame(GlobalAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;

        setTitle("LinkTech ERP - Sign In");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(860, 560));
        setMinimumSize(new Dimension(820, 540));
        setLocationRelativeTo(null);

        setContentPane(buildContent());
    }

    private JPanel buildContent() {
        JPanel root = new GradientPanel();
        root.setLayout(new GridBagLayout());
        root.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel shell = new JPanel(new BorderLayout(0, 0));
        shell.setBackground(Color.WHITE);
        shell.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_SOFT),
            new EmptyBorder(0, 0, 0, 0)
        ));
        shell.setPreferredSize(new Dimension(760, 460));

        JPanel left = new JPanel();
        left.setBackground(new Color(18, 39, 95));
        left.setPreferredSize(new Dimension(300, 460));
        left.setBorder(new EmptyBorder(34, 28, 34, 28));
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel badge = new JLabel("SECURE LOGIN");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(new Color(184, 202, 255));
        badge.setAlignmentX(LEFT_ALIGNMENT);

        JLabel title = new JLabel("LinkTech ERP");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Sign in to access your assigned module account.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitle.setForeground(new Color(222, 230, 250));
        subtitle.setAlignmentX(LEFT_ALIGNMENT);

        left.add(badge);
        left.add(Box.createVerticalStrut(12));
        left.add(title);
        left.add(Box.createVerticalStrut(10));
        left.add(subtitle);
        left.add(Box.createVerticalStrut(28));

        left.add(Box.createVerticalGlue());

        JLabel footer = new JLabel("Single-module account access");
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footer.setForeground(new Color(192, 205, 236));
        footer.setAlignmentX(LEFT_ALIGNMENT);
        left.add(footer);

        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(CARD_BACKGROUND);
        right.setBorder(new EmptyBorder(24, 26, 24, 26));

        JPanel form = new JPanel();
        form.setBackground(CARD_BACKGROUND);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(360, 360));

        JLabel formTitle = new JLabel("Sign in");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        formTitle.setForeground(TEXT_PRIMARY);
        formTitle.setAlignmentX(LEFT_ALIGNMENT);

        JLabel formHint = new JLabel("Sign in to your account.");
        formHint.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formHint.setForeground(TEXT_SECONDARY);
        formHint.setAlignmentX(LEFT_ALIGNMENT);

        JLabel identifierLabel = new JLabel("ACCOUNT");
        identifierLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        identifierLabel.setForeground(TEXT_SECONDARY);
        identifierLabel.setAlignmentX(LEFT_ALIGNMENT);

        styleField(identifierField);
        identifierField.setToolTipText("Email or username");
        identifierField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        identifierField.setAlignmentX(LEFT_ALIGNMENT);

        JLabel passwordLabel = new JLabel("PASSWORD");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        passwordLabel.setForeground(TEXT_SECONDARY);
        passwordLabel.setAlignmentX(LEFT_ALIGNMENT);

        styleField(passwordField);
        passwordField.setToolTipText("Password");
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        passwordField.setAlignmentX(LEFT_ALIGNMENT);

        JButton signIn = new JButton("Sign In");
        signIn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        signIn.setForeground(Color.WHITE);
        signIn.setBackground(PRIMARY);
        signIn.setFocusPainted(false);
        signIn.setBorder(BorderFactory.createEmptyBorder(11, 14, 11, 14));
        signIn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signIn.setAlignmentX(LEFT_ALIGNMENT);
        signIn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                signIn.setBackground(PRIMARY_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent event) {
                signIn.setBackground(PRIMARY);
            }
        });
        signIn.addActionListener(event -> handleLogin());

        form.add(formTitle);
        form.add(Box.createVerticalStrut(6));
        form.add(formHint);
        form.add(Box.createVerticalStrut(24));
        form.add(identifierLabel);
        form.add(Box.createVerticalStrut(6));
        form.add(identifierField);
        form.add(Box.createVerticalStrut(16));
        form.add(passwordLabel);
        form.add(Box.createVerticalStrut(6));
        form.add(passwordField);
        form.add(Box.createVerticalStrut(22));
        form.add(signIn);

        right.add(form);

        shell.add(left, BorderLayout.WEST);
        shell.add(right, BorderLayout.CENTER);

        root.add(shell);
        return root;
    }

    private void styleField(JComponent field) {
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_SOFT, 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));
        field.putClientProperty("JComponent.roundRect", true);
        if (field instanceof JTextField textField) {
            textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        } else if (field instanceof JPasswordField passwordInput) {
            passwordInput.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        }
    }

    private void handleLogin() {
        String identifier = identifierField.getText().trim();
        String password = new String(passwordField.getPassword());

        GlobalSession session = authenticationService.authenticate(identifier, password);
        if (session == null) {
            JOptionPane.showMessageDialog(
                this,
                "Invalid credentials.",
                "Sign in failed",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        GlobalSessionContext.set(session);
        dispose();
        ErpShellFrame.launchShell();
    }

    public static void launch() {
        installLookAndFeel();
        SwingUtilities.invokeLater(() -> new GlobalLoginFrame().setVisible(true));
    }

    private static void installLookAndFeel() {
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

    private static final class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, BACKGROUND, getWidth(), getHeight(), new Color(235, 240, 248)));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }
}