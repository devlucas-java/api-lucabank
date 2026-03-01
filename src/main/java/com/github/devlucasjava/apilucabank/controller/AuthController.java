package com.github.devlucasjava.apilucabank.controller;

import com.github.devlucasjava.apilucabank.dto.request.LoginRequest;
import com.github.devlucasjava.apilucabank.dto.request.RegisterRequest;
import com.github.devlucasjava.apilucabank.dto.response.AuthResponse;
import com.github.devlucasjava.apilucabank.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication users")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Login de usuário", description = "Autentica o usuário e retorna token JWT")
    @ApiResponse(responseCode = "200", description = "Login realizado com sucesso",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = AuthResponse.class)))
    @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {

        log.debug("Login attempt for user: {}", loginRequest.getLogin());

        AuthResponse authResponse = authService.authenticate(loginRequest);

        log.debug("Login successful for user: {}", loginRequest.getLogin());

        return ResponseEntity.status(HttpStatus.OK).body(authResponse);
    }

    @Operation(summary = "Registro de usuário", description = "Cadastra um novo usuário e retorna token JWT")
    @ApiResponse(responseCode = "200", description = "Registro realizado com sucesso",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = AuthResponse.class)))
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {

        log.debug("Register attempt for user: {}", registerRequest.getEmail());

        AuthResponse authResponse = authService.register(registerRequest);

        log.debug("Registration successful for user: {}", registerRequest.getEmail());

        return ResponseEntity.status(HttpStatus.OK).body(authResponse);
    }
}