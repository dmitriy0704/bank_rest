package dev.folomkin.bankrest.service.user;

import dev.folomkin.bankrest.domain.dto.user.UserResponse;
import dev.folomkin.bankrest.domain.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService {

    List<UserResponse> getUsers();

    UserResponse getUserById(Long userId);

    User save(User user);

    User create(User user);

    UserDetailsService userDetailsService();

// User getByUsername(String username);
// User getCurrentUser();
// Page<UserResponse> findAllByFilter(PageRequest request);

    void getAdmin();

    void getUser();
}
