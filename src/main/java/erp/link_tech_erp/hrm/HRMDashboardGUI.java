package erp.link_tech_erp.hrm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import erp.link_tech_erp.integration.GlobalLoginFrame;
import erp.link_tech_erp.integration.auth.GlobalSessionContext;

public class HRMDashboardGUI {
    private JFrame frame;
    private JTextArea textArea;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HRMDashboardGUI().createAndShowGUI());
    }

    private void createAndShowGUI() {
        frame = new JFrame("HRM Dashboard GUI");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(700, 500);
        frame.getContentPane().setBackground(new Color(30, 30, 60)); // dark blue theme
        frame.setLayout(new BorderLayout(10, 10));

        // Title
        JLabel title = new JLabel("HRM DASHBOARD", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setBackground(new Color(240, 240, 240));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel buttons = new JPanel(new GridLayout(1, 6, 5, 5));
        buttons.setBackground(new Color(30, 30, 60));
        JButton btnAdd = new JButton("Add");
        JButton btnView = new JButton("View");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnSearch = new JButton("Search");
        JButton btnExit = new JButton("Sign Out");

        JButton[] btns = {btnAdd, btnView, btnUpdate, btnDelete, btnSearch, btnExit};
        for (JButton btn : btns) {
            btn.setBackground(new Color(0, 153, 255));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            // Add hover effect
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    ((JButton)evt.getSource()).setBackground(new Color(0, 102, 204));
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    ((JButton)evt.getSource()).setBackground(new Color(0, 153, 255));
                }
            });
        }
        btnDelete.setBackground(new Color(255, 51, 51));
        btnDelete.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnDelete.setBackground(new Color(204, 0, 0));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnDelete.setBackground(new Color(255, 51, 51));
            }
        });

        buttons.add(btnAdd);
        buttons.add(btnView);
        buttons.add(btnUpdate);
        buttons.add(btnDelete);
        buttons.add(btnSearch);
        buttons.add(btnExit);

        frame.add(title, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(buttons, BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> addEmployee());
        btnView.addActionListener(e -> viewEmployees());
        btnUpdate.addActionListener(e -> updateEmployee());
        btnDelete.addActionListener(e -> deleteEmployee());
        btnSearch.addActionListener(e -> searchEmployee());
        btnExit.addActionListener(e -> {
            int confirmed = JOptionPane.showConfirmDialog(
                frame,
                "Do you want to sign out now?",
                "Sign Out",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            if (confirmed != JOptionPane.YES_OPTION) {
                return;
            }

            GlobalSessionContext.clear();
            frame.dispose();
            GlobalLoginFrame.launch();
        });

        viewEmployees();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void addEmployee() {
        String id = prompt("Enter Employee ID:");
        if(id == null || id.isBlank()) return;
        String name = prompt("Enter Name:");
        if(name == null || name.isBlank()) return;
        String position = prompt("Enter Position:");
        if(position == null || position.isBlank()) return;
        String department = prompt("Enter Department:");
        if(department == null || department.isBlank()) return;
        String salary = prompt("Enter Salary:");
        if(salary == null || salary.isBlank()) return;

        ArrayList<String[]> employees = DatabaseConnection.loadData();
        employees.add(new String[]{id, name, position, department, salary});
        DatabaseConnection.saveData(employees);
        showMessage("Employee added successfully.");
        viewEmployees();
    }

    private void viewEmployees() {
        ArrayList<String[]> employees = DatabaseConnection.loadData();
        StringBuilder sb = new StringBuilder();
        sb.append("ID\tName\tPosition\tDepartment\tSalary\n");
        sb.append("-----------------------------------------------------\n");
        if (employees.isEmpty()) {
            sb.append("No records found. Click Add to create an employee.");
        } else {
            for (String[] emp : employees) {
                if (emp.length < 5) continue;
                sb.append(emp[0]).append("\t").append(emp[1]).append("\t").append(emp[2]).append("\t").append(emp[3]).append("\t").append(emp[4]).append("\n");
            }
        }
        textArea.setText(sb.toString());
    }

    private void updateEmployee() {
        String id = prompt("Enter Employee ID to update:");
        if(id == null || id.isBlank()) return;

        ArrayList<String[]> employees = DatabaseConnection.loadData();
        for (String[] emp : employees) {
            if (emp[0].equals(id)) {
                String name = prompt("New Name:", emp[1]);
                String position = prompt("New Position:", emp[2]);
                String department = prompt("New Department:", emp[3]);
                String salary = prompt("New Salary:", emp[4]);
                if(name == null || position == null || department == null || salary == null) return;
                emp[1] = name;
                emp[2] = position;
                emp[3] = department;
                emp[4] = salary;
                DatabaseConnection.saveData(employees);
                showMessage("Employee updated.");
                viewEmployees();
                return;
            }
        }
        showMessage("Employee not found.");
    }

    private void deleteEmployee() {
        String id = prompt("Enter Employee ID to delete:");
        if(id == null || id.isBlank()) return;

        ArrayList<String[]> employees = DatabaseConnection.loadData();
        boolean found = false;
        for (int i = 0; i < employees.size(); i++) {
            if (employees.get(i)[0].equals(id)) {
                employees.remove(i);
                found = true;
                break;
            }
        }
        if (found) {
            DatabaseConnection.saveData(employees);
            showMessage("Employee deleted.");
            viewEmployees();
        } else {
            showMessage("Employee not found.");
        }
    }

    private void searchEmployee() {
        String name = prompt("Enter employee name to search:");
        if(name == null || name.isBlank()) return;

        ArrayList<String[]> employees = DatabaseConnection.loadData();
        StringBuilder sb = new StringBuilder();
        for (String[] emp : employees) {
            if (emp.length < 2) continue;
            if (emp[1].equalsIgnoreCase(name.trim())) {
                sb.append("ID: ").append(emp[0]).append("\n");
                sb.append("Name: ").append(emp[1]).append("\n");
                sb.append("Position: ").append(emp[2]).append("\n");
                sb.append("Department: ").append(emp[3]).append("\n");
                sb.append("Salary: ").append(emp[4]).append("\n");
                sb.append("---------------------\n");
            }
        }
        if (sb.length() == 0) {
            showMessage("No employee found with name: " + name);
        } else {
            textArea.setText(sb.toString());
        }
    }

    private String prompt(String message) {
        return JOptionPane.showInputDialog(frame, message);
    }

    private String prompt(String message, String defaultVal) {
        return (String) JOptionPane.showInputDialog(frame, message, "Update Employee", JOptionPane.PLAIN_MESSAGE, null, null, defaultVal);
    }

    private void showMessage(String msg) {
        JOptionPane.showMessageDialog(frame, msg);
    }
}
