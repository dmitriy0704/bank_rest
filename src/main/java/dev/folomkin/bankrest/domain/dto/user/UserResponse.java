package dev.folomkin.bankrest.domain.dto.user;

public record UserResponse(
        Long id,
        String username,
        String email
) {}