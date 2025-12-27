package dev.folomkin.bankrest.service.security;

import dev.folomkin.bankrest.config.JwtTokenProvider;
import dev.folomkin.bankrest.domain.dto.security.JwtResponse;
import dev.folomkin.bankrest.domain.dto.security.LoginRequest;
import dev.folomkin.bankrest.domain.dto.user.UserRequest;
import dev.folomkin.bankrest.domain.dto.user.UserResponse;
import dev.folomkin.bankrest.domain.model.User;
import dev.folomkin.bankrest.service.RoleService;
import dev.folomkin.bankrest.service.user.UserService;
//import dev.folomkin.bankrest.utils.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RoleService roleService;

    /**
     * Регистрация пользователя
     *
     * @param request данные пользователя
     * @return токен
     */
    public UserResponse register(UserRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRoles(List.of(roleService.getUserRole()));
        User saved = userService.create(user);

        UserResponse userResponse = new UserResponse();
        userResponse.setId(saved.getId());
        userResponse.setUsername(saved.getUsername());
        userResponse.setEmail(saved.getEmail());
        userResponse.setRoles(List.of(roleService.getUserRole()));
        userResponse.setCreatedA(saved.getCreatedAt());
        return userResponse;
    }

    /**
     * Аутентификация пользователя
     *
     * @param request данные пользователя
     * @return токен
     */
    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager
                .authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsername(),
                                request.getPassword()
                        ));

        var jwt = jwtTokenProvider.generateToken(authentication);
        return new JwtResponse(jwt);
    }
}
