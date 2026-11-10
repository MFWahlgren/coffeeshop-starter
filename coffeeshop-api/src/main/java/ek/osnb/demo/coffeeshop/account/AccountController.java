package ek.osnb.demo.coffeeshop.account;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/register")
class AccountController {
    private final AccountService service;

    AccountController(AccountService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    void create(@Valid @RequestBody RegisterRequest request){
        service.register(request);
    }

}
