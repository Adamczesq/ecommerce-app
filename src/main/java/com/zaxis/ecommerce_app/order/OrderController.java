package com.zaxis.ecommerce_app.order;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderDtos.OrderRequestDto orderRequestDto) {
        try {
            OrderDtos.OrderResponseDto newOrder = orderService.createOrder(orderRequestDto);
            return new ResponseEntity<>(newOrder, HttpStatus.CREATED);

        } catch (ObjectOptimisticLockingFailureException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Niestety, cena lub dostępność produktów zmieniła się w trakcie składania zamówienia. Odśwież koszyk.");

        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/checkout/{cartId}")
    public ResponseEntity<?> createOrderFromCart(@PathVariable Long cartId) {
        try {
            OrderDtos.OrderResponseDto newOrder = orderService.createOrderFromCart(cartId);
            return new ResponseEntity<>(newOrder, HttpStatus.CREATED);

        } catch (ObjectOptimisticLockingFailureException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Niestety, cena lub dostępność produktów zmieniła się w trakcie składania zamówienia. Odśwież koszyk.");

        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
