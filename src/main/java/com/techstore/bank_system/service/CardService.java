package com.techstore.bank_system.service;

import com.techstore.bank_system.entity.Account;
import com.techstore.bank_system.entity.Card;
import com.techstore.bank_system.entity.User;
import com.techstore.bank_system.repository.AccountRepository;
import com.techstore.bank_system.repository.CardRepository;
import com.techstore.bank_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class CardService {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    public Card createCard(Long userId, Long accountId) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Account> accountOpt = accountRepository.findById(accountId);

        if (userOpt.isEmpty() || accountOpt.isEmpty()) {
            throw new RuntimeException("Пользователь или счёт не найден");
        }

        User user = userOpt.get();
        Account account = accountOpt.get();

        Card card = Card.builder()
                .cardNumber(generateCardNumber())
                .cardHolder(user.getFirstName() + " " + user.getLastName())
                .cvv(generateCVV())
                .expiryDate(LocalDate.now().plusYears(4))
                .isActive(true)
                .isBlocked(false)
                .createdAt(LocalDateTime.now())
                .user(user)
                .account(account)
                .build();

        return cardRepository.save(card);
    }

    @Transactional
    public Card blockCard(Long cardId) {
        Optional<Card> cardOpt = cardRepository.findById(cardId);
        if (cardOpt.isEmpty()) {
            throw new RuntimeException("Карта не найдена");
        }

        Card card = cardOpt.get();
        card.setIsBlocked(true);
        card.setBlockedAt(LocalDateTime.now());
        return cardRepository.save(card);
    }

    @Transactional
    public Card unblockCard(Long cardId) {
        Optional<Card> cardOpt = cardRepository.findById(cardId);
        if (cardOpt.isEmpty()) {
            throw new RuntimeException("Карта не найдена");
        }

        Card card = cardOpt.get();
        card.setIsBlocked(false);
        card.setBlockedAt(null);
        return cardRepository.save(card);
    }

    @Transactional
    public Card deactivateCard(Long cardId) {
        Optional<Card> cardOpt = cardRepository.findById(cardId);
        if (cardOpt.isEmpty()) {
            throw new RuntimeException("Карта не найдена");
        }

        Card card = cardOpt.get();
        card.setIsActive(false);
        return cardRepository.save(card);
    }

    public List<Card> getUserCards(Long userId) {
        return cardRepository.findByUserId(userId);
    }

    public List<Card> getActiveUserCards(Long userId) {
        return cardRepository.findActiveCards(userId);
    }

    public boolean isCardValid(String cardNumber) {
        return cardRepository.findByCardNumber(cardNumber)
                .map(c -> c.getIsActive() && !c.getIsBlocked() && c.getExpiryDate().isAfter(LocalDate.now()))
                .orElse(false);
    }

    private String generateCardNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(random.nextInt(10));
        }

        String cardNumber = sb.toString();
        if (cardRepository.findByCardNumber(cardNumber).isPresent()) {
            return generateCardNumber();
        }

        return cardNumber;
    }

    private String generateCVV() {
        Random random = new Random();
        return String.format("%03d", random.nextInt(1000));
    }
}
