package dev.folomkin.bankrest.domain.dto.card;

import dev.folomkin.bankrest.domain.model.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardRequest(
        @Size(min = 16, max = 16, message = "Номер карты должен содержать 16 цифр")
        @NotBlank(message = "Номер карты не должен быть пустым")
        @Schema(description = "Номер карты")
        String openNumber,

        @NotNull
        @Schema(description = "Дата окончание срока действия карты")
        LocalDate expirationDate,

        @Schema(description = "Статус карты")
        @NotNull
        CardStatus cardStatus,

        @Schema(description = "Баланс карты")
        @NotNull
        BigDecimal balance,

        @Schema(description = "ID владельца карты")
        @NotNull
        Long userId // -> Протестировать без userId
) {}