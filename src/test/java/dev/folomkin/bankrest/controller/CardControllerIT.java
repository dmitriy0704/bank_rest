package dev.folomkin.bankrest.controller;


import dev.folomkin.bankrest.domain.model.Card;
import dev.folomkin.bankrest.domain.model.CardStatus;
import dev.folomkin.bankrest.domain.model.Role;
import dev.folomkin.bankrest.domain.model.User;
import dev.folomkin.bankrest.repository.CardRepository;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
public class CardControllerIT {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;
    @Autowired
    private CardRepository cardRepository;

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


    @DisplayName("Получение списка всех карт с правами администратора")
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldReturnAllTasks() throws Exception {
        mockMvc.perform(get("/api/v1/cards/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


    @DisplayName("Получение карты по id с правами администратора")
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldReturnOneCard() throws Exception {
        //Given

        User user = new User();
        user.setId(1L);
        user.setUsername("username");
        user.setPassword("password");
        user.setEmail("email@email.com");
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(Role.ROLE_USER);
        Card card = new Card(
                1L,
                "1111 2222 3333 4444",
                "**** **** **** 4444",
                LocalDate.now(),
                CardStatus.ACTIVE,
                BigDecimal.valueOf(123.4),
                user
        );
        cardRepository.save(card);

        // When
        ResultActions resultActions = mockMvc.perform(get("/api/v1/cards/card-id/{id}", card.getId())
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());

        // Then
        resultActions.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(card.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.openNumber").value(card.getOpenNumber()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.encryptedNumber").value(card.getEncryptedNumber()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.expirationDate").value(card.getExpirationDate()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(card.getCardStatus().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance").value(card.getBalance().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user").value(card.getUser()));
    }


    @DisplayName("Добавление карты с правами администратора")
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void whenPostRequestToCardAndValidCard_thenCorrectResponse() throws Exception {
        MediaType applicationJson = new MediaType(MediaType.APPLICATION_JSON);
        String cardRequest = "{\"openNumber\":\"1111 2222 3333 4444\",\"expirationDate\":\"2025-10-29\",\"balance\":\"123.4\",\"userId\":1}";
        mockMvc.perform(post("/api/v1/cards/create-card")
                        .content(cardRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(applicationJson));
    }


    @DisplayName("Добавление карты с невалидным полем")
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void whenPostRequestToCardAndValidCard_thenInvalidResponse() throws Exception {
        MediaType applicationJson = new MediaType(MediaType.APPLICATION_JSON);
        String cardRequest = "{\"openNumber\":\"\",\"expirationDate\":\"2025-10-29\",\"balance\":\"123.4\",\"userId\":1}";
        mockMvc.perform(post("/api/v1/create-task")
                        .content(cardRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(applicationJson));
    }


    @DisplayName("Удаление карты")
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void whenDeleteRequestToCardAndValidCard_thenValidResponse() throws Exception {
        MediaType applicationJson = new MediaType(MediaType.APPLICATION_JSON);
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/cards/delete-card-id/28")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(applicationJson));
    }
}
