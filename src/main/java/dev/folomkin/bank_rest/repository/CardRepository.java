package dev.folomkin.bank_rest.repository;

import dev.folomkin.bank_rest.domain.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, Long> {

}
