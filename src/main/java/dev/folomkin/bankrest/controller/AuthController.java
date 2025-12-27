package dev.folomkin.bankrest.controller;

import dev.folomkin.bankrest.domain.dto.security.JwtResponse;
import dev.folomkin.bankrest.domain.dto.security.LoginRequest;
import dev.folomkin.bankrest.domain.dto.user.UserRequest;
import dev.folomkin.bankrest.domain.dto.user.UserResponse;
import dev.folomkin.bankrest.service.security.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Регистрация пользователя")
    @PostMapping("/register")
    public UserResponse register (@RequestBody @Valid UserRequest request) {
        return authService.register(request);
    }

    @Operation(summary = "Авторизация пользователя")
    @PostMapping("/login")
    public JwtResponse login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }

    /**
     * Используется для детализации ошибок полей DTO
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
}

