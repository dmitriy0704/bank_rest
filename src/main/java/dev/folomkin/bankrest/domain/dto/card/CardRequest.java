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
        @Size(min = 19, max = 19, message = "Номер карты должен содержать 16 цифр и соответствовать паттерну 1111 2222 3333 4444")
        @NotBlank(message = "Номер карты не должен быть пустым")
        @Pattern(regexp = "^(\\d{4}(\\s)\\d{4}(\\s)\\d{4}(\\s)\\d{4})$", message = "Номер карты должен состоять только из цифр")
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
        Long userId
) {}