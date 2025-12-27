package dev.folomkin.bankrest.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Утилитный класс для работы с JWT-токенами.
 * Отвечает за:
 * - генерацию токена после успешной аутентификации
 * - извлечение данных из токена (username, роли и т.д.)
 * - проверку валидности токена (подпись + срок действия)
 */
@Component // Делаем бином Spring, чтобы можно было инжектить в другие классы (фильтр, контроллер и т.д.)
public class JwtTokenProvider {

    // Секретный ключ для подписи токенов. Берётся из application.yml/properties
    @Value("${token.signing.key}")
    private String secret; // Например: очень-длинный-секрет-минимум-256-бит

    // Время жизни токена в миллисекундах (по умолчанию 24 часа, если не указано иначе)
    @Value("${jwt.expiration-ms:3600000}") // 1 час по умолчанию
    private long validityInMilliseconds;
    // Ключ для подписи и проверки (создаётся один раз при старте приложения)
    private SecretKey key;

    // Парсер JWT-токенов (thread-safe, создаётся один раз — важно для производительности)
    private JwtParser jwtParser;

    /**
     * Метод вызывается Spring автоматически после создания бина и инъекции свойств.
     * Здесь инициализируем ключ и парсер, чтобы не создавать их при каждом запросе.
     */
    @PostConstruct
    public void init() {
        // Создаём SecretKey из строки секрета. Используем UTF-8, чтобы избежать проблем с кодировкой.
        // Ключ должен быть минимум 256 бит (для HS256). Если секрет короткий — JJWT бросит исключение.
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        // Создаём парсер с верификацией подписи по нашему ключу
        this.jwtParser = Jwts.parser()
                .verifyWith(key)    // Указываем ключ для проверки подписи
                .build();           // Строим неизменяемый thread-safe парсер
    }


    /**
     * Генерирует JWT-токен на основе объекта Authentication (удобно вызывать после успешного логина)
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateToken(userDetails);
    }


    /**
     * Основной метод генерации токена по UserDetails
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // Извлекаем роли без префикса "ROLE_" (если он есть), чтобы в токене хранить чистые названия: USER, ADMIN и т.д.
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .toList();

        // Добавляем роли в claims под ключом "roles" как List<String>
        claims.put("roles", roles);

        Instant now = Instant.now();
        Instant expiry = now.plusMillis(validityInMilliseconds);

        // Строим JWT
        return Jwts.builder()
                .claims(claims)                     // Кастомные claims (включая роли)
                .subject(userDetails.getUsername()) // sub — username пользователя
                .issuedAt(Date.from(now))           // iat — время выдачи
                .expiration(Date.from(expiry))      // exp — время истечения
                .signWith(key)                      // Подписываем токен HS256 с нашим ключом
                .compact();                         // Собираем в строку: header.payload.signature
    }

    /** Извлекает username (subject) из токена */
    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }


    /** Универсальный метод для извлечения любого claim по функции */
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }


    /** Извлекает все claims из токена (с проверкой подписи) */
    public Claims extractAllClaims(String token) {
        // Если подпись неверная или токен истёк — здесь бросится JwtException
        return jwtParser.parseSignedClaims(token).getPayload();
    }



    /**
     * Проверяет валидность токена:
     * - правильная подпись
     * - не истёк срок действия
     * - корректный формат
     */
    public boolean isTokenValid(String token) {
        try {
            jwtParser.parseSignedClaims(token); // Если парсинг прошёл — токен валиден
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Можно добавить логирование: ExpiredJwtException, SignatureException и т.д.
            return false;
        }
    }


    /** Геттер для парсера — нужен JwtAuthenticationFilter, чтобы не создавать парсер заново */
    public JwtParser getJwtParser() {
        return jwtParser;
    }
}