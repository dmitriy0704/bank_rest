package dev.folomkin.bank_rest.repository;

import dev.folomkin.bank_rest.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
