package dev.folomkin.bankrest.domain.mapper;

import dev.folomkin.bankrest.domain.dto.card.CardResponse;
import dev.folomkin.bankrest.domain.dto.user.UserResponse;
import dev.folomkin.bankrest.domain.model.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

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
                card.getCardStatus(),
                card.getBalance(),
                userResponse
        );
    }

    public List<CardResponse> toCardResponseList(List<Card> cards) {
        return cards.stream()
                .map(this::toCardResponse)
                .toList();
    }


//    public Page<CardResponse> toCardResponsePages(List<Card> cards) {
//        return (Page<CardResponse>) cards.stream()
//                .map(this::toCardResponse);
//
//    }
}
