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
}
