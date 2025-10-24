package dev.folomkin.bankrest.service.card;

import dev.folomkin.bankrest.domain.dto.card.CardBalanceChangeRequest;
import dev.folomkin.bankrest.domain.dto.card.CardBalanceChangeResponse;
import dev.folomkin.bankrest.domain.dto.card.CardRequest;
import dev.folomkin.bankrest.domain.dto.card.CardResponse;
import dev.folomkin.bankrest.domain.model.User;

import java.util.List;


public interface CardService {
    CardResponse createCard(CardRequest cardRequest, User user);

    List<CardResponse> getCards();

    CardResponse findById(Long id);

    CardResponse findByEncryptedNumber(String last4);

    CardBalanceChangeResponse balanceChange(CardBalanceChangeRequest cardBalanceChangeRequest);

    List<CardResponse> getCardsByUserId(Long userId);
}
