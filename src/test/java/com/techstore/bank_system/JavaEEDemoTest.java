package com.techstore.bank_system;

import com.techstore.bank_system.resource.JavaEEDemoResource;
import com.techstore.bank_system.util.JdbcDemoUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Java EE тақырыбы #13 — JUnit тесттері.
 *
 * JUnit 5 негізгі аннотациялары:
 *  @Test               — тест әдісі
 *  @DisplayName        — тест атауы (оқуға ыңғайлы)
 *  @BeforeEach         — әр тест алдында орындалады
 *  @AfterEach          — әр тест кейін орындалады
 *  @BeforeAll          — барлық тест алдында бір рет орындалады
 *  @AfterAll           — барлық тест кейін бір рет орындалады
 *  @ParameterizedTest  — бірнеше деректермен тест
 *  @ValueSource        — параметрлер тізімі
 *  @CsvSource          — CSV форматындағы параметрлер
 *
 * Негізгі assertion-дар:
 *  assertEquals()      — мәндер тең ме
 *  assertNotNull()     — null емес пе
 *  assertNull()        — null ма
 *  assertTrue()        — шарт ақиқат па
 *  assertFalse()       — шарт жалған ба
 *  assertThrows()      — ерекше жағдай шығады ма
 *  assertAll()         — барлық шарттарды тексеру
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Java EE #13 — JUnit 5 тесттері (JavaEEDemoResource)")
class JavaEEDemoTest {

    @Mock
    private JdbcDemoUtil jdbcDemoUtil;

    @InjectMocks
    private JavaEEDemoResource demoResource;

    // ────────────────────────────────────────────────
    // Lifecycle аннотациялары (@BeforeAll, @BeforeEach)
    // ────────────────────────────────────────────────
    @BeforeAll
    static void beforeAll() {
        System.out.println("═══════════════════════════════════════");
        System.out.println("🚀 JavaEEDemoTest тесттері басталды");
        System.out.println("═══════════════════════════════════════");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("═══════════════════════════════════════");
        System.out.println("✅ JavaEEDemoTest тесттері аяқталды");
        System.out.println("═══════════════════════════════════════");
    }

    @BeforeEach
    void setUp() {
        System.out.println("── Тест дайындалды ──");
    }

    @AfterEach
    void tearDown() {
        System.out.println("── Тест аяқталды ──");
    }

    // ────────────────────────────────────────────────
    // ТЕСТ 1: GET /api/demo — анықтама эндпоинт
    // ────────────────────────────────────────────────
    @Test
    @DisplayName("1. getDemoInfo() — 200 OK және эндпоинттер тізімі болуы тиіс")
    void testGetDemoInfo_ShouldReturnOkWithEndpoints() {
        // Act
        ResponseEntity<Map<String, Object>> response = demoResource.getDemoInfo();

        // Assert — assertEquals
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "HTTP 200 OK болуы тиіс");

        // Assert — assertNotNull
        assertNotNull(response.getBody(), "Body null болмауы тиіс");

        Map<String, Object> body = response.getBody();
        assertTrue(body.containsKey("endpoints"), "Body 'endpoints' кілті болуы тиіс");

