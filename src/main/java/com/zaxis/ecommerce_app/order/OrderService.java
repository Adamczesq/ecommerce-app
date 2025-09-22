package com.zaxis.ecommerce_app.order;

import com.zaxis.ecommerce_app.product.Product;
import com.zaxis.ecommerce_app.product.ProductRepository;
import com.zaxis.ecommerce_app.user.User;
import com.zaxis.ecommerce_app.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public Order createOrder(OrderDtos.OrderRequestDto orderRequestDto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Nie znaleziono użytkownika"));

        Order newOrder = new Order();
        newOrder.setUser(user);

        for (OrderDtos.OrderItemDto itemDto : orderRequestDto.items()) {
            Product product = productRepository.findById(itemDto.productId())
                    .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono produktu o ID: " + itemDto.productId()));

            if (product.getQuantity() < itemDto.quantity()) {
                throw new IllegalStateException("Niewystarczająca ilość produktu: " + product.getName());
            }

            product.setQuantity(product.getQuantity() - itemDto.quantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDto.quantity());
            orderItem.setPrice(product.getPrice());
            orderItem.setOrder(newOrder);
            newOrder.getItems().add(orderItem);
        }

        return orderRepository.save(newOrder);
    }
}
