package com.zaxis.ecommerce_app.order;

import java.util.List;

class OrderDtos {

    public record OrderRequestDto(List<OrderItemDto> items) {

    }

    public record OrderItemDto(Long productId, int quantity) {

    }
}
