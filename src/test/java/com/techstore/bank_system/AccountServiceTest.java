package com.techstore.bank_system;

import com.techstore.bank_system.entity.Account;
import com.techstore.bank_system.entity.AccountType;
import com.techstore.bank_system.entity.CurrencyType;
import com.techstore.bank_system.entity.User;
import com.techstore.bank_system.repository.AccountRepository;
import com.techstore.bank_system.repository.UserRepository;
import com.techstore.bank_system.service.AccountService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * JUnit 5 + Mockito тесттер — AccountService үшін.
 *
 * Банктік шот операцияларын тестілеу:
 *  - Жаңа шот ашу
 *  - Пайдаланушы жоқ болса ерекше жағдай
 *  - Шоттар тізімін алу
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService — банктік шот тесттері")
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountService accountService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("client@bank.kz")
                .firstName("Ақерке")
                .lastName("Нұрова")
                .isActive(true)
                .build();
    }

    // ────────────────────────────────────────────────
    // ТЕСТ 1: шот ашу — сәтті
    // ────────────────────────────────────────────────
    @Test
    @DisplayName("1. createAccount() — жаңа шот сәтті ашылуы тиіс")
    void testCreateAccount_Success() {
        // Arrange
        Account expectedAccount = Account.builder()
                .id(1L)
                .accountNumber("ACC-00000001")
                .accountType(AccountType.CHECKING)
                .currency(CurrencyType.RUB)
                .balance(new BigDecimal("5000.00"))
                .availableBalance(new BigDecimal("5000.00"))
                .isActive(true)
                .user(testUser)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepository.save(any(Account.class))).thenReturn(expectedAccount);

        // Act
        Account result = accountService.createAccount(
                1L,
                AccountType.CHECKING,
                CurrencyType.RUB,
                new BigDecimal("5000.00")
        );

        // Assert
        assertNotNull(result, "Нәтиже null болмауы тиіс");
        assertEquals("ACC-00000001", result.getAccountNumber());
        assertEquals(AccountType.CHECKING, result.getAccountType());
        assertEquals(CurrencyType.RUB, result.getCurrency());
        assertEquals(new BigDecimal("5000.00"), result.getBalance());

        // Verify: save бір рет шақырылды
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    // ────────────────────────────────────────────────
    // ТЕСТ 2: пайдаланушы жоқ — ерекше жағдай
    // ────────────────────────────────────────────────
    @Test
    @DisplayName("2. createAccount() — пайдаланушы жоқ — RuntimeException шығуы тиіс")
    void testCreateAccount_UserNotFound_ShouldThrow() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Assert — assertThrows: ерекше жағдай шығуын тексеру
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> accountService.createAccount(999L, AccountType.SAVINGS, CurrencyType.USD, BigDecimal.ZERO),
                "Пайдаланушы жоқ болса RuntimeException шығуы тиіс"
        );

        assertNotNull(ex.getMessage());
        // Verify: save ешқашан шақырылмауы тиіс
        verify(accountRepository, never()).save(any());
    }

    // ────────────────────────────────────────────────
    // ТЕСТ 3: шоттар тізімі
    // ────────────────────────────────────────────────
    @Test
    @DisplayName("3. getUserAccounts() — пайдаланушы шоттар тізімін қайтаруы тиіс")
    void testGetUserAccounts_ShouldReturnList() {
        // Arrange
        List<Account> mockAccounts = List.of(
                Account.builder().id(1L).accountNumber("ACC-00000001").user(testUser).build(),
                Account.builder().id(2L).accountNumber("ACC-00000002").user(testUser).build()
        );

        when(accountRepository.findByUserId(1L)).thenReturn(mockAccounts);

        // Act
        List<Account> result = accountService.getUserAccounts(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size(), "2 шот қайтарылуы тиіс");
        assertEquals("ACC-00000001", result.get(0).getAccountNumber());
        assertEquals("ACC-00000002", result.get(1).getAccountNumber());
    }

    // ────────────────────────────────────────────────
    // ТЕСТ 4: бос тізім
    // ────────────────────────────────────────────────
    @Test
    @DisplayName("4. getUserAccounts() — шоты жоқ пайдаланушы — бос тізім")
    void testGetUserAccounts_NoAccounts_ShouldReturnEmpty() {
        // Arrange
        when(accountRepository.findByUserId(99L)).thenReturn(List.of());

        // Act
        List<Account> result = accountService.getUserAccounts(99L);

        // Assert
        assertNotNull(result, "Тізім null болмауы тиіс");
        assertTrue(result.isEmpty(), "Тізім бос болуы тиіс");
    }

    // ────────────────────────────────────────────────
    // ТЕСТ 5: бастапқы салым 0 болса
    // ────────────────────────────────────────────────
    @Test
    @DisplayName("5. createAccount() — нөл салыммен шот ашу")
    void testCreateAccount_ZeroDeposit() {
        // Arrange
        Account accountWithZero = Account.builder()
                .id(3L)
                .accountNumber("ACC-00000003")
                .balance(BigDecimal.ZERO)
                .availableBalance(BigDecimal.ZERO)
                .isActive(true)
                .user(testUser)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepository.save(any(Account.class))).thenReturn(accountWithZero);

        // Act
        Account result = accountService.createAccount(1L, AccountType.SAVINGS, CurrencyType.USD, null);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getBalance(), "Нөл салыммен баланс нөл болуы тиіс");
    }
}

