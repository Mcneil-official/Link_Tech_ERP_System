package erp.link_tech_erp.hrm;

import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;

public class UpdateEmployeeGUI {
    public UpdateEmployeeGUI(){
        JFrame frame = new JFrame("Update Employee");

        // Background panel
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(30, 30, 60)); // dark blue theme

        // Title
        JLabel title = new JLabel("UPDATE EMPLOYEE");
        title.setBounds(80, 5, 180, 30);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 16));

        JLabel idLabel = new JLabel("Employee ID:");
        JTextField idField = new JTextField();

        JLabel nameLabel = new JLabel("Name:");
        JTextField nameField = new JTextField();

        JLabel posLabel = new JLabel("Position:");
        JTextField posField = new JTextField();

        JLabel deptLabel = new JLabel("Department:");
        JTextField deptField = new JTextField();

        JLabel salaryLabel = new JLabel("Salary:");
        JTextField salaryField = new JTextField();

        JButton updateBtn = new JButton("Update");
        JTextArea result = new JTextArea();
        result.setEditable(false);
        result.setBackground(new Color(240, 240, 240));
        result.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Style labels
        JLabel[] labels = {idLabel, nameLabel, posLabel, deptLabel, salaryLabel};
        for (int i = 0; i < labels.length; i++) {
            labels[i].setBounds(20, 40 + i * 30, 100, 25);
            labels[i].setForeground(Color.WHITE);
            labels[i].setFont(new Font("Segoe UI", Font.PLAIN, 12));
        }

        // Style fields
        JTextField[] fields = {idField, nameField, posField, deptField, salaryField};
        for (int i = 0; i < fields.length; i++) {
            fields[i].setBounds(120, 40 + i * 30, 160, 25);
            fields[i].setFont(new Font("Segoe UI", Font.PLAIN, 12));
        }

        updateBtn.setBounds(120, 190, 120, 30);
        updateBtn.setBackground(new Color(0, 153, 255));
        updateBtn.setForeground(Color.WHITE);
        updateBtn.setFocusPainted(false);
        updateBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        // Add hover effect
        updateBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                updateBtn.setBackground(new Color(0, 102, 204));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                updateBtn.setBackground(new Color(0, 153, 255));
            }
        });

        result.setBounds(20, 230, 280, 100);

        panel.add(title);
        panel.add(idLabel);
        panel.add(idField);
        panel.add(nameLabel);
        panel.add(nameField);
        panel.add(posLabel);
        panel.add(posField);
        panel.add(deptLabel);
        panel.add(deptField);
        panel.add(salaryLabel);
        panel.add(salaryField);
        panel.add(updateBtn);
        panel.add(result);

        frame.add(panel);

        frame.setSize(340, 370);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        updateBtn.addActionListener(e -> {
            String id = idField.getText().trim();
            ArrayList<String[]> data = DatabaseConnection.loadData();
            boolean found = false;
            for (String[] emp : data) {
                if (emp.length > 0 && emp[0].equals(id)) {
                    emp[1] = nameField.getText().trim();
                    emp[2] = posField.getText().trim();
                    emp[3] = deptField.getText().trim();
                    emp[4] = salaryField.getText().trim();
                    found = true;
                    break;
                }
            }
            if (found) {
                DatabaseConnection.saveData(data);
                result.setText("Employee updated.");
            } else {
                result.setText("Employee ID not found.");
            }
        });
    }
}
