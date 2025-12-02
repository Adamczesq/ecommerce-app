package com.zaxis.ecommerce_app.cart;

import com.zaxis.ecommerce_app.shared.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CartControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    @WithMockUser(username = "testuser")
    void shouldReturnCart() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldAddItemToCart_andReturnUpdatedCart() throws Exception {
        Long productId = product1.getId();
        int quantity = 2;

        mockMvc.perform(post("/api/cart/items")
                        .param("productId", String.valueOf(productId))
                        .param("quantity", String.valueOf(quantity)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].product.id").value(productId))
                .andExpect(jsonPath("$.items[0].quantity").value(quantity));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldUpdateItemQuantity_whenItemIsInCart() throws Exception {
        Cart cartWithItem = cartService.addProductToCart(product1.getId(), 1);
        Long cartItemId = cartWithItem.getItems().get(0).getId();
        int newQuantity = 5;

        mockMvc.perform(put("/api/cart/items/" + cartItemId)
                        .param("quantity", String.valueOf(newQuantity)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(newQuantity));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldRemoveItemFromCart_whenItemIsInCart() throws Exception {
        cartService.addProductToCart(product1.getId(), 1);
        Cart cartWithItems = cartService.addProductToCart(product2.getId(), 3);
        Long itemToRemoveId = cartWithItems.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product1.getId()))
                .findFirst().get().getId();

        mockMvc.perform(delete("/api/cart/items/" + itemToRemoveId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].product.id").value(product2.getId()));
    }
}
