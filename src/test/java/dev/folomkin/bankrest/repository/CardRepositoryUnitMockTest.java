package dev.folomkin.bankrest.repository;

import dev.folomkin.bankrest.domain.dto.card.CardRequest;
import dev.folomkin.bankrest.domain.dto.user.UserResponse;
import dev.folomkin.bankrest.domain.model.Card;
import dev.folomkin.bankrest.domain.model.CardStatus;
import dev.folomkin.bankrest.domain.model.Role;
import dev.folomkin.bankrest.domain.model.User;
import dev.folomkin.bankrest.service.card.CardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class CardRepositoryUnitMockTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardServiceImpl cardService;

    private CardRequest request;
    private UserResponse userResponse;
    private User user;
    private Card card;

    @Test
    void shouldFindCardById() {

         // Given
        user = new User();
        user.setUsername("username");
        user.setPassword("password");
        user.setEmail("email@email.com");
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(Role.ROLE_ADMIN);

        card = new Card(
                1L,
                "0000 0000 0000 0009",
                "**** **** **** 0009",
                LocalDate.now(),
                CardStatus.ACTIVE,
                BigDecimal.valueOf(123.4),
                user);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        // When
        Optional<Card> found = cardRepository.findById(card.getId());

        // Then
        assertThat(found).isPresent();
        verify(cardRepository).findById(1L);
    }
}