package dev.folomkin.bankrest.domain.dto.card;

import dev.folomkin.bankrest.domain.model.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Форма обновления статуса карты")
public record CardStatusRequest(
        @Schema(description = "Статус карты", example = "ACTIVE, BLOCKED, ISEXPIRATEDDATE")
        @NotNull
        CardStatus cardStatus
) {}