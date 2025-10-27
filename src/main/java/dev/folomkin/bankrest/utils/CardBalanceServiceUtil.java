package dev.folomkin.bankrest.utils;

import dev.folomkin.bankrest.domain.dto.card.CardBalanceChangeRequest;
import dev.folomkin.bankrest.domain.dto.card.CardBalanceChangeResponse;
import dev.folomkin.bankrest.domain.model.Card;
import dev.folomkin.bankrest.domain.model.CardStatus;
import dev.folomkin.bankrest.domain.model.User;
import dev.folomkin.bankrest.exceptions.InvalidCardFieldException;
import dev.folomkin.bankrest.repository.CardRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@AllArgsConstructor
public class CardBalanceServiceUtil {

    private CardRepository cardRepository;

    public CardBalanceChangeResponse balanceChange(CardBalanceChangeRequest request, User user) {

        if (request.cardNumberOut().equals(request.cardNumberIn())) {
            throw new InvalidCardFieldException("Номера карт должны быть разные");
        }

        Card cardOut = cardRepository.findCardByLast4(request.cardNumberOut());
        Card cardIn = cardRepository.findCardByLast4(request.cardNumberIn());

        validateRequest(cardIn, cardOut, user);

        BigDecimal cardOutBalance = cardOut.getBalance();
        BigDecimal cardInBalance = cardIn.getBalance();

        /// Если средств хватает - выполняем перевод
        int compBal = cardOutBalance.compareTo(request.amount());
        if (compBal < 0) {
            throw new InvalidCardFieldException("Недостаточно средств для перевода");
        }
        /// -> С карты отправителя снимаем средства
        cardOut.setBalance(cardOutBalance.subtract(request.amount()));
        /// -> зачисляя их на карту получатель
        cardIn.setBalance(cardInBalance.add(request.amount()));
        cardRepository.save(cardOut);
        cardRepository.save(cardIn);

        cardOutBalance = cardOut.getBalance();
        cardInBalance = cardIn.getBalance();

        return new CardBalanceChangeResponse(
                cardOut.getEncryptedNumber(),
                cardOutBalance,
                cardIn.getEncryptedNumber(),
                cardInBalance
        );
    }


    public void validateRequest(Card cardOut, Card cardIn, User user) {
        if (cardOut == null) {
            throw new InvalidCardFieldException("Не найдена карта-отправитель");
        }
        if (cardIn == null) {
            throw new InvalidCardFieldException("Не найдена карта-получатель");
        }

        if (cardOut.getCardStatus().equals(CardStatus.BLOCKREQUEST)
                || cardOut.getCardStatus().equals(CardStatus.BLOCKED)
        ) {
            throw new InvalidCardFieldException("Карта-отправитель заблокирована, вы не можете переводить с нее средства");
        }

        if (cardIn.getCardStatus().equals(CardStatus.BLOCKREQUEST)
                || cardIn.getCardStatus().equals(CardStatus.BLOCKED)
        ) {
            throw new InvalidCardFieldException("Карта-получатель заблокирована, вы не можете зачислять на нее средства");
        }

        if (!cardOut.getUser().getId().equals(user.getId())
                || !cardIn.getUser().getId().equals(user.getId())) {
            throw new InvalidCardFieldException(
                    "Вы можете переводить средства только между своими картами"
            );
        }
    }
}
