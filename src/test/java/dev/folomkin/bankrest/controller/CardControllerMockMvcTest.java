package dev.folomkin.bankrest.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import dev.folomkin.bankrest.domain.dto.card.CardRequest;
import dev.folomkin.bankrest.domain.dto.card.CardResponse;
import dev.folomkin.bankrest.domain.dto.user.UserResponse;
import dev.folomkin.bankrest.domain.model.CardStatus;
import dev.folomkin.bankrest.domain.model.User;
import dev.folomkin.bankrest.exceptions.NoSuchElementException;
import dev.folomkin.bankrest.service.card.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class CardControllerMockMvcTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CardService cardService;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @BeforeEach
    public void setUp(WebApplicationContext webApplicationContext,
                      RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation)).build();
    }


    @DisplayName("Добавление карты с правами администратора")
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void whenPostRequestToCardAndValidCard_thenCorrectResponse() throws Exception {
        CardRequest request = new CardRequest(
                "6318 0172 1681 9169",
                LocalDate.of(2025, 10, 29),
                BigDecimal.valueOf(123.4),
                1L);
        UserResponse userResponse = new UserResponse(
                1L,
                "username",
                "email@email.com"
        );
        CardResponse cardResponse = new CardResponse(
                1L,
                "**** **** **** 9169",
                LocalDate.now(),
                CardStatus.ACTIVE,
                BigDecimal.valueOf(123.4),
                userResponse);
        String json = objectMapper.writeValueAsString(request);
        when(cardService.createCard(any(CardRequest.class), any(User.class))).thenReturn(cardResponse);
        mockMvc.perform(post("/api/v1/cards/create-card")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)).andExpect(status().isCreated());
    }


    @DisplayName("Добавление карты с невалидным полем")
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void whenPostRequestToCardAndValidCard_thenInvalidResponse() throws Exception {
        MediaType applicationJson = new MediaType(MediaType.APPLICATION_JSON);
        CardRequest request = new CardRequest(
                "",
                LocalDate.of(2025, 10, 29),
                BigDecimal.valueOf(123.4),
                1L);
        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(post("/api/v1/cards/create-card")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content()
                        .contentType(applicationJson));
    }


    @DisplayName("Получение списка всех карт с правами администратора")
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldReturnAllTasks() throws Exception {
        mockMvc.perform(get("/api/v1/cards/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


    @DisplayName("Получение карты по id с правами администратора")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    void shouldReturnOneCard() throws Exception {
        //Given
        UserResponse userResponse = new UserResponse(
                1L,
                "username",
                "email@email.com"
        );
        Long cardId = 1L;
        CardResponse cardResponse = new CardResponse(
                cardId,
                "**** **** **** 0001",
                LocalDate.now(),
                CardStatus.ACTIVE,
                BigDecimal.valueOf(123.4),
                userResponse);
        when(cardService.getCardById(cardId)).thenReturn(cardResponse);
        // When
        ResultActions resultActions =
                mockMvc.perform(get("/api/v1/cards/card-id/{cardId}", cardResponse.id())
                        .contentType(MediaType.APPLICATION_JSON));
        // Then
        resultActions.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(cardResponse.id()))
                .andExpect(jsonPath("$.encryptedNumber").value(cardResponse.encryptedNumber()))
                .andExpect(jsonPath("$.expirationDate").value(cardResponse.expirationDate().toString()))
                .andExpect(jsonPath("$.balance").value(cardResponse.balance().toString()))
                .andExpect(jsonPath("$.owner.id").value(cardResponse.owner().id()));
    }



    @DisplayName("Получение карты по номеру с правами администратора")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    void shouldReturnOneCardByNumber() throws Exception {
        //Given
        UserResponse userResponse = new UserResponse(
                1L,
                "username",
                "email@email.com"
        );
        Long cardId = 1L;
        String cardNumber = "0001";
        CardResponse cardResponse = new CardResponse(
                cardId,
                "**** **** **** 0001",
                LocalDate.now(),
                CardStatus.ACTIVE,
                BigDecimal.valueOf(123.4),
                userResponse);
        when(cardService.getCardByNumber(cardNumber)).thenReturn(cardResponse);
        // When
        ResultActions resultActions =
                mockMvc.perform(get("/api/v1/cards/card-number/{cardNumber}", cardNumber)
                        .contentType(MediaType.APPLICATION_JSON));
        // Then
        resultActions.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(cardResponse.id()))
                .andExpect(jsonPath("$.encryptedNumber").value(cardResponse.encryptedNumber()))
                .andExpect(jsonPath("$.expirationDate").value(cardResponse.expirationDate().toString()))
                .andExpect(jsonPath("$.balance").value(cardResponse.balance().toString()))
                .andExpect(jsonPath("$.owner.id").value(cardResponse.owner().id()));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnNotFoundWhenCardDoesNotExist() throws Exception {
        Long cardId = 999L;

        when(cardService.getCardById(cardId))
                .thenThrow(new NoSuchElementException("Карта с id " + cardId + " не найдена"));

        mockMvc.perform(get("/api/v1/cards/card-id/{id}", cardId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    @DisplayName("Удаление карты")
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void whenDeleteRequestToCardAndValidCard_thenValidResponse() throws Exception {
        MediaType applicationJson = new MediaType(MediaType.APPLICATION_JSON);
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/cards/delete-card-id/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(print())
                .andExpect(content()
                        .contentType(applicationJson));
    }
}