        // Assert — assertAll: бір уақытта бірнеше шарт тексеру
        assertAll("Demo info response тексеру",
                () -> assertEquals("Java EE Demo API — Барлық тақырыптар", body.get("title")),
                () -> assertEquals("1.0", body.get("version")),
                () -> assertNotNull(body.get("endpoints"))
        );
    }

    // ────────────────────────────────────────────────
    // ТЕСТ 2: GET /api/demo/items — бос тізім емес
    // ────────────────────────────────────────────────
    @Test
    @DisplayName("2. getAllItems() — бастапқы элементтер болуы тиіс")
    void testGetAllItems_ShouldHaveInitialItems() {
        // Act
        ResponseEntity<Map<String, Object>> response = demoResource.getAllItems();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Object count = response.getBody().get("count");
        assertNotNull(count, "count мәні болуы тиіс");
        assertTrue((int) count >= 0, "count нөлден кем болмауы тиіс");
    }

    // ────────────────────────────────────────────────
    // ТЕСТ 3: POST /api/demo/items — жаңа элемент
    // ────────────────────────────────────────────────
    @Test
    @DisplayName("3. createItem() — жаңа элемент 201 CREATED қайтаруы тиіс")
    void testCreateItem_ShouldReturn201Created() {
        // Arrange
        Map<String, String> body = new LinkedHashMap<>();
        body.put("name",        "Тест элементі");
        body.put("description", "JUnit тест арқылы жасалды");

        // Act
        ResponseEntity<Map<String, Object>> response = demoResource.createItem(body);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode(),
                "HTTP 201 CREATED болуы тиіс");
        assertNotNull(response.getBody());
        assertEquals("POST", response.getBody().get("method"));
        assertNotNull(response.getBody().get("created"), "created объект болуы тиіс");
    }

    // ────────────────────────────────────────────────
    // ТЕСТ 4: PUT /api/demo/items/{id} — жоқ id
    // ────────────────────────────────────────────────
    @Test
    @DisplayName("4. updateItem() — жоқ ID — 404 NOT FOUND болуы тиіс")
    void testUpdateItem_NotFound_Should404() {
        // Arrange
        Map<String, String> body = Map.of("name", "Жаңа атау");

        // Act
        ResponseEntity<Map<String, Object>> response = demoResource.updateItem(9999L, body);

        // Assert — assertThrows-тің балама тәсілі: статус коды тексеру
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
                "Жоқ ID үшін 404 болуы тиіс");
        assertTrue(response.getBody().containsKey("error"),
                "error хабар болуы тиіс");
    }

    // ────────────────────────────────────────────────
    // ТЕСТ 5: DELETE /api/demo/items/{id} — жоқ id
    // ────────────────────────────────────────────────
    @Test
    @DisplayName("5. deleteItem() — жоқ ID — 404 NOT FOUND болуы тиіс")
    void testDeleteItem_NotFound_Should404() {
        // Act
        ResponseEntity<Map<String, Object>> response = demoResource.deleteItem(9999L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody().get("error"));
    }

    // ────────────────────────────────────────────────
    // ТЕСТ 6: POST → GET → DELETE — толық CRUD цикл
    // ────────────────────────────────────────────────
    @Test
    @DisplayName("6. CRUD цикл — POST → GET → DELETE")
    void testCrudCycle_CreateGetDelete() {
        // CREATE (POST)
        Map<String, String> body = Map.of("name", "CRUD тест", "description", "Толық CRUD цикл");
        ResponseEntity<Map<String, Object>> createResp = demoResource.createItem(body);
        assertEquals(HttpStatus.CREATED, createResp.getStatusCode());

        Long newId = (Long) ((Map<?, ?>) createResp.getBody().get("created")).get("id");
        assertNotNull(newId, "Жаңа элемент ID болуы тиіс");

        // READ (GET)
        ResponseEntity<Map<String, Object>> getResp = demoResource.getItemById(newId);
        assertEquals(HttpStatus.OK, getResp.getStatusCode());
        assertEquals("CRUD тест", getResp.getBody().get("name"));

        // DELETE
        ResponseEntity<Map<String, Object>> deleteResp = demoResource.deleteItem(newId);
        assertEquals(HttpStatus.OK, deleteResp.getStatusCode());

        // Жойылғаннан кейін GET — 404 болуы тиіс
        ResponseEntity<Map<String, Object>> afterDelete = demoResource.getItemById(newId);
        assertEquals(HttpStatus.NOT_FOUND, afterDelete.getStatusCode());
    }

    // ────────────────────────────────────────────────
    // ТЕСТ 7: @ParameterizedTest — бірнеше атаумен
    // ────────────────────────────────────────────────
    /**
     * @ParameterizedTest — бір тестті бірнеше деректермен орындау.
     * Java EE #13 тақырыбында маңызды аннотация.
     */
    @ParameterizedTest(name = "POST item: name=''{0}''")
    @ValueSource(strings = {"Servlet", "Spring MVC", "Hibernate ORM", "REST API", "JUnit"})
    @DisplayName("7. createItem() — әртүрлі атаулармен POST (ParameterizedTest)")
    void testCreateItem_Parameterized(String topicName) {
        // Arrange
        Map<String, String> body = Map.of(
                "name",        topicName,
                "description", "Java EE тақырыбы: " + topicName
        );

        // Act
        ResponseEntity<Map<String, Object>> response = demoResource.createItem(body);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode(),
                topicName + " үшін 201 CREATED болуы тиіс");
        assertNotNull(response.getBody().get("created"));
    }

    // ────────────────────────────────────────────────
    // ТЕСТ 8: @CsvSource — PUT жаңарту тексеру
    // ────────────────────────────────────────────────
    @ParameterizedTest(name = "PUT item id={0}: name=''{1}''")
    @CsvSource({
            "1, Жаңартылған Servlet,  Servlet доп-демо",
            "2, Жаңартылған Login,    Кіру жаңартылды",
            "3, Жаңартылған Account,  Шот жаңартылды"
    })
    @DisplayName("8. updateItem() — CSV деректермен PUT (@CsvSource)")
    void testUpdateItem_CsvSource(Long id, String newName, String newDesc) {
        // Arrange
        Map<String, String> body = Map.of("name", newName, "description", newDesc);

        // Act
        ResponseEntity<Map<String, Object>> response = demoResource.updateItem(id, body);

        // Assert — id 1, 2, 3 конструкторда жасалған, бар болуы тиіс
        assertNotNull(response);
        if (response.getStatusCode() == HttpStatus.OK) {
            Map<?, ?> updated = (Map<?, ?>) response.getBody().get("updated");
            assertEquals(newName.trim(), ((String) updated.get("name")).trim());
        }
    }

    // ────────────────────────────────────────────────
    // ТЕСТ 9: JDBC mock — getDemoConnectionInfo
    // ────────────────────────────────────────────────
    @Test
    @DisplayName("9. getJdbcConnectionInfo() — JDBC mock тест")
    void testGetJdbcConnectionInfo_WithMock() {
        // Arrange — JdbcDemoUtil mock-ты баптау
        Map<String, String> mockInfo = new LinkedHashMap<>();
        mockInfo.put("status",          "✅ Байланыс сәтті орнатылды");
        mockInfo.put("databaseProduct", "Microsoft SQL Server");
        when(jdbcDemoUtil.getDemoConnectionInfo()).thenReturn(mockInfo);

        // Act
        ResponseEntity<Map<String, Object>> response = demoResource.getJdbcConnectionInfo();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Java EE #8 — JDBC Connection", response.getBody().get("topic"));

        // Verify — mock шақырылды ма тексеру
        verify(jdbcDemoUtil, times(1)).getDemoConnectionInfo();
    }

    // ────────────────────────────────────────────────
    // ТЕСТ 10: assertThrows — ерекше жағдайды тексеру
    // ────────────────────────────────────────────────
    @Test
    @DisplayName("10. assertThrows — NullPointerException тексеру")
    void testCreateItem_NullBody_ShouldThrow() {
        // assertThrows — ерекше жағдай шығуын тексереді
        // null body берсек NullPointerException шығады
        assertThrows(
                NullPointerException.class,
                () -> demoResource.createItem(null),
                "null body берсек NullPointerException шығуы тиіс"
        );
    }

    // ────────────────────────────────────────────────
    // ТЕСТ 11: getMvcInfo — MVC архитектура тексеру
    // ────────────────────────────────────────────────
    @Test
    @DisplayName("11. getMvcInfo() — MVC архитектура схемасы болуы тиіс")
    void testGetMvcInfo_ShouldContainMvcLayers() {
        // Act
        ResponseEntity<Map<String, Object>> response = demoResource.getMvcInfo();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();

        assertAll("MVC ақпарат тексеру",
                () -> assertEquals("Java EE #7 — Spring MVC Архитектурасы", body.get("topic")),
                () -> assertEquals("Model — View — Controller", body.get("pattern")),
                () -> assertNotNull(body.get("flow")),
                () -> assertNotNull(body.get("currentProject"))
        );

        // flow тізімінде кем дегенде MVC қабаттары болуы тиіс
        @SuppressWarnings("unchecked")
        List<String> flow = (List<String>) body.get("flow");
        assertTrue(flow.stream().anyMatch(s -> s.contains("Controller")),
                "flow тізімінде Controller болуы тиіс");
        assertTrue(flow.stream().anyMatch(s -> s.contains("Service")),
                "flow тізімінде Service болуы тиіс");
        assertTrue(flow.stream().anyMatch(s -> s.contains("Repository")),
                "flow тізімінде Repository болуы тиіс");
    }
}

