package com.zaxis.ecommerce_app.order;

import com.zaxis.ecommerce_app.product.Product;
import com.zaxis.ecommerce_app.shared.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    @WithMockUser(username = "testuser")
    void shouldCreateOrder_whenUserIsAuthenticatedAndStockIsSufficient() throws Exception {
        OrderDtos.OrderItemDto item1 = new OrderDtos.OrderItemDto(product1.getId(), 2);
        OrderDtos.OrderItemDto item2 = new OrderDtos.OrderItemDto(product2.getId(), 1);
        OrderDtos.OrderRequestDto orderRequest = new OrderDtos.OrderRequestDto(List.of(item1, item2));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].productName").value("Laptop"));

        Product updatedProduct1 = productRepository.findById(product1.getId()).get();
        assertEquals(8, updatedProduct1.getQuantity());

        Product updatedProduct2 = productRepository.findById(product2.getId()).get();
        assertEquals(4, updatedProduct2.getQuantity());
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldReturnBadRequest_whenStockIsInsufficient() throws Exception {
        OrderDtos.OrderItemDto item1 = new OrderDtos.OrderItemDto(product1.getId(), 11);
        OrderDtos.OrderRequestDto orderRequest = new OrderDtos.OrderRequestDto(List.of(item1));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest());
    }
}