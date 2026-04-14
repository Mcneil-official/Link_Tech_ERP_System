package erp.link_tech_erp.hrm;

import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;

public class SearchEmployeeGUI {

    public SearchEmployeeGUI(){
        JFrame frame = new JFrame("Search Employee");

        // Background panel
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(30, 30, 60)); // dark blue theme

        // Title
        JLabel title = new JLabel("SEARCH EMPLOYEE");
        title.setBounds(70, 5, 180, 30);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 16));

        JLabel nameLabel = new JLabel("Name:");
        JTextField nameField = new JTextField();
        JButton searchBtn = new JButton("Search");
        JTextArea result = new JTextArea();
        result.setEditable(false);
        result.setBackground(new Color(240, 240, 240));
        result.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        nameLabel.setBounds(20, 40, 80, 30);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        nameField.setBounds(100, 40, 160, 30);
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        searchBtn.setBounds(100, 80, 100, 30);
        searchBtn.setBackground(new Color(0, 153, 255));
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFocusPainted(false);
        searchBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        // Add hover effect
        searchBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                searchBtn.setBackground(new Color(0, 102, 204));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                searchBtn.setBackground(new Color(0, 153, 255));
            }
        });

        result.setBounds(20, 120, 260, 120);

        panel.add(title);
        panel.add(nameLabel);
        panel.add(nameField);
        panel.add(searchBtn);
        panel.add(result);

        frame.add(panel);

        frame.setSize(320, 280);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        searchBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            ArrayList<String[]> data = DatabaseConnection.loadData();
            StringBuilder sb = new StringBuilder();
            for(String[] emp : data){
                if(emp.length > 1 && emp[1].equalsIgnoreCase(name)){
                    sb.append("ID: ").append(emp[0]).append("\n");
                    sb.append("Name: ").append(emp[1]).append("\n");
                    sb.append("Position: ").append(emp[2]).append("\n");
                    sb.append("Department: ").append(emp[3]).append("\n");
                    sb.append("Salary: ").append(emp[4]).append("\n");
                    sb.append("-------------------\n");
                }
            }
            if(sb.length() == 0) sb.append("Employee not found.");
            result.setText(sb.toString());
        });
    }
}
