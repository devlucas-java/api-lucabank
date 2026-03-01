package com.github.devlucasjava.apilucabank.service;

import com.github.devlucasjava.apilucabank.dto.mapper.AuthMapper;
import com.github.devlucasjava.apilucabank.dto.mapper.RegisterMapper;
import com.github.devlucasjava.apilucabank.dto.request.LoginRequest;
import com.github.devlucasjava.apilucabank.dto.request.RegisterRequest;
import com.github.devlucasjava.apilucabank.dto.response.AuthResponse;
import com.github.devlucasjava.apilucabank.exception.CustomAuthException;
import com.github.devlucasjava.apilucabank.exception.InternalErrorServerException;
import com.github.devlucasjava.apilucabank.exception.ResourceConflictException;
import com.github.devlucasjava.apilucabank.model.Role;
import com.github.devlucasjava.apilucabank.model.Users;
import com.github.devlucasjava.apilucabank.repository.RoleRepository;
import com.github.devlucasjava.apilucabank.repository.UsersRepository;
import com.github.devlucasjava.apilucabank.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsersRepository usersRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {

        if (usersRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Registration failed: email already registered - {}", request.getEmail());
            throw new ResourceConflictException("Email already registered");
        }

        if (usersRepository.findByPassport(request.getPassport()).isPresent()) {
            log.warn("Registration failed: passport already registered - {}", request.getPassport());
            throw new ResourceConflictException("Passport already registered");
        }

        Role defaultRole = roleRepository.findByName("USER")
                .orElseThrow(() -> {
                    log.error("Default role 'USER' not configured");
                    return new InternalErrorServerException("Default role not configured");
                });

        Users user = RegisterMapper.toUsers(request);
        user.setRole(defaultRole);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        usersRepository.save(user);
        log.debug("New user registered: {}", user.getEmail());

        String token = jwtService.generateToken(user);
        AuthResponse response = AuthMapper.toAuthResponse(user);
        response.setAccessToken(token);
        response.setExpiresIn(jwtService.jwtExpiration);

        return response;
    }

    public AuthResponse authenticate(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getLogin(), request.getPassword())
            );
            log.debug("Authentication successful for login: {}", request.getLogin());
        } catch (Exception ex) {
            log.warn("Authentication failed for login: {}", request.getLogin());
            throw new CustomAuthException("Invalid credentials");
        }

        Users user = usersRepository.findByEmailOrPassport(request.getLogin())
                .orElseThrow(() -> {
                    log.warn("Authentication failed: user not found - {}", request.getLogin());
                    return new CustomAuthException("User not found");
                });

        String token = jwtService.generateToken(user);
        AuthResponse response = AuthMapper.toAuthResponse(user);
        response.setAccessToken(token);
        response.setExpiresIn(jwtService.jwtExpiration);

        return response;
    }
}