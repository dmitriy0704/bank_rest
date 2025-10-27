package dev.folomkin.bankrest.service.card;

import dev.folomkin.bankrest.domain.dto.card.*;
import dev.folomkin.bankrest.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;


public interface CardService {
    CardResponse createCard(CardRequest cardRequest, User user);

    List<CardResponse> getCards();

    CardResponse getCardById(Long id);

    CardResponse getCardByNumber(String last4);

    CardBalanceChangeResponse balanceChange(CardBalanceChangeRequest cardBalanceChangeRequest, User user);

    List<CardResponse> getCardsByUserId(Long userId);

    Page<CardResponse> getCardsByPrincipal(PageRequest pageRequest, String cardNumber, User userId);

    CardResponse updateStatusById(Long cardId, CardStatusRequest cardRequest);

    CardResponse updateStatusByNumber(String cardNumber, CardStatusRequest cardRequest);

    void deleteCardById(Long id);

    void deleteCardByNumber(String last4);

    CardResponse sendingBlockingRequest(String cardNumber, User user);

    List<CardResponse> getCardsByBlockRequest();

    Page<CardResponse> getCardsPages(PageRequest pageRequest, String owner);
}
