package ek.osnb.demo.coffeeshop.customer;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
class CustomerController {

    private final CustomerService service;

    CustomerController(CustomerService service) {
        this.service = service;
    }

    // Commented out, and replaced by GET and PUT /api/customers/me
    /*
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CustomerResponse create(@Valid @RequestBody CustomerRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    CustomerResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    // GET /api/customers?email=anna@example.com - email is required,
    // so this never lists every customer
    @GetMapping
    List<CustomerResponse> findByEmail(@RequestParam String email) {
        return service.findByEmail(email);
    }

    @PutMapping("/{id}")
    CustomerResponse update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {

        return service.update(id, request);
    }*/

    @GetMapping("/me")
    CustomerResponse getMe(Authentication auth){
        return service.findMyProfile(auth.getName());
    }

    @PutMapping("/me")
    CustomerResponse updateMe(
            Authentication auth,
            @Valid @RequestBody CustomerRequest request){

        return service.saveMyProfile(auth.getName(),request);
    }
}
