package dev.folomkin.bankrest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.folomkin.bankrest.config.security.SecurityConfiguration;
import dev.folomkin.bankrest.domain.dto.card.CardRequest;
import dev.folomkin.bankrest.domain.dto.card.CardResponse;
import dev.folomkin.bankrest.domain.dto.user.UserResponse;
import dev.folomkin.bankrest.domain.model.Card;
import dev.folomkin.bankrest.domain.model.CardStatus;
import dev.folomkin.bankrest.domain.model.Role;
import dev.folomkin.bankrest.domain.model.User;
import dev.folomkin.bankrest.repository.CardRepository;
import dev.folomkin.bankrest.repository.UserRepository;
import dev.folomkin.bankrest.service.card.CardService;
import dev.folomkin.bankrest.service.security.JwtService;
import dev.folomkin.bankrest.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(CardController.class)
@Import({SecurityConfiguration.class})
class CardControllerTest {

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardsService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthenticationProvider authenticationProvider;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    CardRepository cardRepository;


    @DisplayName("Создание карты авторизованным пользователем")
    @Test
    void createCardShouldReturnCreatedCard_AuthorizeUser() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("username");
        user.setPassword("password");
        user.setEmail("email@email.com");
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(Role.ROLE_USER);

//        Card card = new Card(
//                1L,
//                "1111 2222 3333 4444",
//                "**** **** **** 4444",
//                LocalDate.now(),
//                CardStatus.ACTIVE,
//                BigDecimal.valueOf(123.4),
//                user
//        );

        CardRequest cardRequest = new CardRequest(
                "1111 2222 3333 4444",
                LocalDate.now(),
                BigDecimal.valueOf(123.4),
                1L
        );

        UserResponse userResponse = new UserResponse(
                1L,
                "username",
                "email@email.com"
        );

        CardResponse cardResponse = new CardResponse(
                1L,
                "**** **** **** 4444",
                LocalDate.now(),
                CardStatus.ACTIVE,
                BigDecimal.valueOf(123.4),
                userResponse
        );
        when(this.cardsService.createCard(cardRequest, null)).thenReturn(cardResponse);
        mockMvc.perform(post("/api/v1/card/createCard")
                .with(csrf())
                .with(user("admin").roles("ADMIN"))
                .contentType("application/json")
                .content(mapper.writeValueAsString(cardRequest)));
    }

    @DisplayName("Создание карты неавторизованным пользователем")
    @Test
    void createCardShouldReturnCreatedCard_UnauthorizedUser() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("username");
        user.setPassword("password");
        user.setEmail("email@email.com");
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(Role.ROLE_USER);

        CardRequest cardRequest = new CardRequest(
                "1111 2222 3333 4444",
                LocalDate.now(),
                BigDecimal.valueOf(123.4),
                1L
        );

        UserResponse userResponse = new UserResponse(
                1L,
                "username",
                "email@email.com"
        );

        CardResponse cardResponse = new CardResponse(
                1L,
                "**** **** **** 4444",
                LocalDate.now(),
                CardStatus.ACTIVE,
                BigDecimal.valueOf(123.4),
                userResponse
        );
        when(this.cardsService.createCard(cardRequest, null)).thenReturn(cardResponse);
        mockMvc.perform(post("/api/v1/card/createCard")
                .with(csrf())
                .with(anonymous())
                .contentType("application/json")
                .content(mapper.writeValueAsString(cardRequest)));
    }


    @DisplayName("Просмотр списка карт авторизованным пользователем")
    @Test
    void findAllShouldReturnAllCardsAuthorizeUser() throws Exception {
        mockMvc.perform(get("/api/v1/cards/all")
                .with(csrf())
                .with(user("admin").roles("ADMIN"))
        ).andExpect(status().isOk());
    }


    @DisplayName("Просмотр списка карт неавторизованным пользователе")
    @Test
    void findAllShouldReturnAllCardsUnauthorizedUser() throws Exception {
        mockMvc.perform(get("/api/v1/cards/all")
                .with(csrf())
                .with(anonymous())
        ).andExpect(status().isUnauthorized());
    }


    @DisplayName("Получение карты по id авторизованным пользователем")
    @Test
    @WithMockUser(username = "username", roles = {"ADMIN"})
    void getCardsByIdAuthUser() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("username");
        user.setPassword("password");
        user.setEmail("email@email.com");
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(Role.ROLE_USER);
        when(cardRepository.findById(1L))
                .thenReturn(Optional.of(new Card(
                        1L,
                        "1111 2222 3333 4444",
                        "**** **** **** 4444",
                        LocalDate.now(),
                        CardStatus.ACTIVE,
                        BigDecimal.valueOf(123.4),
                        user
                )));
        mockMvc.perform(get("/api/v1/cards/card-id/{id}", 1L))
                .andExpect(status().isOk());
    }


    @DisplayName("Получение карты по id неавторизованным пользователем")
    @Test
    void getCardsNoAuthUser() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("username");
        user.setPassword("password");
        user.setEmail("email@email.com");
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(Role.ROLE_USER);
        when(cardRepository.findById(1L))
                .thenReturn(Optional.of(new Card(
                1L,
                "1111 2222 3333 4444",
                "**** **** **** 4444",
                LocalDate.now(),
                CardStatus.ACTIVE,
                BigDecimal.valueOf(123.4),
                user
        )));
        mockMvc.perform(get("/api/v1/cards/card-id/{id}", 1L)
                        .with(anonymous()))
                .andExpect(status().isUnauthorized());
    }


    @DisplayName("Получение карты по номеру авторизованным пользователем")
    @Test
    @WithMockUser(username = "username", roles = {"ADMIN"})
    void getCardsByNumberAuthUser() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("username");
        user.setPassword("password");
        user.setEmail("email@email.com");
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(Role.ROLE_USER);
        when(cardRepository.findCardByLast4("4444"))
                .thenReturn(new Card(
                        1L,
                        "1111 2222 3333 4444",
                        "**** **** **** 4444",
                        LocalDate.now(),
                        CardStatus.ACTIVE,
                        BigDecimal.valueOf(123.4),
                        user
                ));
        mockMvc.perform(get("/api/v1/cards/card-id/{id}", 1L))
                .andExpect(status().isOk());
    }


    @DisplayName("Удаление карты по id авторизованным пользователем")
    @Test
    void adminCanDeleteAuthUser() throws Exception {
        mockMvc.perform(delete("/api/v1/cards/delete-card-id/{cardId}", 1L)
                .with(csrf())
                .with(user("admin").roles("ADMIN"))
        ).andExpect(status().isNoContent());
    }


    @DisplayName("Удаление карты по id неавторизованным пользователем")
    @Test
    void cannotDeleteNoAuthUser() throws Exception {
        mockMvc.perform(delete("/api/v1/cards/delete-card-id/{cardId}", 1L)
                .with(csrf())
                .with(anonymous())
        ).andExpect(status().isUnauthorized());
    }
}