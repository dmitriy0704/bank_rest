package dev.folomkin.bankrest.testcontainers;


import dev.folomkin.bankrest.demo_testcontiners.entity.SimpleEntity;
import dev.folomkin.bankrest.demo_testcontiners.repository.SimpleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest // Только JPA-слой (репо)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// Не заменять БД
@Testcontainers // Включает TestContainers
public class SimpleTestContainersTest {


    // Статический контейнер (один на все тесты)
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    // Динамически настраиваем свойства Spring (URL, user, pass от контейнера)
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop"); // Создаёт таблицы автоматически
    }

    @Autowired
    private SimpleRepository repository;



    @Test
    void debugConnection() {
        System.out.println("JDBC URL: " + postgres.getJdbcUrl());
        System.out.println("Username: " + postgres.getUsername());
        System.out.println("Password: " + postgres.getPassword());
        System.out.println("Exposed Port: " + postgres.getMappedPort(5432)); // Локальный порт
    }

    @Test
    void shouldSaveAndFindEntity() {
        // Given: Создаём сущность
        SimpleEntity entity = new SimpleEntity("Test Name");

        // When: Сохраняем
        SimpleEntity saved = repository.save(entity);

        // Then: Проверяем, что сохранено и найдено
        assertThat(saved.getId()).isGreaterThan(0L);
        assertThat(saved.getName()).isEqualTo("Test Name");

        // Получаем по ID
        SimpleEntity found = repository.findById(saved.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Test Name");
    }

}
