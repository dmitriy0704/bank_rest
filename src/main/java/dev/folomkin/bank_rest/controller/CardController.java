package dev.folomkin.bank_rest.controller;

import dev.folomkin.bank_rest.domain.dto.card.CardRequest;
import dev.folomkin.bank_rest.domain.dto.card.CardResponse;
import dev.folomkin.bank_rest.domain.model.User;
import dev.folomkin.bank_rest.service.card.CardService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/card")
@AllArgsConstructor
public class CardController {

    private CardService cardService;

    @PostMapping
    public ResponseEntity<CardResponse> create(@RequestBody CardRequest cardRequest, User user) {
        return new ResponseEntity<>(cardService.create(cardRequest, user), HttpStatus.CREATED);
    }
}
