package dev.folomkin.bankrest.service.user;

import dev.folomkin.bankrest.domain.dto.user.UserResponse;
import dev.folomkin.bankrest.domain.mapper.UserMapper;
import dev.folomkin.bankrest.domain.model.Role;
import dev.folomkin.bankrest.domain.model.User;
import dev.folomkin.bankrest.exceptions.AuthExistUserException;
import dev.folomkin.bankrest.exceptions.NoSuchElementException;
import dev.folomkin.bankrest.repository.UserRepository;
import dev.folomkin.bankrest.service.RoleService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleService roleService;



    /**
     * Получение списка пользователей
     *
     * @return Список пользователей.
     * UserResponse: Пользователь с ограниченным количеством данных
     */
    @Override
    public List<UserResponse> getUsers() {
        return userMapper.toUserResponseList(userRepository.findAll());
    }


    /**
     * Получение списка пользователей с пагинацией
     *
     * @return Список пользователей
     */
    @Override
    public Page<UserResponse> getUsersPages(PageRequest pageRequest) {
        List<User> users = userRepository.findAll(pageRequest).getContent();
        return new PageImpl<>(userMapper.toUserResponseList(users), pageRequest, users.size());
    }


    /**
     * Получение пользователя по id
     *
     * @return Пользователь
     */
    @Override
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NoSuchElementException("Пользователь с id: " + userId + " не найден")
        );
        return userMapper.toUserResponse(user);
    }

    /**
     * Сохранение пользователя
     *
     * @return сохраненный пользователь
     */
    public User save(User user) {
        return userRepository.save(user);
    }


    /**
     * Создание пользователя
     *
     * @return созданный пользователь
     */
    public User create(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new AuthExistUserException("Пользователь с таким именем уже существует");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new AuthExistUserException("Пользователь с таким email уже существует");
        }
        return save(user);
    }

    /**
     * Получение пользователя по имени пользователя
     *
     * @return пользователь
     */
    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = getByUsername(username);
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                true, true, true, true, // accountNonExpired, credentialsNonExpired, accountNonLocked
                user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList())
        );
    }

    /**
     * Получение текущего пользователя
     *
     * @return текущий пользователь
     */
    public User getCurrentUser() {
        // Получение имени пользователя из контекста Spring Security
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        return getByUsername(username);
    }

    /**
     * Выдача прав администратора текущему пользователю
     * <p>
     * Нужен для демонстрации
     */
    public void getAdmin() {
        var user = getCurrentUser();
        user.setRoles(List.of(roleService.getUserRole()));
        save(user);
    }


    /**
     * Выдача прав администратора текущему пользователю
     * <p>
     * Нужен для демонстрации
     */
    public void getUser() {
        var user = getCurrentUser();
        user.setRoles(List.of(roleService.getUserRole()));
        save(user);
    }


}
