package dev.folomkin.bank_rest.service.user;

import dev.folomkin.bank_rest.domain.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService {

//    List<User> findAll();

//    Page<UserResponse> findAllByFilter(PageRequest request);

//    Optional<User> getUserById(Long userId);

    User save(User user);

    User create(User user);

//    User getByUsername(String username);

    UserDetailsService userDetailsService();

//    User getCurrentUser();

    void getAdmin();
    void getUser();
}
