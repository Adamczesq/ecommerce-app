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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;

    @Transactional
    public OrderDtos.OrderResponseDto createOrderFromCart(Long cartId) {
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
            OrderItem orderItem = getOrderItemFromCartItem(cartItem, newOrder);
            newOrder.getItems().add(orderItem);
        }

        Order savedOrder = orderRepository.save(newOrder);

        cart.getItems().clear();
        cartRepository.save(cart);

        return mapOrderToResponseDto(savedOrder);
    }

    private static OrderItem getOrderItemFromCartItem(final CartItem cartItem, final Order newOrder) {
        Product product = cartItem.getProduct();

        if (product.getQuantity() < cartItem.getQuantity()) {
            throw new IllegalStateException("Niewystarczająca ilość produktu: " + product.getName());
        }

        product.setQuantity(product.getQuantity() - cartItem.getQuantity());

        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setPrice(product.getPrice());
        orderItem.setOrder(newOrder);
        return orderItem;
    }

    @Transactional
    public OrderDtos.OrderResponseDto createOrder(OrderDtos.OrderRequestDto orderRequestDto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Nie znaleziono użytkownika"));

        List<Long> productIds = orderRequestDto.items().stream()
                .map(OrderDtos.OrderItemDto::productId)
                .toList();

        List<Product> products = productRepository.findAllById(productIds);

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));


        Order newOrder = new Order();
        newOrder.setUser(user);

        for (OrderDtos.OrderItemDto itemDto : orderRequestDto.items()) {
            Product product = productMap.get(itemDto.productId());

            if (product == null) {
                throw new IllegalArgumentException("Nie znaleziono produktu o ID: " + itemDto.productId());
            }

            if (product.getQuantity() < itemDto.quantity()) {
                throw new IllegalStateException("Niewystarczająca ilość produktu: " + product.getName());
            }

            product.setQuantity(product.getQuantity() - itemDto.quantity());

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
