package dev.folomkin.bankrest.controller;


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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
public class CardControllerIT {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private ObjectMapper objectMapper;


    @MockitoBean
    private CardService cardService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    private String adminJwtToken;

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

    @BeforeEach
    void setUp() throws Exception {
        // создаем пользователя-админа в БД
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("email@gmail.com");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setRole(Role.ROLE_ADMIN);
        userRepository.save(admin);

        // авторизуемся и получаем JWT
        var loginJson = """
            {"username":"admin", "password":"password"}
        """;

        var result = mockMvc.perform(post("/api/v1/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        var response = result.getResponse().getContentAsString();
        var jsonNode = objectMapper.readTree(response);
        adminJwtToken = jsonNode.get("token").asText();
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
        ResultActions resultActions = mockMvc.perform(get("/api/v1/cards/card-id/{cardId}", card.getId())
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());

        // Then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(card.getId()))
                .andExpect(jsonPath("$.encryptedNumber").value(card.getEncryptedNumber()))
                .andExpect(jsonPath("$.expirationDate").value(card.getExpirationDate().toString()))
                .andExpect(jsonPath("$.balance").value(card.getBalance().toString()));
    }

//
//    @DisplayName("Добавление карты с правами администратора. Тест слоя контроллера.")
//    @Test
//    @WithMockUser(username = "admin", roles = {"ADMIN"})
//    void shouldCreateCardSuccessfully() throws Exception {
//        CardRequest request = new CardRequest(
//                "6318 0172 1681 9169",
//                LocalDate.of(2025, 10, 29),
//                BigDecimal.valueOf(123.4),
//                1L);
//
//        UserResponse userResponse = new UserResponse(
//                1L,
//                "username",
//                "email@email.com"
//        );
//
//        CardResponse response = new CardResponse(
//                10L,
//                "**** **** **** 9169",
//                LocalDate.of(2025, 10, 29),
//                CardStatus.ACTIVE,
//                BigDecimal.valueOf(123.4),
//                userResponse
//        );
//
//        User user = new User();
//        user.setId(1L);
//        user.setUsername("username");
//        user.setPassword("password");
//        user.setEmail("email@email.com");
//        user.setCreatedAt(LocalDateTime.now());
//        user.setRole(Role.ROLE_USER);
//
//        given(cardService.createCard(any(CardRequest.class), any(User.class))).willReturn(response);
//
//        String json = objectMapper.writeValueAsString(request);
//
//        mockMvc.perform(post("/api/v1/cards/create-card")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                .andDo(print())
//                .andExpect(status().isCreated())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.id").value(1))
//                .andExpect(jsonPath("$.openNumber").value("6318 0172 1681 9169"))
//                .andExpect(jsonPath("$.expirationDate").value("2025-10-29"))
//                .andExpect(jsonPath("$.balance").value(123.4))
//                .andExpect(jsonPath("$.userId").value(1));
//    }


    @DisplayName("Добавление карты с правами администратора")
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void whenPostRequestToCardAndValidCard_thenCorrectResponse() throws Exception {

        CardRequest request = new CardRequest(
                "6318 0172 1681 9169",
                LocalDate.of(2025, 10, 29),
                BigDecimal.valueOf(123.4),
                1L);

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/cards/create-card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminJwtToken)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.encryptedNumber").value("6318 0172 1681 9169"))
                .andExpect(jsonPath("$.balance").value(123.4))
                .andExpect(jsonPath("$.userId").value(1));


//        MediaType applicationJson = new MediaType(MediaType.APPLICATION_JSON);
//        String cardRequest = "{\"openNumber\":\"1111 2222 3333 4444\",\"expirationDate\":\"2025-10-29\",\"balance\":123.4,\"userId\":1}";
//        mockMvc.perform(post("/api/v1/cards/create-card")
//                        .content(cardRequest)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isCreated())
//                .andExpect(content()
//                        .contentType(applicationJson));
    }


    @DisplayName("Добавление карты с невалидным полем")
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void whenPostRequestToCardAndValidCard_thenInvalidResponse() throws Exception {
        MediaType applicationJson = new MediaType(MediaType.APPLICATION_JSON);
        String cardRequest = "{\"openNumber\":\"\",\"expirationDate\":\"2025-10-29\",\"balance\":123.4,\"userId\":1}";
        mockMvc.perform(post("/api/v1/cards/create-card")
                        .content(cardRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content()
                        .contentType(applicationJson));
    }


    @DisplayName("Удаление карты")
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void whenDeleteRequestToCardAndValidCard_thenValidResponse() throws Exception {
        MediaType applicationJson = new MediaType(MediaType.APPLICATION_JSON);
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/cards/delete-card-id/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content()
                        .contentType(applicationJson));
    }
}
