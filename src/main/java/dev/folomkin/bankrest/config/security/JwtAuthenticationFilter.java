package dev.folomkin.bankrest.config.security;


import dev.folomkin.bankrest.config.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Фильтр, который перехватывает каждый запрос и проверяет наличие валидного JWT-токена
 * в заголовке Authorization: Bearer <token>.
 * Если токен валиден — автоматически аутентифицирует пользователя в Spring Security.
 */
@Component // Бин Spring, чтобы можно было инжектить в SecurityConfig
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    // Инжектируем провайдер через конструктор (рекомендуемый способ в Spring)
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Получаем заголовок Authorization
        String header = request.getHeader("Authorization");

        // Если заголовка нет или он не начинается с "Bearer " — пропускаем (анонимный доступ)
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Обрезаем префикс "Bearer ", получаем чистый токен
        String token = header.substring(7);

        try {
            // Парсим токен с проверкой подписи (используем готовый парсер из провайдера)
            Jws<Claims> jws = jwtTokenProvider.getJwtParser().parseSignedClaims(token);
            Claims claims = jws.getPayload();

            String username = claims.getSubject();

            // Если username есть и пользователь ещё не аутентифицирован в этом запросе
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Извлекаем список ролей из claims (мы сохраняли их как List<String>)
                @SuppressWarnings("unchecked")
                List<String> roles = claims.get("roles", List.class);

                // Преобразуем роли в GrantedAuthority (Spring ожидает префикс ROLE_)
                var authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .toList();

                // Создаём объект аутентификации (principal = username, credentials = null, authorities)
                var authentication = new UsernamePasswordAuthenticationToken(
                        username, null, authorities);

                // Добавляем детали запроса (IP, sessionId и т.д.) — полезно для аудита
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Устанавливаем аутентификацию в SecurityContext — теперь Spring считает пользователя авторизованным
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception e) {
            // Любая ошибка при парсинге токена (истёк, подделан, неверный формат) — считаем пользователя анонимным
            // Здесь можно добавить логирование: logger.warn("Невалидный JWT: {}", e.getMessage());
        }

        // В любом случае передаём управление следующему фильтру в цепочке
        filterChain.doFilter(request, response);
    }


}