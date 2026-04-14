package erp.link_tech_erp.finance.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import erp.link_tech_erp.finance.model.FinancialRecord;
import erp.link_tech_erp.finance.model.RecordType;

public class RecordDialog extends JDialog {
    private static final Color CARD_BACKGROUND = Color.WHITE;
    private static final Color PRIMARY = new Color(25, 55, 135);
    private static final Color TEXT_PRIMARY = new Color(25, 30, 40);
    private static final Color INPUT_BG = new Color(243, 246, 250);
    private static final Color BORDER_SOFT = new Color(223, 231, 245);

    private final JTextField dateField = new JTextField();
    private final JComboBox<RecordType> typeComboBox = new JComboBox<>(RecordType.values());
    private final JTextField categoryField = new JTextField();
    private final JTextField descriptionField = new JTextField();
    private final JTextField amountField = new JTextField();

    private boolean saved = false;

    public RecordDialog(JFrame parent, String title, FinancialRecord record) {
        super(parent, title, true);
        setSize(480, 520);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(CARD_BACKGROUND);
        
        initializeUI(record);
    }

    private void initializeUI(FinancialRecord record) {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(CARD_BACKGROUND);
        rootPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(new LineBorder(new Color(212, 222, 240), 1, true), new LineBorder(Color.WHITE, 1, true)),
            new EmptyBorder(24, 32, 24, 32)
        ));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel(getTitle());
        titleLabel.setFont(new Font("Inter", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_PRIMARY);
        JLabel subtitleLabel = new JLabel("Enter fiscal details to append to the master ledger.");
        subtitleLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(110, 120, 130));
        
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.CENTER);
        headerPanel.setBorder(new EmptyBorder(0, 0, 24, 0));
        rootPanel.add(headerPanel, BorderLayout.NORTH);

        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 6, 0);

        // Row 1: Date & Type
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.5; gbc.insets = new Insets(0, 0, 4, 12);
        formPanel.add(createLabel("RECORD DATE"), gbc);
        gbc.gridx = 1; gbc.insets = new Insets(0, 0, 4, 0);
        formPanel.add(createLabel("TYPE"), gbc);

        gbc.gridy = 1; gbc.gridx = 0; gbc.insets = new Insets(0, 0, 16, 12);
        styleInput(dateField);
        formPanel.add(dateField, gbc);
        gbc.gridx = 1; gbc.insets = new Insets(0, 0, 16, 0);
        typeComboBox.setBackground(INPUT_BG);
        typeComboBox.setPreferredSize(new Dimension(0, 36));
        typeComboBox.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER_SOFT, 1, true), new EmptyBorder(2, 6, 2, 6)));
        formPanel.add(typeComboBox, gbc);

        // Row 2: Category
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.insets = new Insets(0, 0, 4, 0);
        formPanel.add(createLabel("CATEGORY"), gbc);
        gbc.gridy = 3; gbc.insets = new Insets(0, 0, 16, 0);
        styleInput(categoryField);
        formPanel.add(categoryField, gbc);

        // Row 3: Description
        gbc.gridy = 4; gbc.insets = new Insets(0, 0, 4, 0);
        formPanel.add(createLabel("DESCRIPTION"), gbc);
        gbc.gridy = 5; gbc.insets = new Insets(0, 0, 16, 0);
        styleInput(descriptionField);
        formPanel.add(descriptionField, gbc);

        // Row 4: Amount
        gbc.gridy = 6; gbc.insets = new Insets(0, 0, 4, 0);
        formPanel.add(createLabel("AMOUNT (USD)"), gbc);
        gbc.gridy = 7; gbc.gridwidth = 1; gbc.insets = new Insets(0, 0, 24, 12);
        styleInput(amountField);
        formPanel.add(amountField, gbc);

        rootPanel.add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttonPanel.setOpaque(false);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setContentAreaFilled(false);
        cancelButton.setBorderPainted(false);
        cancelButton.setFont(new Font("Inter", Font.PLAIN, 13));
        cancelButton.addActionListener(e -> dispose());
        
        JButton saveButton = new JButton("Save Record");
        saveButton.setBackground(PRIMARY);
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("Inter", Font.BOLD, 13));
        saveButton.setBorder(new EmptyBorder(10, 20, 10, 20));
        saveButton.setFocusPainted(false);
        saveButton.putClientProperty("JComponent.roundRect", true);
        saveButton.addActionListener(e -> handleSave());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        rootPanel.add(buttonPanel, BorderLayout.SOUTH);

        getContentPane().add(rootPanel);

        if (record != null) {
            dateField.setText(record.getDate() != null ? record.getDate().toString() : "");
            typeComboBox.setSelectedItem(record.getType());
            categoryField.setText(record.getCategory());
            descriptionField.setText(record.getDescription());
            amountField.setText(String.format("%.2f", record.getAmount()));
        } else {
            dateField.setText(LocalDate.now().toString());
            typeComboBox.setSelectedItem(RecordType.EXPENSE);
        }
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Inter", Font.BOLD, 10));
        lbl.setForeground(new Color(110, 120, 130));
        return lbl;
    }

    private void styleInput(JTextField field) {
        field.setPreferredSize(new Dimension(0, 36));
        field.setBackground(INPUT_BG);
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_SOFT, 1, true),
            new EmptyBorder(4, 10, 4, 10)
        ));
    }

    private void handleSave() {
        if (dateField.getText().isBlank() || amountField.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Date and Amount are required.", "Wait", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            LocalDate.parse(dateField.getText().trim());
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid date. Use yyyy-MM-dd.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double amt = Double.parseDouble(amountField.getText().trim());
            if (amt <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Amount must be a positive number.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        saved = true;
        dispose();
    }

    public boolean isSaved() {
        return saved;
    }

    public void applyToRecord(FinancialRecord record) {
        record.setDate(LocalDate.parse(dateField.getText().trim()));
        record.setType((RecordType) typeComboBox.getSelectedItem());
        record.setCategory(categoryField.getText().trim());
        record.setDescription(descriptionField.getText().trim());
        record.setAmount(Double.parseDouble(amountField.getText().trim()));
    }
}
