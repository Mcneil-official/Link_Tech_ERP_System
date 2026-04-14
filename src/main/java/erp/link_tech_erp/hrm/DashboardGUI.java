package erp.link_tech_erp.hrm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import erp.link_tech_erp.integration.GlobalLoginFrame;
import erp.link_tech_erp.integration.auth.GlobalSessionContext;

public class DashboardGUI {

    public DashboardGUI(){

        JFrame frame = new JFrame("HRM Dashboard");

        // Background panel with BorderLayout
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(30, 30, 60)); // dark blue theme
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(30, 30, 60));
        JLabel title = new JLabel("HRM DASHBOARD", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        titlePanel.add(title);

        // Buttons panel with vertical layout
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setBackground(new Color(30, 30, 60));

        JButton add = new JButton("➕ Add Employee");
        JButton view = new JButton("👁️ View Employees");
        JButton update = new JButton("✏️ Update Employee");
        JButton delete = new JButton("🗑️ Delete Employee");
        JButton search = new JButton("🔍 Search Employee");
        JButton signOut = new JButton("↩ Sign Out");

        // Style buttons
        JButton[] buttons = {add, view, update, delete, search, signOut};
        for (JButton btn : buttons) {
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(250, 40));
            btn.setPreferredSize(new Dimension(200, 40));
            btn.setBackground(new Color(0, 153, 255));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            // Add hover effect
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    ((JButton)evt.getSource()).setBackground(new Color(0, 102, 204));
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    ((JButton)evt.getSource()).setBackground(new Color(0, 153, 255));
                }
            });
            buttonsPanel.add(Box.createVerticalStrut(10)); // spacing
            buttonsPanel.add(btn);
        }
        // Special styling for delete button
        delete.setBackground(new Color(255, 51, 51));
        delete.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                delete.setBackground(new Color(204, 0, 0));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                delete.setBackground(new Color(255, 51, 51));
            }
        });

        signOut.setBackground(new Color(255, 193, 7));
        signOut.setForeground(Color.BLACK);
        signOut.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                signOut.setBackground(new Color(224, 168, 0));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                signOut.setBackground(new Color(255, 193, 7));
            }
        });

        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(buttonsPanel, BorderLayout.CENTER);

        frame.add(panel);

        frame.setSize(350, 450);
        frame.setMinimumSize(new Dimension(300, 400));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        add.addActionListener(e -> new AddEmployeeGUI());
        view.addActionListener(e -> new ViewEmployeeGUI());
        update.addActionListener(e -> new UpdateEmployeeGUI());
        delete.addActionListener(e -> new DeleteEmployeeGUI());
        search.addActionListener(e -> new SearchEmployeeGUI());
        signOut.addActionListener(e -> {
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

    }
}
