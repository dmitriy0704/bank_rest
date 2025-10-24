package dev.folomkin.bankrest.service.card;

import dev.folomkin.bankrest.domain.dto.card.CardBalanceChangeRequest;
import dev.folomkin.bankrest.domain.dto.card.CardBalanceChangeResponse;
import dev.folomkin.bankrest.domain.dto.card.CardCreateRequest;
import dev.folomkin.bankrest.domain.dto.card.CardCreateResponse;
import dev.folomkin.bankrest.domain.mapper.CardMapper;
import dev.folomkin.bankrest.domain.model.Card;
import dev.folomkin.bankrest.domain.model.User;
import dev.folomkin.bankrest.exceptions.NoSuchElementException;
import dev.folomkin.bankrest.repository.CardRepository;
import dev.folomkin.bankrest.repository.UserRepository;
import dev.folomkin.bankrest.utils.CardBalanceServiceUtil;
import dev.folomkin.bankrest.utils.CardSaveServiceUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class CardServiceImpl implements CardService {

    private CardRepository cardRepository;
    private UserRepository userRepository;
    private CardMapper cardMapper;
    private CardSaveServiceUtil cardSaveServiceUtil;
    private CardBalanceServiceUtil cardBalanceServiceUtil;

    @Override
    @Transactional
    public CardCreateResponse createCard(CardCreateRequest cardCreateRequest, User currentUser) {
        return cardSaveServiceUtil.saveCard(cardCreateRequest, currentUser);
    }

    @Override
    public List<CardCreateResponse> getCards() {
        return cardMapper.toCardResponseList(cardRepository.findAll());
    }

    public CardCreateResponse findById(Long id) {
        Card card = cardRepository.findById(id).orElseThrow(
                () -> new NoSuchElementException("Карта с id " + id + " не найдена")
        );
        return cardMapper.toCardResponse(card);
    }

    public CardCreateResponse findByEncryptedNumber(String last4) {
        Card card = cardRepository.findByLast4(last4);
        if (card == null) {
            throw new NoSuchElementException("Карта с номером **** **** **** " + last4 + " не найдена");
        }
        return cardMapper.toCardResponse(card);
    }


    @Override
    @Transactional
    public CardBalanceChangeResponse balanceChange(CardBalanceChangeRequest request) {
        return cardBalanceServiceUtil.balanceChange(request);
    }
}
