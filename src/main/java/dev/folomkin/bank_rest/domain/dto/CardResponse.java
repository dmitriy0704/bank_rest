package dev.folomkin.bank_rest.domain.dto;

import java.math.BigDecimal;
import java.util.Date;

public record CardResponse(
        Long id,
        String encryptedNumber,
        Date expirationDate,
        BigDecimal balance,
        UserShortResponse user
) {}
