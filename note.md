Отличный вопрос, Дмитрий 💪 — это именно тот шаг, который отличает **юнит-тесты** от **реально интеграционных**.
`Testcontainers` — это мощный инструмент для тестирования **в реальной среде**, но при этом **изолированно**.
Разберём всё по шагам 👇

---

## 🚀 Что такое Testcontainers

**Testcontainers** — библиотека для Java (и Spring Boot),
которая позволяет запускать **настоящие Docker-контейнеры** прямо из тестов.
Например:

* PostgreSQL, MySQL, MongoDB, Redis
* Kafka, RabbitMQ
* Keycloak, MinIO, ElasticSearch
* и многое другое

📦 Всё поднимается автоматически перед тестом
и останавливается после завершения — никакого мусора в системе.

---

## 🧠 Что им можно тестировать

Testcontainers нужен для **интеграционных тестов**, где ты хочешь убедиться,
что твой код реально работает с инфраструктурой, а не с моками.

| Цель теста                                 | Подходит Testcontainers? | Пример                       |
| ------------------------------------------ | ------------------------ | ---------------------------- |
| Проверить логику контроллера               | ❌ Нет (MockMvc)          | мокнутые сервисы             |
| Проверить слой сервисов                    | ⚙️ Иногда                | если там SQL-запросы         |
| Проверить JPA/Repository слой              | ✅ Да                     | реальные SQL-запросы         |
| Проверить интеграцию с Kafka, MinIO, Redis | ✅ Да                     | брокеры, очереди и хранилища |
| Проверить реальную работу REST API + БД    | ✅✅ Да                    | full-stack тест              |

---

## 🧩 Пример: PostgreSQL + Spring Boot

Допустим, у тебя приложение использует PostgreSQL.
Ты хочешь убедиться, что репозитории реально работают с SQL (а не с H2).

---

### 1️⃣ Добавь зависимости

В `build.gradle` (или `pom.xml`):

```groovy
testImplementation 'org.testcontainers:junit-jupiter'
testImplementation 'org.testcontainers:postgresql'
```

---

### 2️⃣ Напиши тест с контейнером

```java
@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CardRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private CardRepository cardRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void shouldSaveAndFindCard() {
        CardEntity card = new CardEntity();
        card.setOpenNumber("1111 2222 3333 4444");
        card.setExpirationDate(LocalDate.of(2025, 10, 29));
        card.setBalance(123.45);
        cardRepository.save(card);

        var found = cardRepository.findById(card.getId());
        assertTrue(found.isPresent());
        assertEquals("1111 2222 3333 4444", found.get().getOpenNumber());
    }
}
```

---

### 🔍 Что здесь происходит

| Что                                          | Зачем                                                  |
| -------------------------------------------- | ------------------------------------------------------ |
| `@Testcontainers`                            | говорит JUnit, что мы используем контейнеры            |
| `@Container`                                 | создаёт и управляет PostgreSQL-контейнером             |
| `DynamicPropertySource`                      | подставляет параметры подключения в Spring Boot        |
| `@AutoConfigureTestDatabase(replace = NONE)` | отключает H2, чтобы использовать контейнер             |
| `CardRepository`                             | реальный Spring Data репозиторий                       |
| В тесте                                      | мы реально сохраняем и читаем из PostgreSQL контейнера |

---

## 🧱 Что можно потестировать с Testcontainers

| Тип теста                   | Пример                                                          |
| --------------------------- | --------------------------------------------------------------- |
| **Repository-тесты**        | Проверить работу запросов (`@Query`, join’ы, фильтры)           |
| **Service-тесты**           | Проверить бизнес-логику, которая реально ходит в БД             |
| **Full Integration (REST)** | Поднять весь Spring Boot контекст и тестировать `/api/v1/cards` |
| **Kafka тесты**             | Проверить, что сообщения реально публикуются/читаются           |
| **MinIO/S3 тесты**          | Проверить загрузку и скачивание файлов                          |
| **Keycloak тесты**          | Проверить авторизацию по JWT                                    |

---

## ⚡ Пример Full Integration Test (REST + PostgreSQL)

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CardControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CardRepository cardRepository;

    @Test
    void shouldCreateCardInRealDatabase() throws Exception {
        String json = """
            {
              "openNumber": "6318 0172 1681 9169",
              "expirationDate": "2025-10-29",
              "balance": 123.4,
              "userId": 1
            }
        """;

        mockMvc.perform(post("/api/v1/cards/create-card")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isCreated());

        assertEquals(1, cardRepository.count());
    }
}
```

📦 Этот тест:

* запускает **реальный PostgreSQL контейнер**,
* стартует **весь Spring Boot контекст**,
* выполняет реальный HTTP POST через MockMvc,
* проверяет, что запись реально сохранилась в базе.

---

## 🧠 Итого

| Цель                                         | Какой подход                         |
| -------------------------------------------- | ------------------------------------ |
| Проверить контроллеры                        | `@WebMvcTest` + моки                 |
| Проверить логику и БД                        | `@SpringBootTest` + Testcontainers   |
| Проверить конкретные запросы JPA             | `@DataJpaTest` + Testcontainers      |
| Проверить интеграцию с Kafka / Redis / MinIO | `@SpringBootTest` + нужный контейнер |

---

Хочешь, я покажу, как подключить **несколько контейнеров** сразу — например PostgreSQL + Redis, и синхронизировать их в одном интеграционном тесте?
