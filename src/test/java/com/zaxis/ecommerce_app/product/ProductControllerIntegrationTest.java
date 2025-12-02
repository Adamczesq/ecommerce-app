package com.zaxis.ecommerce_app.product;

import com.zaxis.ecommerce_app.shared.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProductControllerIntegrationTest extends AbstractIntegrationTest {

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

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productWithCommaInPrice))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(149.99));
    }

    @Test
    void shouldThrowConflict_whenOptimisticLockingOccurs() throws Exception {
        Product productUserA = productRepository.findById(product1.getId()).orElseThrow();
        Product productUserB = productRepository.findById(product1.getId()).orElseThrow();

        productUserA.setQuantity(productUserA.getQuantity() - 1);
        productRepository.saveAndFlush(productUserA);

        productUserB.setQuantity(productUserB.getQuantity() - 10);

        assertThrows(ObjectOptimisticLockingFailureException.class, () -> {
            productRepository.saveAndFlush(productUserB);
        });
    }
}
