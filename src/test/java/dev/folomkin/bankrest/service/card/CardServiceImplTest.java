package dev.folomkin.bankrest.service.card;

import dev.folomkin.bankrest.domain.dto.card.CardRequest;
import dev.folomkin.bankrest.domain.dto.card.CardResponse;
import dev.folomkin.bankrest.domain.dto.user.UserResponse;
import dev.folomkin.bankrest.domain.model.Card;
import dev.folomkin.bankrest.domain.model.CardStatus;
import dev.folomkin.bankrest.domain.model.Role;
import dev.folomkin.bankrest.domain.model.User;
import dev.folomkin.bankrest.exceptions.NoSuchElementException;
import dev.folomkin.bankrest.repository.CardRepository;
import dev.folomkin.bankrest.utils.CardSaveServiceUtil;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardSaveServiceUtil cardSaveServiceUtil;

    @InjectMocks
    private CardServiceImpl cardService;

    private CardRequest request;
    private UserResponse userResponse;
    private User user;
    private Card card;

    @BeforeEach
    void setUp() {
        // Подготовка тестовых данных
        request = new CardRequest(
                "0000 0000 0000 0009",
                LocalDate.of(2026, 10, 30),
                BigDecimal.valueOf(123.4),
                1L
        );

        userResponse = new UserResponse(
                1L,
                "username",
                "email@gmail.com"
        );

        user = new User();
        user.setId(1L);
        user.setUsername("username");
        user.setPassword("password");
        user.setEmail("email@email.com");
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(Role.ROLE_USER);

        card = new Card(
                1L,
                "0000 0000 0000 0009",
                "**** **** **** 0009",
                LocalDate.now(),
                CardStatus.ACTIVE,
                BigDecimal.valueOf(123.4),
                user);
    }

    @Test
    void createCard_ShouldReturnCardResponse_WhenValidRequest() {
        ///-> Given: Мокируем зависимости
        CardResponse expectedResponse = new CardResponse(
                1L,
                "**** **** **** 0009",
                LocalDate.now(),
                CardStatus.ACTIVE,
                BigDecimal.valueOf(123.4),
                userResponse);

        when(cardSaveServiceUtil.saveCard(request, user)).thenReturn(expectedResponse);

        ///-> When: Вызов сервиса (делегирует в мок)
        CardResponse response = cardService.createCard(request, user);

        ///-> Then: Проверки
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.encryptedNumber()).isEqualTo("**** **** **** 0009");
        assertThat(response.balance()).isEqualTo(BigDecimal.valueOf(123.4));
        assertThat(response.owner().id()).isEqualTo(1L);
    }

    @Test
    void deleteCardById_ShouldDeleteCard_WhenCardExists() {
        // Given: Мокаем, что карта найдена
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        // When: Вызов метода сервиса
        cardService.deleteCardById(1L);

        // Then: Проверяем, что delete вызван
        verify(cardRepository).delete(card);
        verifyNoMoreInteractions(cardRepository); // Нет лишних вызовов
    }


    @Test
    void deleteCardById_ShouldThrowException_WhenCardNotFound() {
        // Given: Мокаем, что карта не найдена
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then: Ожидаем exception
        assertThatThrownBy(() -> cardService.deleteCardById(1L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Карта с id 1 не найдена");
    }

// Если id - null
//    @Test
//    void deleteCardById_ShouldNotInteractWithRepository_WhenInvalidId() {
//        // Given: Null ID (edge case)
//        // When & Then: Exception, но репозиторий не вызван
//        assertThatThrownBy(() -> cardService.deleteCardById(null))
//                .isInstanceOf(IllegalArgumentException.class) // Или ваша custom exception
//                .hasMessageContaining("ID cannot be null");
//
//        verifyNoInteractions(cardRepository); // Репо не тронуто
//    }

}