package dev.folomkin.bank_rest.service.card;

import dev.folomkin.bank_rest.domain.dto.card.CardRequest;
import dev.folomkin.bank_rest.domain.dto.card.CardResponse;
import dev.folomkin.bank_rest.domain.model.User;


public interface CardService {
    CardResponse create(CardRequest cardRequest, User user);
}
