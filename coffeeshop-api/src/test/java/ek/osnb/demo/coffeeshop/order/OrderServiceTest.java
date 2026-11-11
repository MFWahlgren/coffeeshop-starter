package ek.osnb.demo.coffeeshop.order;

import ek.osnb.demo.coffeeshop.cart.CartApi;
import ek.osnb.demo.coffeeshop.cart.CartInfo;
import ek.osnb.demo.coffeeshop.cart.CartLineInfo;
import ek.osnb.demo.coffeeshop.catalog.CatalogApi;
import ek.osnb.demo.coffeeshop.catalog.InsufficientStockException;
import ek.osnb.demo.coffeeshop.catalog.ProductInfo;
import ek.osnb.demo.coffeeshop.common.ConflictException;
import ek.osnb.demo.coffeeshop.common.NotFoundException;
import ek.osnb.demo.coffeeshop.customer.CustomerApi;
import ek.osnb.demo.coffeeshop.customer.CustomerInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests OrderService on its own. The other features are mocked through their
 * *Api interfaces - possible because orders only know them by id and interface,
 * never by their entities or repositories. No Spring, no database, no Docker.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final CustomerInfo ANNA = new CustomerInfo(
            1L, "Anna Jensen", "anna@example.com", "Nørregade 1", "1165", "København K");

    private static final ProductInfo BEANS = new ProductInfo(10L, "Colombia Huila", new BigDecimal("99.00"), 50);
    private static final ProductInfo FILTERS = new ProductInfo(20L, "Paper Filters (100 pcs)", new BigDecimal("49.00"), 200);

    private static final Long CART_ID = 5L;

    @Mock
    private OrderRepository repository;

    @Mock
    private CustomerApi customers;

    @Mock
    private CartApi carts;

    @Mock
    private CatalogApi catalog;

    @Mock
    private ApplicationEventPublisher events;

    @InjectMocks
    private OrderService service;

    // ---------- Placing an order ----------

    @Test
    void placeOrderCopiesNamesPricesAndAddress() {
        givenCartWithBeansAndFilters();
        when(repository.save(any(Order.class))).thenAnswer(call -> call.getArgument(0));

        // Changed: the customer now comes from the login (the email), and the request
        // only has the cartId. Before, the request carried a customerId from the client.
        OrderResponse order = service.placeOrder(ANNA.email(), new PlaceOrderRequest(CART_ID));

        assertEquals(OrderStatus.PLACED, order.status());
        assertEquals(ANNA.id(), order.customerId());
        assertEquals("Anna Jensen", order.shippingAddress().recipientName());
        assertEquals("Colombia Huila", order.lines().get(0).productName());
        assertEquals(new BigDecimal("99.00"), order.lines().get(0).unitPrice());
        // 2 x 99.00 + 1 x 49.00
        assertEquals(new BigDecimal("247.00"), order.total());
    }

    @Test
    void placeOrderReservesStockClearsTheCartAndPublishesAnEvent() {
        givenCartWithBeansAndFilters();
        when(repository.save(any(Order.class))).thenAnswer(call -> call.getArgument(0));

        // Changed: email first, request with only the cartId (see the first test)
        service.placeOrder(ANNA.email(), new PlaceOrderRequest(CART_ID));

        verify(catalog).reserveStock(BEANS.id(), 2);
        verify(catalog).reserveStock(FILTERS.id(), 1);
        verify(carts).clearCart(CART_ID);

        ArgumentCaptor<OrderPlacedEvent> event = ArgumentCaptor.forClass(OrderPlacedEvent.class);
        verify(events).publishEvent(event.capture());
        assertEquals(ANNA.id(), event.getValue().customerId());
        assertEquals(new BigDecimal("247.00"), event.getValue().total());
    }

    @Test
    void emptyCartIsRejectedBeforeAnythingHappens() {
        // Changed: the service now looks the customer up with findCustomerByEmail, which
        // returns an Optional. The old stub (getCustomer(id)) is never called anymore, and
        // without this stub the mock returns an empty Optional, so the service would throw
        // NotFoundException ("No customer details saved yet") before it reaches the cart.
        when(customers.findCustomerByEmail(ANNA.email())).thenReturn(Optional.of(ANNA));
        when(carts.getCart(CART_ID)).thenReturn(new CartInfo(CART_ID, List.of()));

        assertThrows(ConflictException.class,
                () -> service.placeOrder(ANNA.email(), new PlaceOrderRequest(CART_ID)));

        verify(catalog, never()).reserveStock(anyLong(), anyInt());
        verify(repository, never()).save(any());
    }

    @Test
    void outOfStockStopsTheOrder() {
        givenCartWithBeansAndFilters();
        doThrow(new InsufficientStockException(BEANS.name(), 1))
                .when(catalog).reserveStock(BEANS.id(), 2);

        // Changed: email first, request with only the cartId (see the first test)
        assertThrows(InsufficientStockException.class,
                () -> service.placeOrder(ANNA.email(), new PlaceOrderRequest(CART_ID)));

        // In the real app the transaction is rolled back as well
        verify(repository, never()).save(any());
        verify(carts, never()).clearCart(anyLong());
        verify(events, never()).publishEvent(any(Object.class));
    }

    @Test
    void customerWithoutSavedDetailsCannotPlaceAnOrder() {
        when(customers.findCustomerByEmail(ANNA.email())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.placeOrder(ANNA.email(), new PlaceOrderRequest(CART_ID)));

        verify(carts, never()).getCart(anyLong());
        verify(repository, never()).save(any());
    }

    // ---------- Cancelling ----------

    @Test
    void cancelPutsTheItemsBackInStock() {
        Order order = Order.place(ANNA.id(), address(), List.of(
                new OrderLine(BEANS.id(), BEANS.name(), BEANS.price(), 2)
        ), Instant.now());

        // Changed: cancel() now does two lookups, so the test stubs two things:
        // 1) the customer from the email, 2) the order by BOTH order id and customer id.
        // Before it was repository.findById(7L), which any customer could use for any order.
        // The order is placed with ANNA.id(), so it matches the lookup (7, Anna's id).
        when(customers.findCustomerByEmail(ANNA.email())).thenReturn(Optional.of(ANNA));
        when(repository.findByIdAndCustomerId(7L, ANNA.id())).thenReturn(Optional.of(order));

        // Changed: cancel() takes the caller's email first
        OrderResponse cancelled = service.cancel(ANNA.email(), 7L);

        assertEquals(OrderStatus.CANCELLED, cancelled.status());
        verify(catalog).releaseStock(BEANS.id(), 2);
    }

    // ---------- Helpers ----------

    private void givenCartWithBeansAndFilters() {
        // Changed: findCustomerByEmail (returns an Optional) instead of getCustomer(id).
        // The three tests that use this helper are fixed here, in one place.
        when(customers.findCustomerByEmail(ANNA.email())).thenReturn(Optional.of(ANNA));
        when(carts.getCart(CART_ID)).thenReturn(new CartInfo(CART_ID, List.of(
                new CartLineInfo(BEANS.id(), 2),
                new CartLineInfo(FILTERS.id(), 1)
        )));
        when(catalog.findProducts(List.of(BEANS.id(), FILTERS.id())))
                .thenReturn(Map.of(BEANS.id(), BEANS, FILTERS.id(), FILTERS));
    }

    private static ShippingAddress address() {
        return new ShippingAddress(ANNA.name(), ANNA.street(), ANNA.postalCode(), ANNA.city());
    }
}