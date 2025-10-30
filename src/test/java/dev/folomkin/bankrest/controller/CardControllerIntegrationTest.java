package dev.folomkin.bankrest.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import dev.folomkin.bankrest.domain.dto.card.CardRequest;
import dev.folomkin.bankrest.domain.dto.card.CardResponse;
import dev.folomkin.bankrest.domain.dto.user.UserResponse;
import dev.folomkin.bankrest.domain.model.CardStatus;
import dev.folomkin.bankrest.domain.model.Role;
import dev.folomkin.bankrest.domain.model.User;
import dev.folomkin.bankrest.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminJwtToken;


    @BeforeEach
    void setUp() throws Exception {

        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("email@gmail.com");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setRole(Role.ROLE_ADMIN);
        userRepository.save(admin);

        // 2️⃣ Получаем JWT токен через реальный /auth/login
        String loginJson = """
                    {"username": "admin", "password": "password"}
                """;

        var result = mockMvc.perform(post("/api/v1/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        var json = result.getResponse().getContentAsString();
        adminJwtToken = objectMapper.readTree(json).get("token").asText();
    }

//    @AfterEach
//    void tearDown() {
//        User user = userRepository.findByEmail("email@gmail.com");
//        if (user != null) {
//            userRepository.delete(user);
//        }
//    }

    @Test
    @Transactional
    @WithMockUser(roles = "ADMIN")
    void shouldCreateCardSuccessfully() throws Exception {
        User user = userRepository.findByEmail("email@gmail.com");
        CardRequest request = new CardRequest(
                "0000 0000 0000 9000",
                LocalDate.now(),
                BigDecimal.valueOf(123.4),
                user.getId()
        );
        UserResponse userResponse = new UserResponse(
                user.getId(),
                "username",
                "email@email.com"
        );
        CardResponse cardResponse = new CardResponse(
                null,
                "**** **** **** 9000",
                LocalDate.now(),
                CardStatus.ACTIVE,
                BigDecimal.valueOf(123.4),
                userResponse);
        mockMvc.perform(post("/api/v1/cards/create-card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminJwtToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.encryptedNumber").value(cardResponse.encryptedNumber()))
                .andExpect(jsonPath("$.expirationDate").value(cardResponse.expirationDate().toString()))
                .andExpect(jsonPath("$.cardStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.balance").value(123.4))
                .andExpect(jsonPath("$.owner.id").value(cardResponse.owner().id()));
    }
}

