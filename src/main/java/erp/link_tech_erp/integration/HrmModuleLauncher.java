package erp.link_tech_erp.integration;

import javax.swing.SwingUtilities;

import erp.link_tech_erp.hrm.DashboardGUI;

public final class HrmModuleLauncher {
    private HrmModuleLauncher() {
    }

    public static void launch() {
        SwingUtilities.invokeLater(DashboardGUI::new);
    }
}