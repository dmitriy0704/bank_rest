package dev.folomkin.bankrest.controller;

import dev.folomkin.bankrest.domain.dto.card.CardBalanceChangeRequest;
import dev.folomkin.bankrest.domain.dto.card.CardBalanceChangeResponse;
import dev.folomkin.bankrest.domain.dto.card.CardRequest;
import dev.folomkin.bankrest.domain.dto.card.CardResponse;
import dev.folomkin.bankrest.domain.mapper.CardMapper;
import dev.folomkin.bankrest.domain.model.User;
import dev.folomkin.bankrest.service.card.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/cards")
@AllArgsConstructor
@Tag(name = "Управление картами", description = "Только для авторизованных пользователей")
public class CardController {

    private final CardMapper cardMapper;
    private CardService cardService;

    @Operation(summary = "Создание карты", description = "")
    @PostMapping(value = "/create-card", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize(value = "hasRole('ADMIN')")
    public ResponseEntity<CardResponse> create(@Valid @RequestBody CardRequest cardRequest, User user) {
        return new ResponseEntity<>(cardService.createCard(cardRequest, user), HttpStatus.CREATED);
    }

    @Operation(summary = "Получение списка всех карт", description = "")
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize(value = "hasRole('ADMIN')")
    public List<CardResponse> getAllCards() {
        return cardService.getCards();
    }

    @Operation(summary = "Получение карты по id", description = "Поиск по id карты")
    @GetMapping(value = "/card-id/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize(value = "hasRole('ADMIN')")
    public ResponseEntity<CardResponse> getCardById(@PathVariable("id")
                                                    @Parameter(description = "Id карты", required = true)
                                                    Long id) {
        return new ResponseEntity<>(cardService.getCardById(id), HttpStatus.OK);
    }

    @Operation(
            summary = "Получение карты по номеру",
            description = "Поиск производится по последним 4-м цифрам номера карты"
    )
    @GetMapping(value = "/card-number/{number}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize(value = "hasRole('ADMIN')")
    public ResponseEntity<CardResponse> getCardByNumber(@PathVariable("number")
                                                        @Parameter(description = "Номер карты", required = true)
                                                        String number) {
        return new ResponseEntity<>(cardService.getCardByNumber(number), HttpStatus.OK);
    }

    @Operation(summary = "Перевод средств между картами", description = "")
    @PostMapping(value = "/change-balance", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize(value = "hasRole('ADMIN')")
    public ResponseEntity<CardBalanceChangeResponse> changeCardBalance(
            @RequestBody CardBalanceChangeRequest changeRequest) {
        return new ResponseEntity<>(cardService.balanceChange(changeRequest), HttpStatus.OK);
    }

    @Operation(summary = "Получение списка всех карт пользователя", description = "")
    @GetMapping(value = "/cards-userid/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize(value = "hasRole('ADMIN')")
    public List<CardResponse> getCardsByUserId(@PathVariable("userId")
                                               @Parameter(description = "Id пользователя", required = true)
                                               Long userId) {
        return cardService.getCardsByUserId(userId);
    }


    @Operation(
            summary = "Обновление статуса карты по id",
            description = "Необходимо указать id карты и одно из доступных значений статуса")
    @PutMapping(value = "/update-status-id/{cardId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CardResponse> updateCardStatusById(
            @PathVariable("cardId")
            @Parameter(description = "Идентификатор карты", required = true)
            Long cardId,
            @RequestBody @Parameter(description = "Статус карты", required = true)
            CardRequest cardRequest
    ) {
        return new ResponseEntity<>(cardService.updateStatusById(cardId, cardRequest), HttpStatus.OK);
    }


    @Operation(summary = "Обновление статуса карты по номеру",
            description = "Необходимо указать последние 4 цифры номера карты и одно из доступных значений статуса")
    @PutMapping(value = "/update-status-number/{cardNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CardResponse> updateCardStatusByNumber(
            @PathVariable("cardNumber")
            @Parameter(description = "Номер карты", required = true)
            String cardNumber,
            @RequestBody @Parameter(description = "Статус карты", required = true)
            CardRequest cardRequest
    ) {
        return new ResponseEntity<>(cardService.updateStatusByNumber(cardNumber, cardRequest), HttpStatus.OK);
    }

    @Operation(summary = "Удаление карты по id", description = "Необходимо указать id карты")
    @DeleteMapping(value = "/delete-card-id/{cardId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize(value = "hasRole('ADMIN')")
    public ResponseEntity<String> deleteCardById(@PathVariable("cardId")
                                                 @Parameter(description = "ID карты", required = true) Long cardId
    ) {
        cardService.deleteCardById(cardId);
        return new ResponseEntity<>("Карта успешно удалена", HttpStatus.NO_CONTENT);
    }


    @Operation(summary = "Удаление карты по номеру", description = "Необходимо указать последние 4 цифры карты")
    @DeleteMapping(value = "/delete-card-number/{number}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize(value = "hasRole('ADMIN')")
    public ResponseEntity<String> deleteCardByNumber(@PathVariable("number")
                                                     @Parameter(description = "ID карты", required = true) String number
    ) {
        cardService.deleteCardByNumber(number);
        return new ResponseEntity<>("Карта успешно удалена", HttpStatus.NO_CONTENT);
    }


    /**
     * Используется для детализации ошибок полей DTO
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
}
