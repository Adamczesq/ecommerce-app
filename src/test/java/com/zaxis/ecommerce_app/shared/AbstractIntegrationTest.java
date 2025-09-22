package com.zaxis.ecommerce_app.shared;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxis.ecommerce_app.cart.CartService;
import com.zaxis.ecommerce_app.product.Product;
import com.zaxis.ecommerce_app.product.ProductRepository;
import com.zaxis.ecommerce_app.user.User;
import com.zaxis.ecommerce_app.user.UserRepository;
import com.zaxis.ecommerce_app.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected ProductRepository productRepository;
    @Autowired
    protected PasswordEncoder passwordEncoder;
    @Autowired
    protected UserService userService;
    @Autowired
    protected CartService cartService;

    protected User testUser;
    protected Product product1;
    protected Product product2;

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
}
