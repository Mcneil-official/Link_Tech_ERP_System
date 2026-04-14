package erp.link_tech_erp.inventory;

import java.awt.Font;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLightLaf;

import erp.link_tech_erp.integration.GlobalLoginFrame;

public class Main {
    public static void main(String[] args) {
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("Table.rowHeight", 52);
            UIManager.put("TableHeader.height", 44);
            UIManager.put("ScrollBar.width", 8);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("TabbedPane.tabArc", 6);
            UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 15));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            SupabaseInitializer.initialize();
        } catch (Exception exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(
                null,
                "Database initialization failed. Please configure SUPABASE_DB_PASSWORD and try again.\n\n" + exception.getMessage(),
                "Supabase Error",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        GlobalLoginFrame.launch();
    }
}
