package com.zaxis.ecommerce_app.cart;

import com.zaxis.ecommerce_app.product.Product;
import com.zaxis.ecommerce_app.product.ProductRepository;
import com.zaxis.ecommerce_app.user.User;
import com.zaxis.ecommerce_app.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;

    @Transactional
    public Cart addProductToCart(Long productId, int quantity) {
        Cart cart = findOrCreateCartForCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono produktu"));

        cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .ifPresentOrElse(
                        item -> item.setQuantity(item.getQuantity() + quantity),
                        () -> {
                            CartItem newItem = new CartItem();
                            newItem.setProduct(product);
                            newItem.setQuantity(quantity);
                            newItem.setCart(cart);
                            cart.getItems().add(newItem);
                        }
                );

        return cartRepository.save(cart);
    }

    public Cart getCartForCurrentUser() {
        return findOrCreateCartForCurrentUser();
    }

    @Transactional
    public Cart updateCartItemQuantity(Long cartItemId, int newQuantity) {
        Cart cart = findOrCreateCartForCurrentUser();

        CartItem itemToUpdate = cart.getItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono pozycji w koszyku"));

        if (newQuantity <= 0) {
            cart.getItems().remove(itemToUpdate);
            cartItemRepository.delete(itemToUpdate);
        } else {
            if (itemToUpdate.getProduct().getQuantity() < newQuantity) {
                throw new IllegalStateException("Niewystarczająca ilość produktu w magazynie");
            }
            itemToUpdate.setQuantity(newQuantity);
        }

        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeCartItem(Long cartItemId) {
        Cart cart = findOrCreateCartForCurrentUser();

        boolean removed = cart.getItems().removeIf(item -> item.getId().equals(cartItemId));

        if (!removed) {
            throw new IllegalArgumentException("Nie znaleziono pozycji w koszyku");
        }

        return cartRepository.save(cart);
    }

    private Cart findOrCreateCartForCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow();

        return cartRepository.findByUserId(user.getId()).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });
    }
}
