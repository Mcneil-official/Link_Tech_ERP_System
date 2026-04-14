package erp.link_tech_erp.finance.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import erp.link_tech_erp.finance.service.AuthenticationService;

public class LoginFrame extends JFrame {
    private static final Color BACKGROUND = new Color(245, 247, 248);
    private static final Color CARD_BACKGROUND = Color.WHITE;
    private static final Color PRIMARY = new Color(25, 55, 135); // Dark blue
    private static final Color TEXT_PRIMARY = new Color(25, 30, 40);
    private static final Color TEXT_SECONDARY = new Color(110, 120, 130);
    private static final Color INPUT_BG = new Color(243, 246, 250);
    private static final Color BORDER_SOFT = new Color(223, 231, 245);

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private final AuthenticationService authenticationService;
    private final JTextField emailField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final Runnable onLoginSuccess;

    public LoginFrame(AuthenticationService authenticationService, Runnable onLoginSuccess) {
        this.authenticationService = authenticationService;
        this.onLoginSuccess = onLoginSuccess;

        setTitle("Link Tech");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(new Dimension(500, 660));
        setResizable(false);
        setLocationRelativeTo(null);

        initializeLayout();
    }

    private void initializeLayout() {
        JPanel rootPanel = new JPanel();
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
        rootPanel.setBackground(BACKGROUND);
        rootPanel.setBorder(new EmptyBorder(40, 0, 30, 0));

        // --- Logo and Title above the card ---
        JLabel logoIcon = new JLabel("\uD83C\uDFDB");
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        logoIcon.setForeground(PRIMARY);
        logoIcon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel mainTitle = new JLabel("Link Tech");
        mainTitle.setFont(new Font("Inter", Font.BOLD, 20));
        mainTitle.setForeground(TEXT_PRIMARY);
        mainTitle.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subTitle = new JLabel("Financial Operations Platform");
        subTitle.setFont(new Font("Inter", Font.PLAIN, 12));
        subTitle.setForeground(TEXT_SECONDARY);
        subTitle.setAlignmentX(CENTER_ALIGNMENT);

        rootPanel.add(logoIcon);
        rootPanel.add(Box.createVerticalStrut(4));
        rootPanel.add(mainTitle);
        rootPanel.add(Box.createVerticalStrut(4));
        rootPanel.add(subTitle);
        rootPanel.add(Box.createVerticalStrut(30));

        // --- The White Card ---
        JPanel loginCard = new JPanel();
        loginCard.setLayout(new BoxLayout(loginCard, BoxLayout.Y_AXIS));
        loginCard.setBackground(CARD_BACKGROUND);
        loginCard.setBorder(new CompoundBorder(
            new CompoundBorder(new LineBorder(new Color(212, 222, 240), 1, true), new LineBorder(Color.WHITE, 1, true)),
                new EmptyBorder(30, 40, 30, 40)));
        loginCard.setMaximumSize(new Dimension(380, 390));
        loginCard.setAlignmentX(CENTER_ALIGNMENT);

        // Card Header
        JLabel welcomeLabel = new JLabel("Welcome Back");
        welcomeLabel.setFont(new Font("Inter", Font.BOLD, 18));
        welcomeLabel.setForeground(TEXT_PRIMARY);
        welcomeLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel promptLabel = new JLabel("Please enter your credentials to access the ledger.");
        promptLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        promptLabel.setForeground(TEXT_SECONDARY);
        promptLabel.setAlignmentX(LEFT_ALIGNMENT);
        
        loginCard.add(welcomeLabel);
        loginCard.add(Box.createVerticalStrut(4));
        loginCard.add(promptLabel);
        loginCard.add(Box.createVerticalStrut(24));

        // Form Fields
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setAlignmentX(LEFT_ALIGNMENT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 6, 0);

        JLabel emailLabel = new JLabel("INSTITUTIONAL EMAIL");
        emailLabel.setFont(new Font("Inter", Font.BOLD, 10));
        emailLabel.setForeground(TEXT_SECONDARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(emailLabel, gbc);

        styleInput(emailField);
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 16, 0);
        formPanel.add(emailField, gbc);

        gbc.insets = new Insets(0, 0, 6, 0);
        JLabel passwordLabel = new JLabel("ACCESS CODE");
        passwordLabel.setFont(new Font("Inter", Font.BOLD, 10));
        passwordLabel.setForeground(TEXT_SECONDARY);
        gbc.gridy = 2; gbc.gridwidth = 1;
        formPanel.add(passwordLabel, gbc);

        JLabel forgotLabel = new JLabel("Forgot?");
        forgotLabel.setFont(new Font("Inter", Font.PLAIN, 11));
        forgotLabel.setForeground(PRIMARY);
        forgotLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        forgotLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridx = 1; 
        formPanel.add(forgotLabel, gbc);

        styleInput(passwordField);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.insets = new Insets(0, 0, 16, 0);
        formPanel.add(passwordField, gbc);

        final char defaultEchoChar = passwordField.getEchoChar();
        JCheckBox showPasswordBox = new JCheckBox("Show access code");
        showPasswordBox.setFont(new Font("Inter", Font.PLAIN, 11));
        showPasswordBox.setForeground(TEXT_SECONDARY);
        showPasswordBox.setOpaque(false);
        showPasswordBox.setFocusPainted(false);
        showPasswordBox.addActionListener(event -> {
            if (showPasswordBox.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar(defaultEchoChar);
            }
        });
        gbc.gridy = 4; gbc.insets = new Insets(0, 0, 12, 0);
        formPanel.add(showPasswordBox, gbc);

        loginCard.add(formPanel);

        // Checkbox
        JCheckBox stayAuthBox = new JCheckBox("Stay authenticated for 24 hours");
        stayAuthBox.setFont(new Font("Inter", Font.PLAIN, 12));
        stayAuthBox.setForeground(TEXT_SECONDARY);
        stayAuthBox.setOpaque(false);
        stayAuthBox.setFocusPainted(false);
        stayAuthBox.setAlignmentX(LEFT_ALIGNMENT);
        loginCard.add(stayAuthBox);
        loginCard.add(Box.createVerticalStrut(20));

        // Login Button
        JButton loginButton = new JButton("Sign In \u2192");
        loginButton.setBackground(PRIMARY);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setFont(new Font("Inter", Font.BOLD, 13));
        loginButton.setBorder(new EmptyBorder(12, 16, 12, 16));
        loginButton.setAlignmentX(LEFT_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        loginButton.putClientProperty("JComponent.roundRect", true);
        loginButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                loginButton.setBackground(new Color(20, 47, 119));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                loginButton.setBackground(PRIMARY);
            }
        });
        loginButton.addActionListener(event -> handleLogin());
        loginCard.add(loginButton);

        rootPanel.add(loginCard);

        // --- Footer ---
        rootPanel.add(Box.createVerticalGlue());
        
        JLabel footerCopy = new JLabel("© 2024 Link Tech. All rights reserved.");
        footerCopy.setFont(new Font("Inter", Font.PLAIN, 11));
        footerCopy.setForeground(new Color(150, 160, 170));
        footerCopy.setAlignmentX(CENTER_ALIGNMENT);
        
        JPanel linksPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        linksPanel.setOpaque(false);
        linksPanel.add(createFooterLink("Privacy Policy"));
        linksPanel.add(createFooterLink("Security Audit"));
        linksPanel.add(createFooterLink("Support"));

        rootPanel.add(footerCopy);
        rootPanel.add(Box.createVerticalStrut(4));
        rootPanel.add(linksPanel);

        getRootPane().setDefaultButton(loginButton);
        setContentPane(rootPanel);
    }

    private void styleInput(JTextField field) {
        field.setPreferredSize(new Dimension(300, 36));
        field.setBackground(INPUT_BG);
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_SOFT, 1, true),
            new EmptyBorder(4, 10, 4, 10)
        ));
        // FlatLaf rounded corners hint
        field.putClientProperty("JComponent.roundRect", true);
    }

    private JLabel createFooterLink(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Inter", Font.PLAIN, 11));
        label.setForeground(new Color(170, 180, 190));
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return label;
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isBlank() || password.isBlank()) {
            showWarning("Email and access code are required.");
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showWarning("Please enter a valid institutional email address.");
            return;
        }

        if (!authenticationService.authenticate(email, password)) {
            showWarning("Invalid email or access code.");
            return;
        }

        dispose();
        onLoginSuccess.run();
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Login Failed", JOptionPane.WARNING_MESSAGE);
    }

    public void showWindow() {
        setVisible(true);
    }
}