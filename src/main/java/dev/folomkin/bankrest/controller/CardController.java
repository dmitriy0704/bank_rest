package dev.folomkin.bankrest.controller;

import dev.folomkin.bankrest.domain.dto.card.CardBalanceChangeRequest;
import dev.folomkin.bankrest.domain.dto.card.CardBalanceChangeResponse;
import dev.folomkin.bankrest.domain.dto.card.CardCreateRequest;
import dev.folomkin.bankrest.domain.dto.card.CardCreateResponse;
import dev.folomkin.bankrest.domain.model.User;
import dev.folomkin.bankrest.service.card.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/cards")
@AllArgsConstructor
@Tag(name = "Управление картами", description = "Только для авторизованных пользователей")
public class CardController {

    private CardService cardService;

    @Operation(summary = "Создание карты", description = "")
    @PostMapping("/create-card")
    public ResponseEntity<CardCreateResponse> create(@RequestBody CardCreateRequest cardCreateRequest, User user) {
        return new ResponseEntity<>(cardService.createCard(cardCreateRequest, user), HttpStatus.CREATED);
    }

    @Operation(summary = "Получение списка всех карт", description = "")
    @GetMapping("/all")
    public List<CardCreateResponse> getAllCards() {
        return cardService.getCards();
    }

    @Operation(summary = "Получение карты по id", description = "Поиск по id карты")
    @GetMapping("/card-id")
    public ResponseEntity<CardCreateResponse> findById(@RequestParam Long id) {
        return new ResponseEntity<>(cardService.findById(id), HttpStatus.OK);
    }

    @Operation(
            summary = "Получение карты по номеру",
            description = "Поиск производится по последним 4-м цифрам номера карты"
    )
    @GetMapping("/card-number")
    public ResponseEntity<CardCreateResponse> findByEncryptedNumber(@RequestParam String number) {
        return new ResponseEntity<>(cardService.findByEncryptedNumber(number), HttpStatus.OK);
    }

    @Operation(summary = "Перевод средств между картами", description = "")
    @PostMapping("/change-balance")
    public ResponseEntity<CardBalanceChangeResponse> changeCardBalance(
            @RequestBody CardBalanceChangeRequest changeRequest) {
        return new ResponseEntity<>(cardService.balanceChange(changeRequest), HttpStatus.OK);
    }

    //-> Получение всех карт пользователя:
}
