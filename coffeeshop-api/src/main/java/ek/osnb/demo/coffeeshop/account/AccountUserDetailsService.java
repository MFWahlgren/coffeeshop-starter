package ek.osnb.demo.coffeeshop.account;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
class AccountUserDetailsService implements UserDetailsService {
    private final AccountRepository repository;

    AccountUserDetailsService(AccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String email = Account.normalizeEmail(username);

        Account account = repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));

        return User
                .withUsername(account.getEmail())
                .password(account.getPassword())
                .roles(account.getRole().name())
                .build();
    }
}
