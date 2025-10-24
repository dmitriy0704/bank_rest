package dev.folomkin.bankrest.domain.dto.card;

import dev.folomkin.bankrest.domain.dto.user.UserResponse;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardCreateResponse(
        Long id,
        String encryptedNumber,
        LocalDate expirationDate,
        BigDecimal balance,
        UserResponse user
) {}
