package dev.folomkin.bankrest.domain.dto.card;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Форма регистрации карты")
public record CardRequest(
        @Size(min = 16, max = 16, message = "Номер карты должен содержать 16 цифр")
        @NotBlank(message = "Номер карты не должен быть пустым")
        @Pattern(regexp = "\\d{16}", message = "Номер карты должен состоять из 16 цифр")
        @Schema(description = "Номер карты")
        String openNumber,

        @NotNull(message = "Вы должны указать дату окончания действия карты")
        @Schema(description = "Дата окончание срока действия карты")
        LocalDate expirationDate,

//        @Schema(description = "Статус карты", example = "ACTIVE, BLOCKED, ISEXPIRATEDDATE")
//        @NotNull
//        CardStatus cardStatus,

        @Schema(description = "Баланс карты")
        @NotNull
        BigDecimal balance,

        @Schema(description = "ID владельца карты")
        @NotNull
        Long userId // -> Протестировать без userId
) {}