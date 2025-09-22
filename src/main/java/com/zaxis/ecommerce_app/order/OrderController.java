package com.zaxis.ecommerce_app.order;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDtos.OrderResponseDto> createOrder(@RequestBody OrderDtos.OrderRequestDto orderRequestDto) {
        try {
            OrderDtos.OrderResponseDto newOrder = orderService.createOrder(orderRequestDto);
            return new ResponseEntity<>(newOrder, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/checkout/{cartId}")
    public ResponseEntity<?> createOrderFromCart(@PathVariable Long cartId) {
        try {
            Order newOrder = orderService.createOrderFromCart(cartId);
            OrderDtos.OrderResponseDto responseDto = new OrderDtos.OrderResponseDto(
                    newOrder.getId(),
                    newOrder.getOrderDate(),
                    newOrder.getStatus(),
                    null
            );
            return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
