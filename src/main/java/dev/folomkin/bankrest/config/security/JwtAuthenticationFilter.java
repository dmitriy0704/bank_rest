package dev.folomkin.bankrest.config.security;

import dev.folomkin.bankrest.utils.JwtTokenUtils;
import dev.folomkin.bankrest.service.user.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String HEADER_NAME = "Authorization";
    private final JwtTokenUtils jwtTokenUtils;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Получаем токен из заголовка
        var authHeader = request.getHeader(HEADER_NAME);
        if (StringUtils.isEmpty(authHeader) || !StringUtils.startsWith(authHeader, BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Обрезаем префикс и получаем имя пользователя из токена
        var jwt = authHeader.substring(BEARER_PREFIX.length());
        var username = jwtTokenUtils.extractUsername(jwt);

        if (StringUtils.isNotEmpty(username) && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userService
                        .userDetailsService()
                        .loadUserByUsername(username);
                // Если токен валиден, то аутентифицируем пользователя
                if (jwtTokenUtils.isTokenValid(jwt, userDetails)) {
                    SecurityContext context = SecurityContextHolder.createEmptyContext();

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    context.setAuthentication(authToken);
                    SecurityContextHolder.setContext(context);
                }


        }
        filterChain.doFilter(request, response);
    }
}

//@Component
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//    private final JwtParser jwtParser;  // Инжектируй или создай в конструкторе
//
//    public JwtAuthenticationFilter(String secret) {
//        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
//        this.jwtParser = Jwts.parser().verifyWith(key).build();
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, ...) {
//        String token = extractTokenFromHeader(request);  // Твоя логика извлечения
//        if (token != null) {
//            try {
//
//Jws<Claims> jws = jwtParser.parseSignedClaims(token);
//String username = jws.getBody().getSubject();
//List<GrantedAuthority> authorities = jws.getBody().get("roles", List.class)
//        .stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role))
//        .collect(Collectors.toList());
//Authentication auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
//SecurityContextHolder.getContext().setAuthentication(auth);
//            } catch (JwtException e) {
//                // Логируй и игнорируй
//            }
//        }
//        filterChain.doFilter(request, response);
//    }
//}
