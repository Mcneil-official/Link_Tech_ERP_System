package erp.link_tech_erp.integration;

import java.util.List;

import erp.link_tech_erp.integration.auth.ModuleAccess;

public interface ErpModule {
    String getName();

    ModuleAccess getAccess();

    String getDescription();

    default List<String> getConfigurationIssues() {
        return List.of();
    }

    void launch();
}
