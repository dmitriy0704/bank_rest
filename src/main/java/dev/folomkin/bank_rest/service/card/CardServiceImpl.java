package dev.folomkin.bank_rest.service.card;

import dev.folomkin.bank_rest.domain.dto.card.CardRequest;
import dev.folomkin.bank_rest.domain.dto.card.CardResponse;
import dev.folomkin.bank_rest.domain.mapper.CardMapper;
import dev.folomkin.bank_rest.domain.model.Card;
import dev.folomkin.bank_rest.domain.model.User;
import dev.folomkin.bank_rest.repository.CardRepository;
import dev.folomkin.bank_rest.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CardServiceImpl implements CardService {

    private CardRepository cardRepository;
    private UserRepository userRepository;
    private CardMapper cardMapper;

    @Override
    @Transactional
    public CardResponse create(CardRequest cardRequest, User currentUser) {

        Card card = new Card();
        card.setOpenNumber(cardRequest.openNumber());
        card.setBalance(cardRequest.balance());

        User userToAssign;

        if (cardRequest.userId() != null) {
            userToAssign = userRepository.findById(cardRequest.userId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        } else {
            userToAssign = currentUser;
        }

        card.setUser(userToAssign);

        Card saved = cardRepository.save(card);
        return cardMapper.toCardResponse(saved);
    }
}
