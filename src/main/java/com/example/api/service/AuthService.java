package com.example.api.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import com.example.api.dto.auth.LoginRequest;
import com.example.api.dto.auth.LoginResponse;
import com.example.api.entity.User;
import com.example.api.repository.UserRepository;
import com.example.api.security.JwtService;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByLogin(request.getLogin())
                .orElseThrow(() -> new BadCredentialsException("Invalid login or password"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid login or password");
        }
        String safeLogin = HtmlUtils.htmlEscape(user.getLogin());
        return new LoginResponse(
                "Login successful for user '" + safeLogin + "'.",
                jwtService.createToken(user.getLogin()));
    }
}
