package dev.folomkin.bankrest.testcontainers;


import dev.folomkin.bankrest.domain.dto.card.CardRequest;
import dev.folomkin.bankrest.domain.dto.card.CardResponse;
import dev.folomkin.bankrest.domain.model.Card;
import dev.folomkin.bankrest.domain.model.CardStatus;
import dev.folomkin.bankrest.domain.model.Role;
import dev.folomkin.bankrest.domain.model.User;
import dev.folomkin.bankrest.repository.UserRepository;
import dev.folomkin.bankrest.service.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties =
        "spring.security.enabled=false")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class CardControllerTestCntrs {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtService jwtService; // ← Авто-инжект из контекста

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("jwt.secret", () -> "myTestSecretKey1234567890abcdef");
    }


    @Test
    void shouldCreateAndGetCard() throws JsonProcessingException {
        var user = new User();
        user.setUsername("John Doe");
        user.setPassword("password");
        user.setEmail("email@email.com");
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(Role.ROLE_ADMIN);
        userRepository.save(user);

        log.info("Сохранился пользователь с Id {}", user.getId());

        // Given: Генерируем JWT-токен для user с ролью USER
        String token = jwtService.generateToken("John Doe", List.of("ROLE_ADMIN")); // Или ROLE_USER, в зависимости от вашего формата

        // Headers с Bearer
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON); // Для JSON body
        restTemplate = restTemplate.withBasicAuth("", ""); // Очистка, если нужно

        CardRequest cardRequest = new CardRequest(
                "0000 0000 0000 0009",
                LocalDate.of(2026, 10, 30), // Future дата для @Future
                BigDecimal.valueOf(123.4),
                user.getId() // ← userId из saved user
        );
        String json = objectMapper.writeValueAsString(cardRequest); // JSON для body
        log.info("Request JSON: {}", json); // Debug

        // When: POST создание
        HttpEntity<String> requestEntity = new HttpEntity<>(json, headers);
        ResponseEntity<CardResponse> createResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/cards/create-card",
                HttpMethod.POST,
                requestEntity,
                CardResponse.class
        );

        // Then: 200 OK
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED); // Или OK
        CardResponse savedCard = createResponse.getBody();
        assertThat(savedCard).isNotNull();
        Assertions.assertNotNull(savedCard);
        assertThat(savedCard.id()).isGreaterThan(0L); // Генерируется БД

        // GET с тем же токеном
        HttpEntity<Void> getEntity = new HttpEntity<>(headers);
        ResponseEntity<CardResponse> getResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/cards/card-id/" + savedCard.id(),
                HttpMethod.GET,
                getEntity,
                CardResponse.class
        );

        // Then: Проверяем получение
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
