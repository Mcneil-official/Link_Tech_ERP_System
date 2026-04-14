package erp.link_tech_erp.finance.service;

public final class CurrentUserContext {
    private static final ThreadLocal<String> CURRENT_EMAIL = new ThreadLocal<>();

    private CurrentUserContext() {
    }

    public static void setCurrentEmail(String email) {
        if (email == null || email.isBlank()) {
            CURRENT_EMAIL.remove();
            return;
        }
        CURRENT_EMAIL.set(email.trim().toLowerCase());
    }

    public static String getCurrentEmail() {
        return CURRENT_EMAIL.get();
    }

    public static void clear() {
        CURRENT_EMAIL.remove();
    }
}
