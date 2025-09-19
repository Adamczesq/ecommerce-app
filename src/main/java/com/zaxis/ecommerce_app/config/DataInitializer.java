package com.zaxis.ecommerce_app.config;

import com.zaxis.ecommerce_app.user.User;
import com.zaxis.ecommerce_app.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123_!"));
            admin.setEmail("admin@example.com");
            admin.setRole(User.UserRole.ADMIN);
            userRepository.save(admin);
            System.out.println(">>> Stworzono domyślnego użytkownika: ADMIN");
        }

        if (userRepository.findByUsername("user").isEmpty()) {
            User regularUser = new User();
            regularUser.setUsername("user");
            regularUser.setPassword(passwordEncoder.encode("user123_!"));
            regularUser.setEmail("user@example.com");
            regularUser.setRole(User.UserRole.USER);
            userRepository.save(regularUser);
            System.out.println(">>> Stworzono domyślnego użytkownika: USER");
        }
    }
}
