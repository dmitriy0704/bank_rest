package dev.folomkin.bankrest.domain.dto.user;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String email
) {}