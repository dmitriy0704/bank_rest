package dev.folomkin.bankrest.domain.dto.card;

import dev.folomkin.bankrest.domain.dto.user.UserResponse;
import dev.folomkin.bankrest.domain.model.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Возвращаемый объект при регистрации карты")
public record CardResponse(
        Long id,

        @Schema(description = "Номер карты с маской")
        String encryptedNumber,

        @Schema(description = "Дата окончания срока действия карты")
        LocalDate expirationDate,

        @Schema(description = "Статус карты")
        CardStatus cardStatus,

        @Schema(description = "Баланс карты")
        BigDecimal balance,

        @Schema(description = "Пользователь карты")
        UserResponse owner
) {}
