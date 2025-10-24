package dev.folomkin.bankrest.domain.dto.card;

import java.math.BigDecimal;

public record CardBalanceChangeRequest(
        String cardNumberOut,
        String cardNumberIn,
        BigDecimal amount
) {
}
