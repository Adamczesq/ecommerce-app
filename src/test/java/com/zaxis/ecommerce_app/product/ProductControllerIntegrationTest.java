package com.zaxis.ecommerce_app.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnListOfProducts_whenGetProductsIsCalled() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateProduct_whenUserIsAdmin() throws Exception {
        Product newProduct = new Product();
        newProduct.setName("Testowy Laptop");
        newProduct.setDescription("Laptop wichura");
        newProduct.setPrice(new BigDecimal("2499.99"));
        newProduct.setQuantity(10);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Testowy Laptop"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbidden_whenUserIsNotAdmin() throws Exception {
        Product newProduct = new Product();
        newProduct.setName("Nieautoryzowany Produkt");
        newProduct.setPrice(new BigDecimal("100.00"));
        newProduct.setQuantity(1);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateProduct_whenPriceHasComma() throws Exception {
        String productWithCommaInPrice = """
    {
        "name": "Produkt z przecinkiem",
        "description": "Test deserializatora",
        "price": "149,99",
        "quantity": 20
    }
    """;

        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productWithCommaInPrice))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(149.99));
    }
}
