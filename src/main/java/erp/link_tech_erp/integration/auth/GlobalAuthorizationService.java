package erp.link_tech_erp.integration.auth;

public final class GlobalAuthorizationService {
    public boolean canAccess(ModuleAccess module) {
        return GlobalSessionContext.canAccess(module);
    }

    public void requireAccess(ModuleAccess module) {
        if (!canAccess(module)) {
            String moduleName = module == null ? "this module" : module.getDisplayName();
            throw new IllegalStateException("Access denied. Your account does not have permission to open "
                + moduleName + ".");
        }
    }
}