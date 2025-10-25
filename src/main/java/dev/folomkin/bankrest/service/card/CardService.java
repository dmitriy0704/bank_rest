package dev.folomkin.bankrest.service.card;

import dev.folomkin.bankrest.domain.dto.card.*;
import dev.folomkin.bankrest.domain.model.User;

import java.util.List;


public interface CardService {
    CardResponse createCard(CardRequest cardRequest, User user);

    List<CardResponse> getCards();

    CardResponse getCardById(Long id);

    CardResponse getCardByNumber(String last4);

    CardBalanceChangeResponse balanceChange(CardBalanceChangeRequest cardBalanceChangeRequest,  User user);

    List<CardResponse> getCardsByUserId(Long userId);

    List<CardResponse> getCardsByPrincipal(User userId);

    CardResponse updateStatusById(Long cardId, CardStatusRequest cardRequest);

    CardResponse updateStatusByNumber(String cardNumber, CardStatusRequest cardRequest);

    void deleteCardById(Long id);

    void deleteCardByNumber(String last4);

    CardResponse sendingBlockingRequest(String cardNumber);

    List<CardResponse> getCardsByBlockRequest();

}
