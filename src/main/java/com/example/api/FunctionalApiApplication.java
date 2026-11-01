package com.example.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.api.entity.User;
import com.example.api.repository.UserRepository;

@SpringBootApplication
public class FunctionalApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FunctionalApiApplication.class, args);
    }

    @Bean
    CommandLineRunner seedDemoUser(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            org.springframework.core.env.Environment environment) {
        return args -> {
            String login = environment.getRequiredProperty("app.demo-user.login");
            String password = environment.getRequiredProperty("app.demo-user.password");
            if (userRepository.findByLogin(login).isEmpty()) {
                userRepository.save(new User(login, passwordEncoder.encode(password)));
            }
        };
    }
}
