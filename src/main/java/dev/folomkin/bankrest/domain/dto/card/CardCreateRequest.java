package dev.folomkin.bankrest.domain.dto.card;

import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardCreateRequest(
        @Size(min = 16, max = 16, message = "Номер карты должен содержать 16 цифр")
        String openNumber,
        LocalDate expirationDate,
        BigDecimal balance,
        Long userId // -> Протестировать без userId
) {}