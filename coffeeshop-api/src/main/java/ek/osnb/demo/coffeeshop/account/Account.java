package ek.osnb.demo.coffeeshop.account;

import ek.osnb.demo.coffeeshop.common.InvalidInputException;
import jakarta.persistence.*;

import java.util.Locale;

@Entity
@Table(name = "accounts")
class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    protected Account(){}

    private Account(String email, String password, Role role){
        String validEmail = normalizeEmail(required(email, "email"));
        String validPw = required(password, "password");

        if (role == null) {
            throw new InvalidInputException("An account must have a role");
        }

        this.email = validEmail;
        this.password = validPw;
        this.role = role;
    }

    static Account create(String email, String password, Role role){
        return new Account(email, password, role);
    }

    // "  Anna@Example.COM " and "anna@example.com" are the same address
    static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new InvalidInputException("An account must have a " + field);
        }

        return value.trim();
    }

    String getEmail() {
        return email;
    }

    String getPassword() {
        return password;
    }

    Role getRole() {
        return role;
    }
}
