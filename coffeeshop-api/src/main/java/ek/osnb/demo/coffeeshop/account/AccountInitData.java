package ek.osnb.demo.coffeeshop.account;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
class AccountInitData implements CommandLineRunner {
    private final AccountRepository accounts;

    AccountInitData(AccountRepository accounts) {
        this.accounts = accounts;
    }

    @Override
    public void run(String... args) throws Exception {
        if (accounts.findByEmail("staff@example.com").isEmpty()) {
            accounts.save(Account.create("staff@example.com",
                    "$2a$12$oF..ekU87tjm0HBof/Zjw.h4YmltfZ6bHlyOWqIMBhKx48X2guruK",
                    Role.STAFF));
        }
    }
}
