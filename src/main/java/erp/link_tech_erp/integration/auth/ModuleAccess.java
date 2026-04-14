package erp.link_tech_erp.integration.auth;

import erp.link_tech_erp.integration.FinanceModuleLauncher;
import erp.link_tech_erp.integration.HrmModuleLauncher;
import erp.link_tech_erp.integration.InventoryModuleLauncher;
import erp.link_tech_erp.integration.SalesModuleLauncher;

public enum ModuleAccess {
    FINANCE("Finance", "module:finance:access"),
    INVENTORY("Inventory", "module:inventory:access"),
    HRM("HRM", "module:hrm:access"),
    SALES("Sales", "module:sales:access");

    private final String displayName;
    private final String permissionKey;

    ModuleAccess(String displayName, String permissionKey) {
        this.displayName = displayName;
        this.permissionKey = permissionKey;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPermissionKey() {
        return permissionKey;
    }

    public String getDescription() {
        return switch (this) {
            case FINANCE -> "Financial controls and reporting";
            case INVENTORY -> "Stock, suppliers, and purchase orders";
            case HRM -> "Employees and records management";
            case SALES -> "Orders, delivery, and sales tracking";
        };
    }

    public void launch() {
        switch (this) {
            case FINANCE -> FinanceModuleLauncher.launch();
            case INVENTORY -> InventoryModuleLauncher.launch();
            case HRM -> HrmModuleLauncher.launch();
            case SALES -> SalesModuleLauncher.launch();
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}