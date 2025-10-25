package dev.folomkin.bankrest.repository;

import dev.folomkin.bankrest.domain.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findAll();

    @Query("SELECT c FROM Card c WHERE c.encryptedNumber LIKE %:last4")
    Card findCardByLast4(@Param("last4") String last4);

    List<Card> findAllCardsByUserId(Long userId);

    @Query("SELECT c FROM Card c WHERE c.cardStatus = 'BLOCKREQUEST' ")
    List<Card> findAllCardsByBlockRequest();
}
