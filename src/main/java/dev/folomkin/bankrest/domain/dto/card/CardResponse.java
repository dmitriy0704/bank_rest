package dev.folomkin.bankrest.domain.dto.card;

import dev.folomkin.bankrest.domain.dto.user.UserResponse;
import dev.folomkin.bankrest.domain.model.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardResponse(
        Long id,
        String encryptedNumber,
        LocalDate expirationDate,
        CardStatus cardStatus,
        BigDecimal balance,
        UserResponse owner
) {}
