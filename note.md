Отлично 👍 Давай соберём полноценный интеграционный тест **“с нуля”**, который:

* Поднимает **весь Spring Boot контекст** (`@SpringBootTest` + `@AutoConfigureMockMvc`)
* Использует **H2 in-memory базу**
* Создаёт пользователя ADMIN в тестовой базе
* Авторизует его через реальный эндпоинт `/api/v1/auth/login`
* Берёт JWT из ответа
* Использует этот JWT для вызова `/api/v1/cards/create-card`
* Проверяет, что возвращается JSON с нужным `Content-Type` и данными

---

## Пример интеграционного теста

```java
package dev.folomkin.bankrest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.folomkin.bankrest.dto.CardRequest;
import dev.folomkin.bankrest.dto.CardResponse;
import dev.folomkin.bankrest.entity.User;
import dev.folomkin.bankrest.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CardControllerIT {

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
        // 1. Создаём пользователя-админа в тестовой БД
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setRole("ROLE_ADMIN");
        userRepository.save(admin);

        // 2. Логинимся через реальный эндпоинт /auth/login
        var loginJson = """
            {"username":"admin","password":"password"}
        """;

        var result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        System.out.println(">>> LOGIN RESPONSE = " + response); // Посмотрим структуру

        // 3. Берём JWT из ответа
        var jsonNode = objectMapper.readTree(response);
        // Подставь правильное поле, которое реально возвращает твой login-эндпоинт
        adminJwtToken = jsonNode.get("accessToken").asText();
    }

    @Test
    void shouldCreateCardSuccessfully() throws Exception {
        // 4. Подготовка JSON запроса
        var request = new CardRequest();
        request.setOpenNumber("6318 0172 1681 9169");
        request.setExpirationDate(LocalDate.of(2025, 10, 29));
        request.setBalance(123.4);
        request.setUserId(1L);

        String json = objectMapper.writeValueAsString(request);

        // 5. Отправка запроса с JWT
        mockMvc.perform(post("/api/v1/cards/create-card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminJwtToken)
                        .content(json))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.openNumber").value("6318 0172 1681 9169"))
                .andExpect(jsonPath("$.balance").value(123.4))
                .andExpect(jsonPath("$.userId").value(1));
    }
}
```

---

### 🔑 Объяснение

1. **Создание тестового пользователя** (`@BeforeEach`):

    * Нужно, чтобы `@PreAuthorize("hasRole('ADMIN')")` пропустил запрос.
2. **Реальная авторизация через `/auth/login`**:

    * Возвращается JWT.
    * JWT сохраняем в переменной `adminJwtToken`.
3. **MockMvc запрос к `/create-card`**:

    * Передаём JWT в заголовке `Authorization`.
    * Проверяем `status().isCreated()` и `Content-Type`.
4. **`.andDo(print())`**:

    * Выводит реальный HTTP-ответ в консоль.
    * Помогает понять, что реально возвращает сервер, особенно полезно для отладки JWT.

---

### ⚙️ Совет

* Если твой эндпоинт логина возвращает поле не `accessToken`, а, например, `token` или `jwt`, замени строку:

```java
adminJwtToken = jsonNode.get("accessToken").asText();
```

на правильное имя поля.

* Убедись, что в тестовой БД есть `User` с ролью `ROLE_ADMIN`.

---

Если хочешь, я могу прислать **вариант с полностью “готовой” H2 базой + сущностями Card и User**, чтобы этот тест реально выполнялся без доработок.

Хочешь, чтобы я это сделал?
