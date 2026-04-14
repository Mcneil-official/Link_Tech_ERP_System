package erp.link_tech_erp.integration.auth;

import java.util.concurrent.atomic.AtomicReference;

import erp.link_tech_erp.finance.service.CurrentUserContext;

public final class GlobalSessionContext {
    private static final AtomicReference<GlobalSession> CURRENT = new AtomicReference<>();

    private GlobalSessionContext() {
    }

    public static void set(GlobalSession session) {
        CURRENT.set(session);
        if (session != null && session.activeModule() == ModuleAccess.FINANCE) {
            CurrentUserContext.setCurrentEmail(session.loginIdentifier());
        } else {
            CurrentUserContext.clear();
        }
    }

    public static GlobalSession get() {
        return CURRENT.get();
    }

    public static boolean isAuthenticated() {
        return CURRENT.get() != null;
    }

    public static boolean canAccess(ModuleAccess module) {
        GlobalSession session = CURRENT.get();
        return session != null && session.canAccess(module);
    }

    public static void clear() {
        CURRENT.set(null);
        CurrentUserContext.clear();
    }
}