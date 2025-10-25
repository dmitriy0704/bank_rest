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
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
@Tag(name = "Пользователи", description = "Действия пользователя и над пользователем. Только для авторизованных пользователей")
public class UserController {

    private final UserService userService;
    private final CardService cardService;

    /// Методы пользователя:
    ///
    /// getAllUsers() - Получение списка всех пользователей
    /// getUserById() - Получение пользователя по id
    /// changeCardBalance() - Перевод между своими картами. Указывается последние 4 цифры номера карты-отправителя, последние 4 цифры номера карты-получателя и сумма перевода
    /// sendingBlockingRequest() - Отправка запроса на блокировку карты. Пользователь меняет статус карты на BLOCKREQUEST, Администратор видит все карты с этим статусом и блокирует

    @Operation(summary = "Получение списка всех пользователей", description = "")
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserResponse>> getUsers() {
        return new ResponseEntity<>(userService.getUsers(), HttpStatus.OK);
    }

    @Operation(summary = "Получение пользователя по id", description = "")
    @GetMapping("/{userid}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable("userid")
            @Parameter(description = "Id пользователя", required = true)
            Long userId
    ) {
        return new ResponseEntity<>(userService.getUserById(userId), HttpStatus.OK);
    }


    @Operation(summary = "Получение списка своих карт пользователем", description = "")
    @GetMapping("/my-cards")
    public ResponseEntity<List<CardResponse>> getMyCards(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(cardService.getCardsByPrincipal(user), HttpStatus.OK);
    }


    /// -> Добавить идентификацию пользователя
    @Operation(summary = "Перевод средств между своими картами", description = "")
    @PostMapping(value = "/change-balance", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CardBalanceChangeResponse> changeCardBalance(
            @RequestBody CardBalanceChangeRequest changeRequest,
            @AuthenticationPrincipal User user
    ) {
        return new ResponseEntity<>(cardService.balanceChange(changeRequest, user), HttpStatus.OK);
    }


    /// -> Запрос на блокировку:

    @Operation(
            summary = "Отправка запроса на блокировку карты",
            description = "Укажите последние 4 цифры номера"
    )
    @GetMapping(value = "/block-request/{cardNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CardResponse> sendingBlockingRequest(
            @PathVariable("cardNumber")
            @Parameter(description = "Последние 4 цифры номера карты", required = true) String cardNumber
    ) {
        return new ResponseEntity<>(cardService.sendingBlockingRequest(cardNumber), HttpStatus.OK);
    }
}
