package dev.folomkin.bankrest.utils;

import dev.folomkin.bankrest.domain.dto.card.CardRequest;
import dev.folomkin.bankrest.domain.dto.card.CardResponse;
import dev.folomkin.bankrest.domain.mapper.CardMapper;
import dev.folomkin.bankrest.domain.model.Card;
import dev.folomkin.bankrest.domain.model.CardStatus;
import dev.folomkin.bankrest.domain.model.User;
import dev.folomkin.bankrest.exceptions.InvalidCardFieldException;
import dev.folomkin.bankrest.exceptions.NoSuchElementException;
import dev.folomkin.bankrest.repository.CardRepository;
import dev.folomkin.bankrest.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class CardSaveServiceUtil {

    private CardRepository cardRepository;
    private UserRepository userRepository;
    private CardMapper cardMapper;

    public CardResponse saveCard(CardRequest cardRequest, User currentUser) {

        //-> Поиск по последним 4-м цифрам номера карты
        String openNumber = cardRequest.openNumber();
        String searchedNumber = openNumber.substring(openNumber.length() - 4);

        Card card = cardRepository.findCardByLast4(searchedNumber);

        if (card != null) {
            throw new InvalidCardFieldException(
                    "Карта с номером *** *** *** " + searchedNumber + " уже существует"
            );
        }

        if (cardRequest.balance().signum() == -1) {
            throw new InvalidCardFieldException("Баланс не должен быть отрицательным");
        }

        card = new Card();
        card.setOpenNumber(cardRequest.openNumber());
        card.setEncryptedNumber(encryptedNumber(cardRequest.openNumber()));
        card.setExpirationDate(cardRequest.expirationDate());
        card.setCardStatus(CardStatus.ACTIVE);
        card.setBalance(cardRequest.balance());

        User userToAssign;
        if (cardRequest.userId() != null && cardRequest.userId() != 0) {
            userToAssign = userRepository.findById(cardRequest.userId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "Пользователь с id " + cardRequest.userId() + " не найден"
                    ));
        } else {
            userToAssign = currentUser;
        }
        card.setUser(userToAssign);
        Card saved = cardRepository.save(card);
        return cardMapper.toCardResponse(saved);
    }

    public String encryptedNumber(String openNumber) {
        if (openNumber == null) return null;
        String digits = openNumber.replaceAll("\\s+", ""); // убираем все пробелы
        return digits.replaceAll("\\d{4}(?=\\d{4})", "**** ").trim();
    }
}
