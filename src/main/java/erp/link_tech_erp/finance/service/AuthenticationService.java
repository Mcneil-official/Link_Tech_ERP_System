package erp.link_tech_erp.finance.service;

import erp.link_tech_erp.finance.repository.AccountRepository;

public class AuthenticationService {
    private final AccountRepository accountRepository;

    public AuthenticationService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public boolean authenticate(String email, String password) {
        boolean authenticated = accountRepository.authenticate(email, password);
        if (authenticated) {
            CurrentUserContext.setCurrentEmail(email);
        } else {
            CurrentUserContext.clear();
        }
        return authenticated;
    }
}