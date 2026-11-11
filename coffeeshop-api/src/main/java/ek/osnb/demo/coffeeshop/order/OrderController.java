package ek.osnb.demo.coffeeshop.order;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
class OrderController {

    private final OrderService service;

    OrderController(OrderService service) {
        this.service = service;
    }

    // Checkout: turns a cart into an order
    /*@PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    OrderResponse placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        return service.placeOrder(request);
    }
     */

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    OrderResponse placeOrder(Authentication auth, @Valid @RequestBody PlaceOrderRequest request) {
        return service.placeOrder(auth.getName(), request);
    }

    // replaced by another method which takes auth as parameter
    /*
    // GET /api/orders?customerId=1 - newest first
    @GetMapping
    List<OrderSummaryResponse> findByCustomer(@RequestParam Long customerId) {
        return service.findByCustomer(customerId);
    }
     */

    @GetMapping
    List<OrderSummaryResponse> findByCustomer(Authentication auth) {
        return service.findByCustomer(auth.getName());
    }

    /*
    @GetMapping("/{id}")
    OrderResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }
     */

    @GetMapping("/{id}")
    OrderResponse findById(Authentication auth, @PathVariable Long id) {
        boolean isStaff = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"));

        if(isStaff){
            return service.findById(id);
        }

        return service.findMyOrder(auth.getName(), id);
    }

    // replaced by another method which takes auth as parameter
    /*
    @PostMapping("/{id}/cancel")
    OrderResponse cancel(@PathVariable Long id) {
        return service.cancel(id);
    }
     */

    @PostMapping("/{id}/cancel")
    OrderResponse cancel(Authentication auth, @PathVariable Long id) {
        return service.cancel(auth.getName(), id);
    }

    // Should only be allowed for staff - a job for Spring Security later
    @PostMapping("/{id}/ship")
    OrderResponse ship(@PathVariable Long id) {
        return service.ship(id);
    }
}
