package dev.folomkin.bankrest.repository;

import dev.folomkin.bankrest.domain.dto.card.CardRequest;
import dev.folomkin.bankrest.domain.dto.user.UserResponse;
import dev.folomkin.bankrest.domain.model.Card;
import dev.folomkin.bankrest.domain.model.CardStatus;
import dev.folomkin.bankrest.domain.model.Role;
import dev.folomkin.bankrest.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest // Только JPA-слой (репо + entity)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Не заменять БД
class CardRepositorySliceIT {

    @Autowired
    private CardRepository cardRepository;

    private CardRequest request;
    private UserResponse userResponse;
    private User user;
    private Card card;

    @Test
    void shouldSaveAndFindCard() {
        user = new User();
        user.setUsername("username");
        user.setPassword("password");
        user.setEmail("email@email.com");
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(Role.ROLE_ADMIN);

        card = new Card(
                null,
                "0000 0000 0000 0009",
                "**** **** **** 0009",
                LocalDate.now(),
                CardStatus.ACTIVE,
                BigDecimal.valueOf(123.4),
                user);

        // Given
        cardRepository.save(card);

        // When
        Optional<Card> found = cardRepository.findById(card.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getOpenNumber()).isEqualTo("0000 0000 0000 0009");
    }

    @Test
    void shouldReturnEmptyForNonExistingCard() {
        // When
        Optional<Card> found = cardRepository.findById(999L);

        // Then
        assertThat(found).isEmpty();
    }

        @Test
        void shouldSaveCard() {
            // Только репозиторий + БД
            Card saved = cardRepository.save(new Card("1234 5678"));
            assertThat(saved.getId()).isGreaterThan(0L);
    }
}