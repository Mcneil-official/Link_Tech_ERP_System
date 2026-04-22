package erp.link_tech_erp.hrm;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class DeleteEmployeeGUI {

    public DeleteEmployeeGUI(){
        JFrame frame = new JFrame("Delete Employee");

        // Background panel
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(30, 30, 60)); // dark blue theme

        // Title
        JLabel title = new JLabel("DELETE EMPLOYEE");
        title.setBounds(80, 5, 180, 30);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 16));

        JLabel idLabel = new JLabel("Employee ID:");
        JTextField idField = new JTextField();
        JButton delete = new JButton("Delete");
        JTextArea result = new JTextArea();
        result.setEditable(false);
        result.setBackground(new Color(240, 240, 240));
        result.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        idLabel.setBounds(30, 40, 100, 30);
        idLabel.setForeground(Color.WHITE);
        idLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        idField.setBounds(120, 40, 150, 30);
        idField.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        delete.setBounds(120, 80, 100, 30);
        delete.setBackground(new Color(255, 51, 51)); // red for delete
        delete.setForeground(Color.WHITE);
        delete.setFocusPainted(false);
        delete.setFont(new Font("Segoe UI", Font.BOLD, 12));
        // Add hover effect
        delete.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                delete.setBackground(new Color(204, 0, 0));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                delete.setBackground(new Color(255, 51, 51));
            }
        });

        result.setBounds(30, 120, 260, 120);

        panel.add(title);
        panel.add(idLabel);
        panel.add(idField);
        panel.add(delete);
        panel.add(result);

        frame.add(panel);

        frame.setSize(340, 280);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        delete.addActionListener(e -> {
            String id = idField.getText().trim();
            if (id.isBlank()) {
                result.setText("Employee ID is required.");
                return;
            }

            boolean deleted = DatabaseConnection.deleteEmployee(id);

            if(deleted){
                result.setText("Employee deleted.");
            } else {
                result.setText("Employee ID not found.");
            }
        });
    }
}
