package dev.folomkin.bankrest.domain.dto.card;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Форма перевода средств между картами")
public record CardBalanceChangeRequest(
        @Schema(description = "Номер карты отправителя. Указать последние 4 цифры", example = "1111")
        String cardNumberOut,

        @Schema(description = "Номер карты получателя. Указать последние 4 цифры", example = "1111")
        String cardNumberIn,

        @Schema(description = "Сумма перевода", example = "12.3")
        BigDecimal amount
) {
}
