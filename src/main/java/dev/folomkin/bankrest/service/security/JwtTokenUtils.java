package dev.folomkin.bankrest.service.security;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtTokenUtils {

    @Value("${token.signing.key}")
    private String SECRET;
    private static final long EXPIRATION_MS = 1000 * 60 * 60 * 24; // 1 сутки

    public String generateToken(UserDetails userDetails) {
        String username = userDetails.getUsername();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role ->
                        role.replace("ROLE_", ""))
                .toList();

        return generateToken(username, roles);
    }

    public String generateToken(String username, List<String> roles) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_MS);

        Map<String, Object> map = new HashMap<>();
        map.put("roles", roles);

        JwtBuilder builder = Jwts.builder()
                .subject(username)
                .claims(map)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()));

        return builder.compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }


    private Claims extractAllClaims(String token) {
        JwtParser parser = Jwts
                .parser()
                .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
                .build();
        return parser.parseSignedClaims(token).getPayload();
    }


// => Новая версия
//    public Claims getAllClaimsFromToken(String token) {
//        // Создаем SecretKey из секрета (для HS256)
//        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
//
//        // Строим парсер
//        JwtParser parser = Jwts.parser()
//                .verifyWith(key)  // Верификация подписи (для JWS)
//                .build();
//
//        try {
//            // Парсим как signed JWT (JWS)
//            Jws<Claims> jws = parser.parseSignedClaims(token);
//            return jws.getBody();  // Возвращаем claims (body)
//        } catch (JwtException e) {
//            // Обработка ошибок: неверный токен, истекший, подделка и т.д.
//            throw new IllegalArgumentException("Invalid JWT token: " + e.getMessage());
//        }
//    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}