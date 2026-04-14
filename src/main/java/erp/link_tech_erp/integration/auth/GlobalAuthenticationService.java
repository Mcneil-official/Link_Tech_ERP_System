package erp.link_tech_erp.integration.auth;

import erp.link_tech_erp.finance.service.CurrentUserContext;

public final class GlobalAuthenticationService {
    private final UnifiedCredentialsRepository unifiedCredentialsRepository;

    public GlobalAuthenticationService() {
        this.unifiedCredentialsRepository = new UnifiedCredentialsRepository();
    }

    public GlobalSession authenticate(String loginIdentifier, String password) {
        String normalizedIdentifier = loginIdentifier == null ? "" : loginIdentifier.trim();
        if (normalizedIdentifier.isBlank() || password == null || password.isBlank()) {
            return null;
        }

        GlobalSession session = unifiedCredentialsRepository.authenticate(normalizedIdentifier, password);
        if (session == null) {
            CurrentUserContext.clear();
            return null;
        }

        return session;
    }
}