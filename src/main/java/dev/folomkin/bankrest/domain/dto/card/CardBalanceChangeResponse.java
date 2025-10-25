package dev.folomkin.bankrest.domain.dto.card;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Возвращаемый объект при переводе средств между карт")
public record CardBalanceChangeResponse(

        @Schema(description = "Номер карты отправителя")
        String cardNumberOut,

        @Schema(description = "Баланс карты отправителя")
        BigDecimal balanceOut,

        @Schema(description = "Номер карты получателя")
        String cardNumberIn,

        @Schema(description = "Баланс карты получателя")
        BigDecimal balanceIn
) {
}
