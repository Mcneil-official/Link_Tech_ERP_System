package erp.link_tech_erp.integration;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import erp.link_tech_erp.inventory.MainFrame;
import erp.link_tech_erp.inventory.SupabaseInitializer;

public final class InventoryModuleLauncher {
    private InventoryModuleLauncher() {
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            try {
                SupabaseInitializer.initialize();
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            } catch (Exception exception) {
                JOptionPane.showMessageDialog(
                    null,
                    "Inventory module failed to start. Check Supabase config env vars and try again.\n\n"
                        + exception.getMessage(),
                    "Inventory Launch Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}