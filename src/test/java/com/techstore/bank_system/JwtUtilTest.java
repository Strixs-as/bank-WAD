package com.techstore.bank_system;

import com.techstore.bank_system.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 тесттер — JwtUtil класы үшін.
 *
 * JUnit негізгі аннотациялары:
 *  @Test        — тест әдісі
 *  @BeforeEach  — әр тесттен БҰРЫН орындалады
 *  @DisplayName — тестке оқылатын атау береді
 *
 * Assertions (тексеру әдістері):
 *  assertEquals()    — күтілген = нақты
 *  assertNotNull()   — null емес
 *  assertTrue()      — шарт ақиқат
 *  assertFalse()     — шарт жалған
 *  assertThrows()    — ерекше жағдай шықты
 */
@DisplayName("JwtUtil — JWT токен тесттері")
class JwtUtilTest {

    // Тест объектісі
    private JwtUtil jwtUtil;

    // Тест деректері
    private static final String TEST_EMAIL  = "test@bank.kz";
    private static final Long   TEST_USER_ID = 42L;
    private static final String TEST_ROLE   = "USER";

    /**
     * @BeforeEach — әр тесттен БҰРЫН жаңа объект жасайды.
     * Тесттер бір-біріне тәуелді болмауы үшін.
     */
    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
    }

    // ────────────────────────────────────────────────
    // ТЕСТ 1: токен жасау
    // ────────────────────────────────────────────────
    @Test
    @DisplayName("1. generateToken() — токен жасалуы тиіс")
    void testGenerateToken_ShouldNotBeNull() {
        // Arrange (дайындық)
        // Act (орындау)
        String token = jwtUtil.generateToken(TEST_EMAIL, TEST_USER_ID, TEST_ROLE);

        // Assert (тексеру) — assertNotNull: null болмауы тиіс
        assertNotNull(token, "Токен null болмауы тиіс");
        assertFalse(token.isBlank(), "Токен бос болмауы тиіс");
        // JWT 3 бөліктен тұрады: header.payload.signature
        assertEquals(3, token.split("\\.").length, "JWT 3 бөліктен тұруы тиіс");
    }

    // ────────────────────────────────────────────────
    // ТЕСТ 2: email-ді токеннен алу
    // ────────────────────────────────────────────────
    @Test
    @DisplayName("2. extractEmail() — дұрыс email қайтаруы тиіс")
    void testExtractEmail_ShouldReturnCorrectEmail() {
        // Arrange
        String token = jwtUtil.generateToken(TEST_EMAIL, TEST_USER_ID, TEST_ROLE);

        // Act
        String extractedEmail = jwtUtil.extractEmail(token);

        // Assert — assertEquals: күтілген мән = нақты мән
        assertEquals(TEST_EMAIL, extractedEmail,
                "Токеннен алынған email бастапқымен сәйкес болуы тиіс");
    }

    // ────────────────────────────────────────────────
    // ТЕСТ 3: userId-ді токеннен алу
    // ────────────────────────────────────────────────
    @Test
    @DisplayName("3. extractUserId() — дұрыс userId қайтаруы тиіс")
    void testExtractUserId_ShouldReturnCorrectUserId() {
        // Arrange
        String token = jwtUtil.generateToken(TEST_EMAIL, TEST_USER_ID, TEST_ROLE);

        // Act
        Long extractedUserId = jwtUtil.extractUserId(token);

        // Assert
        assertEquals(TEST_USER_ID, extractedUserId,
                "Токеннен алынған userId бастапқымен сәйкес болуы тиіс");
    }

    // ────────────────────────────────────────────────
    // ТЕСТ 4: роль алу
    // ────────────────────────────────────────────────
    @Test
    @DisplayName("4. extractRole() — дұрыс роль қайтаруы тиіс")
    void testExtractRole_ShouldReturnCorrectRole() {
        // Arrange
        String token = jwtUtil.generateToken(TEST_EMAIL, TEST_USER_ID, "ADMIN");

        // Act
        String role = jwtUtil.extractRole(token);

        // Assert
        assertEquals("ADMIN", role, "Роль ADMIN болуы тиіс");
    }

    // ────────────────────────────────────────────────
    // ТЕСТ 5: токен мерзімі
    // ────────────────────────────────────────────────
    @Test
    @DisplayName("5. isTokenExpired() — жаңа токен мерзімі өтпеген болуы тиіс")
    void testIsTokenExpired_NewTokenShouldNotBeExpired() {
        // Arrange
        String token = jwtUtil.generateToken(TEST_EMAIL, TEST_USER_ID, TEST_ROLE);

        // Act
        boolean isExpired = jwtUtil.isTokenExpired(token);

        // Assert — assertFalse: жаңа токен мерзімі өтпеген
        assertFalse(isExpired, "Жаңа токен мерзімі өтпеген болуы тиіс");
    }

    // ────────────────────────────────────────────────
    // ТЕСТ 6: бұзылған токен — assertThrows
    // ────────────────────────────────────────────────
    @Test
    @DisplayName("6. extractEmail() — жарамсыз токен — ерекше жағдай шығуы тиіс")
    void testExtractEmail_InvalidToken_ShouldThrowException() {
        // Arrange
        String invalidToken = "bu.zhylgan.token";

        // Assert — assertThrows: ерекше жағдай шығуын тексеру
        assertThrows(Exception.class,
                () -> jwtUtil.extractEmail(invalidToken),
                "Жарамсыз токен үшін ерекше жағдай шығуы тиіс");
    }

    // ────────────────────────────────────────────────
    // ТЕСТ 7: әртүрлі пайдаланушылар үшін токендер
    // ────────────────────────────────────────────────
    @Test
    @DisplayName("7. generateToken() — әртүрлі пайдаланушылар үшін әртүрлі токендер")
    void testGenerateToken_DifferentUsersGetDifferentTokens() {
        // Arrange
        String token1 = jwtUtil.generateToken("user1@bank.kz", 1L, "USER");
        String token2 = jwtUtil.generateToken("user2@bank.kz", 2L, "ADMIN");

        // Assert — токендер бірдей болмауы тиіс
        assertNotEquals(token1, token2, "Әртүрлі пайдаланушылардың токендері бірдей болмауы тиіс");
    }
}

