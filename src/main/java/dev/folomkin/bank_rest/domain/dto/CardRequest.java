package dev.folomkin.bank_rest.domain.dto;

import java.math.BigDecimal;
import java.util.Date;

public record CardRequest(
        String openNumber,
        Date expirationDate,
        BigDecimal balance,
        Long userId
) {
}