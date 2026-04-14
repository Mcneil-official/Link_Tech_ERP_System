package erp.link_tech_erp.hrm;

import java.util.*;
import javax.swing.*;
import java.awt.*;

public class AddEmployeeGUI {

    public AddEmployeeGUI(){

        JFrame frame = new JFrame("Add Employee");

        // Background panel
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(30, 30, 60)); // dark blue theme

        // Title
        JLabel title = new JLabel("ADD EMPLOYEE");
        title.setBounds(90, 5, 150, 30);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 16));

        JTextField id = new JTextField();
        JTextField name = new JTextField();
        JTextField position = new JTextField();
        JTextField dept = new JTextField();
        JTextField salary = new JTextField();

        JButton save = new JButton("Save");

        // Style text fields
        JTextField[] fields = {id, name, position, dept, salary};
        for (int i = 0; i < fields.length; i++) {
            fields[i].setBounds(120, 40 + i * 30, 150, 25);
            fields[i].setFont(new Font("Segoe UI", Font.PLAIN, 12));
        }

        // Style labels
        String[] labels = {"ID", "Name", "Position", "Department", "Salary"};
        for (int i = 0; i < labels.length; i++) {
            JLabel label = new JLabel(labels[i]);
            label.setBounds(40, 40 + i * 30, 100, 25);
            label.setForeground(Color.WHITE);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            panel.add(label);
        }

        panel.add(id);
        panel.add(name);
        panel.add(position);
        panel.add(dept);
        panel.add(salary);

        save.setBounds(120, 190, 100, 30);
        save.setBackground(new Color(0, 153, 255));
        save.setForeground(Color.WHITE);
        save.setFocusPainted(false);
        save.setFont(new Font("Segoe UI", Font.BOLD, 12));
        // Add hover effect
        save.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                save.setBackground(new Color(0, 102, 204));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                save.setBackground(new Color(0, 153, 255));
            }
        });
        panel.add(save);

        frame.add(panel);

        frame.setSize(320, 280);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        save.addActionListener(e -> {
            String newId = id.getText().trim();
            String newName = name.getText().trim();
            String newPosition = position.getText().trim();
            String newDept = dept.getText().trim();
            String newSalary = salary.getText().trim();

            if (newId.isEmpty() || newName.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "ID and Name are required", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            ArrayList<String[]> data = DatabaseConnection.loadData();

            for (String[] row : data) {
                if (row.length >= 2) {
                    if (row[0].trim().equalsIgnoreCase(newId)) {
                        JOptionPane.showMessageDialog(frame, "Employee ID already exists", "Validation Error", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    if (row[1].trim().equalsIgnoreCase(newName)) {
                        JOptionPane.showMessageDialog(frame, "Employee Name already exists", "Validation Error", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
            }

            data.add(new String[]{
                    newId,
                    newName,
                    newPosition,
                    newDept,
                    newSalary
            });

            DatabaseConnection.saveData(data);

            id.setText("");
            name.setText("");
            position.setText("");
            dept.setText("");
            salary.setText("");

            JOptionPane.showMessageDialog(frame, "Employee Saved");
        });

    }
}
