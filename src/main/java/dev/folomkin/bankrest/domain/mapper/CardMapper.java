package dev.folomkin.bankrest.domain.mapper;

import dev.folomkin.bankrest.domain.dto.card.CardResponse;
import dev.folomkin.bankrest.domain.dto.user.UserResponse;
import dev.folomkin.bankrest.domain.model.Card;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CardMapper {
    public CardResponse toCardResponse(Card card) {
        if (card == null) return null;

        UserResponse userResponse = null;
        if (card.getUser() != null) {
            userResponse = new UserResponse(
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

    public List<CardResponse> toCardResponseList(List<Card> cards) {
        return cards.stream()
                .map(this::toCardResponse)
                .toList();
    }
}
