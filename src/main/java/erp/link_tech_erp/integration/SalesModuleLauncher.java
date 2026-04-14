package erp.link_tech_erp.integration;

import javax.swing.SwingUtilities;

import erp.link_tech_erp.sales.MainApp;

public final class SalesModuleLauncher {
    private SalesModuleLauncher() {
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> new MainApp().setVisible(true));
    }
}