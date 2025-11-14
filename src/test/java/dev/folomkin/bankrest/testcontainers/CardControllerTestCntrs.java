package dev.folomkin.bankrest.testcontainers;


import dev.folomkin.bankrest.config.JwtTokenProvider;
import dev.folomkin.bankrest.domain.dto.card.CardRequest;
import dev.folomkin.bankrest.domain.dto.card.CardResponse;
import dev.folomkin.bankrest.domain.dto.user.UserResponse;
import dev.folomkin.bankrest.domain.model.Card;
import dev.folomkin.bankrest.domain.model.CardStatus;
import dev.folomkin.bankrest.domain.model.Role;
import dev.folomkin.bankrest.domain.model.User;
import dev.folomkin.bankrest.repository.UserRepository;
import dev.folomkin.bankrest.service.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import static io.restassured.RestAssured.when;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    private JwtTokenProvider jwtTokenProvider; // ← Авто-инжект из контекста

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
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    void shouldCreateAndGetCard() {

        // Given: Генерируем JWT-токен для user с ролью USER
        String token = jwtTokenProvider.createToken("testuser", Arrays.asList("ROLE_ADMIN")); // Или ROLE_USER, в зависимости от вашего формата

        // Headers с Bearer
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        restTemplate = restTemplate.withBasicAuth("", ""); // Очистка, если нужно

        var user = new User();
        user.setId(1L);
        user.setUsername("username");
        user.setPassword("password");
        user.setEmail("email@email.com");
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(Role.ROLE_USER);

        Card newCard = new Card(
                1L,
                "0000 0000 0000 0009",
                "**** **** **** 0009",
                LocalDate.now(),
                CardStatus.ACTIVE,
                BigDecimal.valueOf(123.4),
                user);

        // When: POST создание
        HttpEntity<Card> requestEntity = new HttpEntity<>(newCard, headers);
        ResponseEntity<Card> createResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/cards/create-card",
                HttpMethod.POST,
                requestEntity,
                Card.class
        );

        // Then: 200 OK
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Card savedCard = createResponse.getBody();
        assertThat(savedCard.getId()).isGreaterThan(1L);


        // GET с тем же токеном
        HttpEntity<Void> getEntity = new HttpEntity<>(headers);
        ResponseEntity<Card> getResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/cards/card-id/{cardId}" + savedCard.getId(),
                HttpMethod.GET,
                getEntity,
                Card.class
        );

        // Then: Проверяем получение
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
