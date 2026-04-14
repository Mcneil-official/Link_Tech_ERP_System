package erp.link_tech_erp.integration.auth;

public record GlobalSession(
    ModuleAccess activeModule,
    String loginIdentifier,
    String displayName) {

    public GlobalSession {
        if (activeModule == null) {
            throw new IllegalArgumentException("Active module is required.");
        }
        if (loginIdentifier == null || loginIdentifier.isBlank()) {
            throw new IllegalArgumentException("Login identifier is required.");
        }
        displayName = (displayName == null || displayName.isBlank()) ? loginIdentifier.trim() : displayName.trim();
    }

    public boolean canAccess(ModuleAccess module) {
        return module != null && activeModule == module;
    }
}