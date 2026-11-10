package ek.osnb.demo.coffeeshop.account;

import ek.osnb.demo.coffeeshop.common.ConflictException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class AccountService {
    private final AccountRepository repository;

    private final PasswordEncoder passwordEncoder;

    AccountService(AccountRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    void register(RegisterRequest request){
        requireEmailIsFree(request.email());

        Account account = Account.create(
                request.email(),
                passwordEncoder.encode(request.password()),
                Role.CUSTOMER
        );

        repository.save(account);
    }

    private void requireEmailIsFree(String email) {
        if(repository.findByEmail(Account.normalizeEmail(email)).isPresent()){
            throw new ConflictException("An account with this email already exists");
        }
    }
}
