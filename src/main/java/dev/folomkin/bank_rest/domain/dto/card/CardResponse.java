package dev.folomkin.bank_rest.domain.dto.card;

import dev.folomkin.bank_rest.domain.dto.user.UserResponse;

import java.math.BigDecimal;
import java.util.Date;

public record CardResponse(
        Long id,
        String encryptedNumber,
        Date expirationDate,
        BigDecimal balance,
        UserResponse user
) {}
