package dev.folomkin.bankrest.utils;

import dev.folomkin.bankrest.domain.dto.card.CardCreateRequest;
import dev.folomkin.bankrest.domain.dto.card.CardCreateResponse;
import dev.folomkin.bankrest.domain.mapper.CardMapper;
import dev.folomkin.bankrest.domain.model.Card;
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

    public CardCreateResponse saveCard(CardCreateRequest cardCreateRequest, User currentUser) {

        //-> Поиск по последним 4-м цифрам номера карты
        String openNumber = cardCreateRequest.openNumber();
        String searchedNumber = openNumber.substring(openNumber.length() - 4);

        log.info("Поиск карты с номером: {}", searchedNumber);

        Card card = cardRepository.findByLast4(searchedNumber);

        if (card != null) {
            throw new InvalidCardFieldException(
                    "Карта с номером *** *** *** " + searchedNumber + " уже существует"
            );
        }

        card = new Card();
        card.setOpenNumber(cardCreateRequest.openNumber());
        card.setEncryptedNumber(
                encryptedNumber(cardCreateRequest.openNumber())
        );
        card.setExpirationDate(cardCreateRequest.expirationDate());
        card.setBalance(cardCreateRequest.balance());
        User userToAssign;
        if (cardCreateRequest.userId() != null) {
            userToAssign = userRepository.findById(cardCreateRequest.userId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "Пользователь с id " + cardCreateRequest.userId() + " не найден"
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
        if (!digits.matches("\\d{16}"))
            return digits;     // если не 16 цифр — просто возвращаем как есть
        return digits.replaceAll("\\d{4}(?=\\d{4})", "**** ").trim();
    }
}
