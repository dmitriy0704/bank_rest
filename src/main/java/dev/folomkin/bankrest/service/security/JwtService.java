package dev.folomkin.bankrest.service.security;


import dev.folomkin.bankrest.domain.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {

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

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}