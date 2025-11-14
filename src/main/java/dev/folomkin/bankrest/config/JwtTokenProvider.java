package dev.folomkin.bankrest.config;


import io.jsonwebtoken.JwtBuilder;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;


import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class JwtTokenProvider {


    @Value("${token.signing.key}")
    private String secretKey;

    private static final long validityInMilliseconds = 1000 * 60 * 60 * 24;

    private Key key;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }
    // Генерация токена из Authentication
    public String createToken(Authentication authentication) {
        String username = authentication.getName();
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.replace("ROLE_", "")) // Убираем префикс, если нужно
                .collect(Collectors.toList());

        return createToken(username, roles);
    }

    // Основной метод: Сначала кастомные claims, потом subject!
    public String createToken(String username, List<String> roles) {
        // Создаём Claims и добавляем кастомные ПЕРЕД setSubject
        Claims claims = Jwts.claims().build();
        claims.put("roles", roles); // ← Сначала put() — mutable на этом этапе

//        claims.setSubject(username); // ← Потом setSubject — теперь immutable, но builder скопирует

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()

                .setClaims(claims) // Передаём готовые claims в builder
                .subject(username)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Валидация токена (без изменений)
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Извлечение username (без изменений)
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    // Создание Authentication из токена (с фиксом для ролей)
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();

        List<String> roles = claims.get("roles", List.class);
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(claims.getSubject())
                .authorities(roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role)) // Добавляем префикс ROLE_
                        .collect(Collectors.toList()))
                .build();

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

}
