package dev.folomkin.bankrest.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@NoArgsConstructor
@Table(name = "cards")
public class Card implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cards_id_seq")
    @SequenceGenerator(name = "cards_id_seq", sequenceName = "cards_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "open_number", unique = true, nullable = false)
    private String openNumber;

    @Column(name = "encrypted_number", unique = true, nullable = false)
    private String encryptedNumber;

    @CreationTimestamp
    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Schema(description = "Статус", example = "ACTIVE, BLOCKED, ISEXPIRATEDDATE")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CardStatus cardStatus;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    public Card(
            Long id,
            String openNumber,
            String encryptedNumber,
            LocalDate expirationDate,
            CardStatus cardStatus,
            BigDecimal balance,
            User user) {
        this.id = id;
        this.openNumber = openNumber;
        this.encryptedNumber = encryptedNumber;
        this.expirationDate = expirationDate;
        this.cardStatus = cardStatus;
        this.balance = balance;
        this.user = user;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOpenNumber() {
        return openNumber;
    }

    public void setOpenNumber(String openNumber) {
        this.openNumber = openNumber;
    }

    public String getEncryptedNumber() {
        return encryptedNumber;
    }

    public void setEncryptedNumber(String encryptedNumber) {
        this.encryptedNumber = encryptedNumber;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public CardStatus getCardStatus() {
        return cardStatus;
    }

    public void setCardStatus(CardStatus cardStatus) {
        this.cardStatus = cardStatus;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
