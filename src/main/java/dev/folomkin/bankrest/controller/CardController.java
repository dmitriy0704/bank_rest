package dev.folomkin.bankrest.controller;

import dev.folomkin.bankrest.domain.dto.card.CardRequest;
import dev.folomkin.bankrest.domain.dto.card.CardResponse;
import dev.folomkin.bankrest.domain.dto.card.CardStatusRequest;
import dev.folomkin.bankrest.domain.model.User;
import dev.folomkin.bankrest.service.card.CardService;
import dev.folomkin.bankrest.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@Tag(name = "Карты", description = "Только для авторизованных пользователей. Для Администратора")
public class CardController {

    private CardService cardService;
    private UserService userService;

    /// Методы карт:
    ///
    /// create() - Создание карты с указанием id пользователя карты
    /// getAllCards() - Получение списка всех карт
    /// getCardById() - Получение карты по id
    /// getCardByNumber() - Получение карты по последним 4 цифрам номера
    /// getCardsByUserId() - Получение списка карт пользователя по id пользователя
    /// updateCardStatusById() - Обновление статуса карты по id карты
    /// updateCardStatusByNumber() - Обновление статуса карты по номеру карты
    /// deleteCardById() - Удаление карты по id карты
    /// deleteCardByNumber() - Удаление карты по номеру карты
    /// getCardsByBlockRequest() - Получение списка карт с запросом на блокировку
    /// getCardResponsePages() - Получение списка карт с пагинацией, фильтрацией и сортировкой


    @Operation(
            summary = "Создание карты",
            description = "Форма создания карты. " +
                    "Статус карты по умолчанию \"Активна\". " +
                    "Необходимо указать id пользователя для которого создается карта или 0 для текущего пользователя"
    )
    @PostMapping(value = "/create-card", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize(value = "hasRole('ADMIN')")
    public ResponseEntity<CardResponse> create(
            @Valid @RequestBody CardRequest cardRequest,
            @AuthenticationPrincipal User user) {
        return new ResponseEntity<>(cardService.createCard(cardRequest, user), HttpStatus.CREATED);
    }


    @Operation(
            summary = "Получение списка всех карт",
            description = "Список без пагинации и фильтров"
    )
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize(value = "hasRole('ADMIN')")
    public List<CardResponse> getAllCards() {
        return cardService.getCards();
    }


    @Operation(
            summary = "Получение карты по id",
            description = "Поиск по id карты"
    )
    @GetMapping(value = "/card-id/{cardId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize(value = "hasRole('ADMIN')")
    public ResponseEntity<CardResponse> getCardById(
            @PathVariable("cardId")
            @Parameter(description = "Id карты", required = true)
            Long cardId) {
        return new ResponseEntity<>(cardService.getCardById(cardId), HttpStatus.OK);
    }


    @Operation(
            summary = "Получение карты по номеру",
            description = "Укажите последние 4 цифры номера карты"
    )
    @GetMapping(value = "/card-number/{cardNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize(value = "hasRole('ADMIN')")
    public ResponseEntity<CardResponse> getCardByNumber(
            @PathVariable("cardNumber")
            @Parameter(description = "Номер карты", required = true)
            String cardNumber) {
        return new ResponseEntity<>(cardService.getCardByNumber(cardNumber), HttpStatus.OK);
    }


    @Operation(
            summary = "Получение списка всех карт пользователя",
            description = ""
    )
    @GetMapping(value = "/cards-userid/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize(value = "hasRole('ADMIN')")
    public List<CardResponse> getCardsByUserId(
            @PathVariable("userId")
            @Parameter(description = "Id пользователя", required = true)
            Long userId
    ) {
        return cardService.getCardsByUserId(userId);
    }


    @Operation(
            summary = "Обновление статуса карты по id",
            description = "Укажите id карты и одно из доступных значений статуса. Статусы: ACTIVE - Активна, BLOCKED - Заблокирована, ISEXPIRATEDDATE - Истек срок действия"
    )
    @PutMapping(value = "/update-status-card-id/{cardId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize(value = "hasRole('ADMIN')")
    public ResponseEntity<CardResponse> updateCardStatusById(
            @PathVariable("cardId")
            @Parameter(description = "Идентификатор карты", required = true)
            Long cardId,
            @RequestBody @Parameter(description = "Статус карты", required = true)
            CardStatusRequest cardRequest
    ) {
        return new ResponseEntity<>(cardService.updateStatusById(cardId, cardRequest), HttpStatus.OK);
    }


    @Operation(
            summary = "Обновление статуса карты по номеру",
            description = "Укажите последние 4 цифры номера карты и одно из доступных значений статуса. Статусы: ACTIVE - Активна, BLOCKED - Заблокирована, ISEXPIRATEDDATE - Истек срок действия"
    )
    @PutMapping(value = "/update-status-card-number/{cardNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize(value = "hasRole('ADMIN')")
    public ResponseEntity<CardResponse> updateCardStatusByNumber(
            @PathVariable("cardNumber")
            @Parameter(description = "Номер карты", required = true)
            String cardNumber,
            @RequestBody @Parameter(description = "Статус карты", required = true)
            CardStatusRequest cardRequest
    ) {
        return new ResponseEntity<>(cardService.updateStatusByNumber(cardNumber, cardRequest), HttpStatus.OK);
    }


    @Operation(
            summary = "Удаление карты по id",
            description = "Укажите id карты"
    )
    @DeleteMapping(value = "/delete-card-id/{cardId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize(value = "hasRole('ADMIN')")
    public ResponseEntity<String> deleteCardById(
            @PathVariable("cardId")
            @Parameter(description = "ID карты", required = true)
            Long cardId
    ) {
        cardService.deleteCardById(cardId);
        return new ResponseEntity<>("Карта успешно удалена", HttpStatus.OK);
    }


    @Operation(
            summary = "Удаление карты по номеру",
            description = "Укажите последние 4 цифры карты"
    )
    @DeleteMapping(value = "/delete-card-number/{cardNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize(value = "hasRole('ADMIN')")
    public ResponseEntity<String> deleteCardByNumber(
            @PathVariable("cardNumber")
            @Parameter(description = "Номер карты", required = true)
            String cardNumber
    ) {
        cardService.deleteCardByNumber(cardNumber);
        return new ResponseEntity<>("Карта успешно удалена", HttpStatus.OK);
    }


    @Operation(
            summary = "Получение списка карт с запросом на блокировку",
            description = ""
    )
    @GetMapping(value = "/cards-blockrequest", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize(value = "hasRole('ADMIN')")
    public ResponseEntity<List<CardResponse>> getCardsByBlockRequest() {
        return new ResponseEntity<>(cardService.getCardsByBlockRequest(), HttpStatus.OK);
    }


    @Operation(summary = "Получение списка всех карт постранично", description = "")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/filter-cards", produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<CardResponse> getCardsPages(
            @RequestParam(value = "offset", defaultValue = "0")
            @Min(0) @Parameter(description = "Номер страницы с результатом") Integer offset,
            @RequestParam(value = "limit", defaultValue = "50") @Min(1) @Max(50)
            @Parameter(description = "Количество выводимых карт на странице. Минимум 1, максимум 50") Integer limit,
            @RequestParam(value = "sort") @Parameter(description = "Поле сортировки") String sortField,
            @RequestParam(value = "owner", required = false) @Parameter(description = "Email пользователя") String owner
    ) {
        PageRequest pageRequest = PageRequest.of(offset, limit, Sort.by(Sort.Direction.ASC, sortField));
        return cardService.getCardsPages(pageRequest, owner);
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
