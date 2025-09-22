package com.zaxis.ecommerce_app.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

class OrderDtos {

    public record OrderRequestDto(List<OrderItemDto> items) {

    }

    public record OrderItemDto(Long productId, int quantity) {

    }

    public record OrderResponseDto(Long id, LocalDateTime orderDate, String status, List<OrderItemResponseDto> items) {

    }

    public record OrderItemResponseDto(Long productId, String productName, int quantity, BigDecimal price) {

    }
}
