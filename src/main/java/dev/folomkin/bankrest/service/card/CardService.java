package dev.folomkin.bankrest.service.card;

import dev.folomkin.bankrest.domain.dto.card.CardBalanceChangeRequest;
import dev.folomkin.bankrest.domain.dto.card.CardBalanceChangeResponse;
import dev.folomkin.bankrest.domain.dto.card.CardCreateRequest;
import dev.folomkin.bankrest.domain.dto.card.CardCreateResponse;
import dev.folomkin.bankrest.domain.model.User;

import java.util.List;


public interface CardService {
    CardCreateResponse createCard(CardCreateRequest cardCreateRequest, User user);
    List<CardCreateResponse> getCards();
    CardCreateResponse findById(Long id);
    CardCreateResponse findByEncryptedNumber(String last4);
    CardBalanceChangeResponse balanceChange(CardBalanceChangeRequest cardBalanceChangeRequest);
}
