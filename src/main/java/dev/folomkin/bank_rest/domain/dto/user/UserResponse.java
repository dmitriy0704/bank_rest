package dev.folomkin.bank_rest.domain.dto.user;

public record UserResponse(
        Long id,
        String username,
        String email
) {}