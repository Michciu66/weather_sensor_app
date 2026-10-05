package com.chudzik.weather_sensors.config;

import com.chudzik.weather_sensors.model.Admin;
import com.chudzik.weather_sensors.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminDataInitializer {
            @Value("${ADMIN_USERNAME}")
            private String username;

            @Value("${ADMIN_PASSWORD}")
            private String password;

    @Bean
    CommandLineRunner initializeAdmin(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (adminRepository.findByUsername("admin").isEmpty()) {
                Admin admin = new Admin(username, passwordEncoder.encode(password));
                adminRepository.save(admin);
            }
        };
    }
}