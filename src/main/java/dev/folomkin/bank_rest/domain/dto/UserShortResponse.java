package dev.folomkin.bank_rest.domain.dto;

public record UserShortResponse(
        Long id,
        String username,
        String email
) {}