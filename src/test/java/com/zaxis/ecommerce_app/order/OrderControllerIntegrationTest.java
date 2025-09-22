package com.zaxis.ecommerce_app.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxis.ecommerce_app.product.Product;
import com.zaxis.ecommerce_app.product.ProductRepository;
import com.zaxis.ecommerce_app.user.User;
import com.zaxis.ecommerce_app.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.jaxb.SpringDataJaxb;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        productRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser.setEmail("test@example.com");
        testUser.setRole(User.UserRole.USER);
        userRepository.save(testUser);

        product1 = new Product();
        product1.setName("Laptop");
        product1.setDescription("ACER");
        product1.setPrice(new BigDecimal("3000.00"));
        product1.setQuantity(10);
        productRepository.save(product1);

        product2 = new Product();
        product2.setName("Myszka");
        product2.setDescription("Logitech");
        product2.setPrice(new BigDecimal("100.00"));
        product2.setQuantity(5);
        productRepository.save(product2);
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
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
    @WithMockUser(username = "testuser", roles = "USER")
    void shouldReturnBadRequest_whenStockIsInsufficient() throws Exception {
        OrderDtos.OrderItemDto item1 = new OrderDtos.OrderItemDto(product1.getId(), 11);
        OrderDtos.OrderRequestDto orderRequest = new OrderDtos.OrderRequestDto(List.of(item1));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest());
    }
}