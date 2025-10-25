package dev.folomkin.bankrest.service.card;

import dev.folomkin.bankrest.domain.dto.card.*;
import dev.folomkin.bankrest.domain.mapper.CardMapper;
import dev.folomkin.bankrest.domain.model.Card;
import dev.folomkin.bankrest.domain.model.CardStatus;
import dev.folomkin.bankrest.domain.model.User;
import dev.folomkin.bankrest.exceptions.NoSuchElementException;
import dev.folomkin.bankrest.repository.CardRepository;
import dev.folomkin.bankrest.repository.UserRepository;
import dev.folomkin.bankrest.utils.CardBalanceServiceUtil;
import dev.folomkin.bankrest.utils.CardSaveServiceUtil;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
    public CardResponse createCard(CardRequest cardRequest, User currentUser) {
        return cardSaveServiceUtil.saveCard(cardRequest, currentUser);
    }

    @Override
    public List<CardResponse> getCards() {
        return cardMapper.toCardResponseList(cardRepository.findAll());
    }

    public CardResponse getCardById(Long id) {
        Card card = cardRepository.findById(id).orElseThrow(
                () -> new NoSuchElementException("Карта с id " + id + " не найдена")
        );
        return cardMapper.toCardResponse(card);
    }

    public CardResponse getCardByNumber(String last4) {
        Card card = cardRepository.findCardByLast4(last4);
        if (card == null) {
            throw new NoSuchElementException("Карта с номером **** **** **** " + last4 + " не найдена");
        }
        return cardMapper.toCardResponse(card);
    }

    @Override
    @Transactional
    public CardBalanceChangeResponse balanceChange(CardBalanceChangeRequest request, User user) {
        return cardBalanceServiceUtil.balanceChange(request, user);
    }

    @Override
    public List<CardResponse> getCardsByUserId(Long userId) {
        List<Card> cards = cardRepository.findAllCardsByUserId(userId);
        return cardMapper.toCardResponseList(cards);
    }

    @Override
    public List<CardResponse> getCardsByPrincipal(User principal) {
        List<Card> cardsByPrincipal = cardRepository.findAllCardsByUserId(principal.getId());
        return cardMapper.toCardResponseList(cardsByPrincipal);
    }


    @Override
    public CardResponse updateStatusById(Long cardId, CardStatusRequest cardRequest) {
        Card card = cardRepository.findById(cardId).orElseThrow(
                () -> new NoSuchElementException("Карта с id " + cardId + " не найдена")
        );
        card.setCardStatus(cardRequest.cardStatus());
        cardRepository.save(card);
        return cardMapper.toCardResponse(card);
    }

    @Override
    public CardResponse updateStatusByNumber(String cardNumber, CardStatusRequest cardRequest) {
        Card card = cardRepository.findCardByLast4(cardNumber);
        if (card == null) {
            throw new NoSuchElementException("Карта с номером **** **** **** " + cardNumber + " не найдена");
        }
        card.setCardStatus(cardRequest.cardStatus());
        cardRepository.save(card);
        return cardMapper.toCardResponse(card);
    }

    @Override
    public void deleteCardById(Long id) {
        Card card = cardRepository.findById(id).orElseThrow(
                () -> new NoSuchElementException("Карта с id " + id + " не найдена")
        );
        cardRepository.delete(card);
    }

    @Override
    public void deleteCardByNumber(String cardNumber) {
        Card card = cardRepository.findCardByLast4(cardNumber);
        if (card == null) {
            throw new NoSuchElementException("Карта с номером **** **** **** " + cardNumber + " не найдена");
        }
        cardRepository.delete(card);
    }


    @Override
    public CardResponse sendingBlockingRequest(String cardNumber) {
        Card card = cardRepository.findCardByLast4(cardNumber);
        if (card == null) {
            throw new NoSuchElementException("Карта с номером **** **** **** " + cardNumber + " не найдена");
        }
        card.setCardStatus(CardStatus.BLOCKREQUEST);
        cardRepository.save(card);
        return cardMapper.toCardResponse(card);
    }


    @Override
    public List<CardResponse> getCardsByBlockRequest() {
        return cardMapper.toCardResponseList(cardRepository.findAllCardsByBlockRequest());
    }


    @Override
    public Page<CardResponse> getCardsPages(PageRequest pageRequest, String owner) {
        List<Card> cards = cardRepository.findAll(pageRequest).getContent();
        if(owner != null) {
            return new PageImpl<>(cardMapper.toCardResponseList(
                    cards.stream()
                            .filter(c -> c.getUser().getEmail().equals(owner))
                            .collect(Collectors.toList())
            ), pageRequest, cards.size());
        } else {
            return new PageImpl<>(cardMapper.toCardResponseList(cards), pageRequest, cards.size());
        }
    }
}
