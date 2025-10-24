package dev.folomkin.bankrest.domain.dto.card;

import java.math.BigDecimal;

public record CardBalanceChangeResponse(
        String cardNumberOut,
        BigDecimal balanceOut,
        String cardNumberIn,
        BigDecimal balanceIn
) {
}
