package dev.folomkin.bankrest.controller;

import dev.folomkin.bankrest.domain.dto.card.CardBalanceChangeRequest;
import dev.folomkin.bankrest.domain.dto.card.CardBalanceChangeResponse;
import dev.folomkin.bankrest.domain.dto.card.CardResponse;
import dev.folomkin.bankrest.domain.dto.user.UserResponse;
import dev.folomkin.bankrest.domain.model.User;
import dev.folomkin.bankrest.service.card.CardService;
import dev.folomkin.bankrest.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
@Tag(
        name = "Пользователи",
        description = "Действия пользователя и над пользователем. Только для авторизованных пользователей"
)
public class UserController {

    private final UserService userService;
    private final CardService cardService;

    /// -> Метода Администратора:
    ///
    /// getAllUsers() - Получение списка всех пользователей
    /// getUserById() - Получение пользователя по id
    /// getUsersPages() - Получение списка пользователей с пагинацией
    ///
    /// -> Методы пользователя
    ///
    /// changeCardBalance() - Перевод между своими картами. Указывается последние 4 цифры номера карты-отправителя, последние 4 цифры номера карты-получателя и сумма перевода
    /// sendingBlockingRequest() - Отправка запроса на блокировку карты. Пользователь меняет статус карты на BLOCKREQUEST, Администратор видит все карты с этим статусом и блокирует

    @Operation(summary = "Получение списка всех пользователей. Для Администратора", description = "")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserResponse>> getUsers() {
        return new ResponseEntity<>(userService.getUsers(), HttpStatus.OK);
    }


    @Operation(summary = "Получение пользователя по id. Для Администратора", description = "")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{userid}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable("userid")
            @Parameter(description = "Id пользователя", required = true)
            Long userId
    ) {
        return new ResponseEntity<>(userService.getUserById(userId), HttpStatus.OK);
    }


    @Operation(summary = "Получение списка своих карт пользователем. Для Пользователя", description = "")
    @GetMapping("/my-cards")
    public Page<CardResponse> getMyCards(
            @RequestParam(value = "offset", defaultValue = "0")
            @Min(0) @Parameter(description = "Номер страницы с результатом") Integer offset,
            @RequestParam(value = "limit", defaultValue = "30") @Min(1) @Max(30)
            @Parameter(description = "Количество выводимых пользователей на странице. Минимум 1, максимум 30") Integer limit,
            @RequestParam(value = "sort") @Parameter(description = "Поле сортировки") String sortField,
            @RequestParam(value = "cardNumber", required = false) @Parameter(description = "Укажите 4 последние цифры номера своей карты") String cardNumber,
            @AuthenticationPrincipal User user
    ) {
        PageRequest pageRequest = PageRequest.of(offset, limit, Sort.by(Sort.Direction.ASC, sortField));
        return cardService.getCardsByPrincipal(pageRequest, cardNumber, user);
    }


    @Operation(
            summary = "Перевод средств между своими картами. Для Пользователя",
            description = "Укажите последние 4 цифры номера карты-отправителя, карты-получателя и сумму перевода")
    @PostMapping(value = "/change-balance", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CardBalanceChangeResponse> changeCardBalance(
            @RequestBody CardBalanceChangeRequest changeRequest,
            @AuthenticationPrincipal User user
    ) {
        return new ResponseEntity<>(cardService.balanceChange(changeRequest, user), HttpStatus.OK);
    }


    @Operation(
            summary = "Отправка пользователем запроса на блокировку карты. Для Пользователя",
            description = "Укажите последние 4 цифры номера"
    )
    @GetMapping(value = "/block-request/{cardNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CardResponse> sendingBlockingRequest(
            @PathVariable("cardNumber")
            @Parameter(description = "Последние 4 цифры номера карты", required = true) String cardNumber,
            @AuthenticationPrincipal User user
    ) {
        return new ResponseEntity<>(cardService.sendingBlockingRequest(cardNumber, user), HttpStatus.OK);
    }


    @Operation(summary = "Получение списка пользователей постранично. Для Администратора", description = "")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/filter-users", produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<UserResponse> getUsersPages(
            @RequestParam(value = "offset", defaultValue = "0")
            @Min(0) @Parameter(description = "Номер страницы с результатом") Integer offset,
            @RequestParam(value = "limit", defaultValue = "30") @Min(1) @Max(30)
            @Parameter(description = "Количество выводимых пользователей на странице. Минимум 1, максимум 30") Integer limit,
            @RequestParam(value = "sort") @Parameter(description = "Поле сортировки") String sortField
    ) {
        PageRequest pageRequest = PageRequest.of(offset, limit, Sort.by(Sort.Direction.ASC, sortField));
        return userService.getUsersPages(pageRequest);
    }
}
