package dev.folomkin.bankrest.service.user;

import dev.folomkin.bankrest.domain.dto.user.UserResponse;
import dev.folomkin.bankrest.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService {

    List<UserResponse> getUsers();

    UserResponse getUserById(Long userId);

    User save(User user);

    User create(User user);

    void getAdmin();

    void getUser();

    Page<UserResponse> getUsersPages(PageRequest request);
}
