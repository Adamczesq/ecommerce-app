package com.zaxis.ecommerce_app.order;

import com.zaxis.ecommerce_app.cart.Cart;
import com.zaxis.ecommerce_app.cart.CartItem;
import com.zaxis.ecommerce_app.cart.CartRepository;
import com.zaxis.ecommerce_app.product.Product;
import com.zaxis.ecommerce_app.product.ProductRepository;
import com.zaxis.ecommerce_app.user.User;
import com.zaxis.ecommerce_app.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;

    @Transactional
    public Order createOrderFromCart(Long cartId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Nie znaleziono użytkownika"));
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono koszyka"));

        if (!cart.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Nie masz uprawnień do tego koszyka");
        }

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Nie można złożyć zamówienia z pustego koszyka");
        }

        Order newOrder = new Order();
        newOrder.setUser(user);

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            if (product.getQuantity() < cartItem.getQuantity()) {
                throw new IllegalStateException("Niewystarczająca ilość produktu: " + product.getName());
            }

            product.setQuantity(product.getQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem.setOrder(newOrder);
            newOrder.getItems().add(orderItem);
        }

        Order savedOrder = orderRepository.save(newOrder);

        cart.getItems().clear();
        cartRepository.save(cart);

        return savedOrder;
    }

    @Transactional
    public OrderDtos.OrderResponseDto createOrder(OrderDtos.OrderRequestDto orderRequestDto) {
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

        return mapOrderToResponseDto(orderRepository.save(newOrder));
    }

    private OrderDtos.OrderResponseDto mapOrderToResponseDto(Order order) {
        List<OrderDtos.OrderItemResponseDto> itemDtos = order.getItems().stream()
                .map(item -> new OrderDtos.OrderItemResponseDto(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getPrice()
                )).collect(Collectors.toList());

        return new OrderDtos.OrderResponseDto(
                order.getId(),
                order.getOrderDate(),
                order.getStatus(),
                itemDtos
        );
    }
}
