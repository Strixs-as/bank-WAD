package com.techstore.bank_system;

import com.techstore.bank_system.dto.AuthResponse;
import com.techstore.bank_system.dto.LoginRequest;
import com.techstore.bank_system.dto.RegisterRequest;
import com.techstore.bank_system.entity.User;
import com.techstore.bank_system.repository.RoleRepository;
import com.techstore.bank_system.repository.UserRepository;
import com.techstore.bank_system.service.AuthService;
import com.techstore.bank_system.util.JwtUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * JUnit 5 + Mockito тесттер — AuthService үшін.
 *
 * Mockito — тәуелділіктерді алмастыру (mock) үшін:
 *  @Mock        — нақты класстың орнына жасанды объект
 *  @InjectMocks — тестілейтін класс (mock-тар енгізіледі)
 *  when(...).thenReturn(...) — mock жауабын баптау
 *  verify(...)  — әдіс шақырылды ма тексеру
 *
 * @ExtendWith(MockitoExtension.class) — Mockito қосқышы
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService — тіркелу және кіру тесттері")
class AuthServiceTest {

    // @Mock — нақты дерекқорсыз жұмыс істеу үшін
    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private JwtUtil jwtUtil;

    // @InjectMocks — тестілейтін класс, mock-тар автоматты енгізіледі
    @InjectMocks
    private AuthService authService;

    // ────────────────────────────────────────────────
    // ТЕСТ 1: тіркелу — email бұрын бар
    // ────────────────────────────────────────────────
    @Test
    @DisplayName("1. register() — бар email-мен тіркелу — қате хабар")
    void testRegister_ExistingEmail_ShouldReturnErrorMessage() {
        // Arrange: email бар деп имитациялаймыз
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@bank.kz");
        request.setPassword("pass123");
        request.setFirstName("Айгерім");
        request.setLastName("Бекова");

        when(userRepository.findByEmail("existing@bank.kz"))
                .thenReturn(Optional.of(new User())); // бар email

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        assertNull(response.getToken(), "Токен null болуы тиіс");
        assertNotNull(response.getMessage(), "Қате хабар болуы тиіс");
        assertTrue(response.getMessage().contains("email"),
                "Хабарда 'email' сөзі болуы тиіс");

        // Verify: save() шақырылмауы тиіс
        verify(userRepository, never()).save(any());
    }

    // ────────────────────────────────────────────────
    // ТЕСТ 2: тіркелу — жаңа пайдаланушы
    // ────────────────────────────────────────────────
    @Test
    @DisplayName("2. register() — жаңа пайдаланушы — токен қайтарылуы тиіс")
    void testRegister_NewUser_ShouldReturnToken() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@bank.kz");
        request.setPassword("securePass123");
        request.setFirstName("Нұрлан");
        request.setLastName("Сейтов");

        User savedUser = User.builder()
                .id(1L)
                .email("newuser@bank.kz")
                .firstName("Нұрлан")
                .lastName("Сейтов")
                .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumber(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByPassportNumber(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());
        when(jwtUtil.generateToken(anyString(), any(), anyString()))
                .thenReturn("mock.jwt.token");

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        assertNotNull(response.getToken(), "Токен null болмауы тиіс");
        assertEquals("mock.jwt.token", response.getToken());
        assertEquals("newuser@bank.kz", response.getEmail());

        // Verify: save бір рет шақырылды
        verify(userRepository, times(1)).save(any(User.class));
    }

    // ────────────────────────────────────────────────
    // ТЕСТ 3: кіру — дұрыс деректер
    // ────────────────────────────────────────────────
    @Test
    @DisplayName("3. login() — дұрыс деректер — токен қайтарылуы тиіс")
    void testLogin_ValidCredentials_ShouldReturnToken() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("user@bank.kz");
        request.setPassword("mypassword");

        // BCrypt хэш — "mypassword" сөзінің хэші
        String hashedPassword = org.mindrot.jbcrypt.BCrypt.hashpw("mypassword",
                org.mindrot.jbcrypt.BCrypt.gensalt());

        User existingUser = User.builder()
                .id(5L)
                .email("user@bank.kz")
                .password(hashedPassword)
                .firstName("Зарина")
                .lastName("Акберова")
                .isActive(true)
                .build();

        when(userRepository.findByEmail("user@bank.kz"))
                .thenReturn(Optional.of(existingUser));
        when(jwtUtil.generateToken(anyString(), any(), anyString()))
                .thenReturn("valid.jwt.token");

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertNotNull(response.getToken(), "Токен null болмауы тиіс");
        assertEquals("valid.jwt.token", response.getToken());
    }

    // ────────────────────────────────────────────────
    // ТЕСТ 4: кіру — қате пароль
    // ────────────────────────────────────────────────
    @Test
    @DisplayName("4. login() — қате пароль — токен null болуы тиіс")
    void testLogin_WrongPassword_ShouldReturnNullToken() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("user@bank.kz");
        request.setPassword("wrongpassword");

        String correctHash = org.mindrot.jbcrypt.BCrypt.hashpw("correctpassword",
                org.mindrot.jbcrypt.BCrypt.gensalt());

        User existingUser = User.builder()
                .id(5L)
                .email("user@bank.kz")
                .password(correctHash)
                .isActive(true)
                .build();

        when(userRepository.findByEmail("user@bank.kz"))
                .thenReturn(Optional.of(existingUser));

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertNull(response.getToken(), "Қате парольда токен null болуы тиіс");
        assertNotNull(response.getMessage(), "Қате хабар болуы тиіс");
    }

    // ────────────────────────────────────────────────
    // ТЕСТ 5: кіру — пайдаланушы жоқ
    // ────────────────────────────────────────────────
    @Test
    @DisplayName("5. login() — жоқ пайдаланушы — қате хабар")
    void testLogin_UserNotFound_ShouldReturnErrorMessage() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("ghost@bank.kz");
        request.setPassword("any");

        when(userRepository.findByEmail("ghost@bank.kz"))
                .thenReturn(Optional.empty()); // пайдаланушы жоқ

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertNull(response.getToken());
        assertNotNull(response.getMessage());

        // Verify: generateToken ешқашан шақырылмауы тиіс
        verify(jwtUtil, never()).generateToken(any(), any(), any());
    }
}

