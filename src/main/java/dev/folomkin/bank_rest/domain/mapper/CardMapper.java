package dev.folomkin.bank_rest.domain.mapper;

import dev.folomkin.bank_rest.domain.dto.CardResponse;
import dev.folomkin.bank_rest.domain.dto.UserShortResponse;
import dev.folomkin.bank_rest.domain.model.Card;
import org.springframework.stereotype.Component;

@Component
public class CardMapper {
    public CardResponse toCardResponse(Card card) {
        if (card == null) return null;

        UserShortResponse userResponse = null;
        if (card.getUser() != null) {
            userResponse = new UserShortResponse(
                    card.getUser().getId(),
                    card.getUser().getUsername(),
                    card.getUser().getEmail()
            );
        }

        return new CardResponse(
                card.getId(),
                card.getEncryptedNumber(),
                card.getExpirationDate(),
                card.getBalance(),
                userResponse
        );
    }
}
