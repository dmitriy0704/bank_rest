package dev.folomkin.bankrest.utils;

import dev.folomkin.bankrest.domain.dto.card.CardBalanceChangeRequest;
import dev.folomkin.bankrest.domain.dto.card.CardBalanceChangeResponse;
import dev.folomkin.bankrest.domain.model.Card;
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

    public CardBalanceChangeResponse balanceChange(CardBalanceChangeRequest request) {

        Card cardOut = cardRepository.findByLast4(request.cardNumberOut());
        Card cardIn = cardRepository.findByLast4(request.cardNumberIn());

        BigDecimal cardOutBalance = cardOut.getBalance();
        BigDecimal cardInBalance = cardIn.getBalance();

        //-> Если баланс карты-отправителя больше баланса карты-получателя:
        if (cardOutBalance.compareTo(cardInBalance) > 0) {
            //-> Вычитаем из баланса карты-отправителя баланс карты-получателя
            BigDecimal diff = cardOutBalance.subtract(cardInBalance);//-> уточнить метод

            //=> Разницу сравниваем с суммой перевода:
            int res = diff.compareTo(request.amount());
            if (res < 0) {
                throw new InvalidCardFieldException("На карте-отправителе недостаточно средств для перевода");
            }

            cardIn.setBalance(cardInBalance.add(request.amount()));
            cardRepository.save(cardOut);
            cardRepository.save(cardIn);

        } else {
            throw new InvalidCardFieldException("Баланс карты-отправителя меньше баланса карты-получателя");
        }


        return new CardBalanceChangeResponse(
                cardOut.getEncryptedNumber(),
                cardOutBalance,
                cardIn.getEncryptedNumber(),
                cardInBalance
        );
    }
}
