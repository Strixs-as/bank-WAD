package com.techstore.bank_system.resource;

import com.techstore.bank_system.util.JdbcDemoUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Java EE Demo REST API — Java EE тақырыптарын демонстрациялау.
 *
 * REST API тақырыбы #12 — HTTP әдістері:
 *  GET    /api/demo/items         — барлық элементтерді алу
 *  GET    /api/demo/items/{id}    — бір элементті алу
 *  POST   /api/demo/items         — жаңа элемент қосу
 *  PUT    /api/demo/items/{id}    — элементті жаңарту
 *  DELETE /api/demo/items/{id}    — элементті жою
 *
 * JDBC тақырыбы #8:
 *  GET    /api/demo/jdbc/connection — JDBC байланыс ақпараты
 *  GET    /api/demo/jdbc/users      — JDBC арқылы пайдаланушылар
 *  GET    /api/demo/jdbc/transaction — JDBC транзакция демосы
 *
 * Spring тақырыбы #7 — MVC архитектурасы:
 *  GET    /api/demo/mvc-info       — MVC схемасын түсіндіру
 *
 * Connection Pool тақырыбы #9:
 *  GET    /api/demo/pool-info      — HikariCP ақпараты
 */
@RestController
@RequestMapping("/api/demo")
public class JavaEEDemoResource {

    private final JdbcDemoUtil jdbcDemoUtil;

    // In-memory деректер қоймасы (Spring Data JPA-сыз — таза демо)
    private final Map<Long, Map<String, Object>> items = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public JavaEEDemoResource(JdbcDemoUtil jdbcDemoUtil) {
        this.jdbcDemoUtil = jdbcDemoUtil;
        // Бастапқы деректер
        addItem("Тіркелу (Register)", "POST /api/auth/register — жаңа пайдаланушы тіркеу");
        addItem("Кіру (Login)",       "POST /api/auth/login — жүйеге кіру, JWT токен алу");
        addItem("Шот ашу (Account)",  "POST /api/accounts — банктік шот ашу");
    }

    // ─────────────────────────────────────────────────────────────
    // HTTP GET — барлық элементтерді алу (READ ALL)
    // ─────────────────────────────────────────────────────────────
    /**
     * GET /api/demo/items — барлық элементтер тізімі.
     * HTTP GET: деректерді алу үшін (мәлімет өзгермейді).
     */
    @GetMapping("/items")
    public ResponseEntity<Map<String, Object>> getAllItems() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("method",      "GET");
        response.put("description", "Барлық элементтерді алу — HTTP GET");
        response.put("count",       items.size());
        response.put("items",       new ArrayList<>(items.values()));
        response.put("timestamp",   LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────
    // HTTP GET — бір элементті алу (READ ONE)
    // ─────────────────────────────────────────────────────────────
    /**
     * GET /api/demo/items/{id} — id бойынша бір элемент.
     */
    @GetMapping("/items/{id}")
    public ResponseEntity<Map<String, Object>> getItemById(@PathVariable Long id) {
        Map<String, Object> item = items.get(id);
        if (item == null) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Элемент табылмады. ID: " + id);
            error.put("status", 404);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        return ResponseEntity.ok(item);
    }

    // ─────────────────────────────────────────────────────────────
    // HTTP POST — жаңа элемент қосу (CREATE)
    // ─────────────────────────────────────────────────────────────
    /**
     * POST /api/demo/items — жаңа элемент жасау.
     * HTTP POST: жаңа деректер жіберу үшін (body-да JSON).
     *
     * Body: {"name": "...", "description": "..."}
     */
    @PostMapping("/items")
    public ResponseEntity<Map<String, Object>> createItem(@RequestBody Map<String, String> body) {
        String name        = body.getOrDefault("name",        "Атауы жоқ");
        String description = body.getOrDefault("description", "Сипаттамасы жоқ");

        Map<String, Object> newItem = addItem(name, description);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("method",      "POST");
        response.put("description", "Жаңа элемент жасалды — HTTP POST");
        response.put("created",     newItem);
        response.put("timestamp",   LocalDateTime.now().toString());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ─────────────────────────────────────────────────────────────
    // HTTP PUT — элементті жаңарту (UPDATE)
    // ─────────────────────────────────────────────────────────────
    /**
     * PUT /api/demo/items/{id} — элементті толық жаңарту.
     * HTTP PUT: бар деректі жаңарту үшін (толық алмастыру).
     *
     * Body: {"name": "...", "description": "..."}
     */
    @PutMapping("/items/{id}")
    public ResponseEntity<Map<String, Object>> updateItem(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        if (!items.containsKey(id)) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Жаңартылатын элемент табылмады. ID: " + id);
            error.put("status", 404);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        Map<String, Object> existing = items.get(id);
        existing.put("name",        body.getOrDefault("name",        (String) existing.get("name")));
        existing.put("description", body.getOrDefault("description", (String) existing.get("description")));
        existing.put("updatedAt",   LocalDateTime.now().toString());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("method",      "PUT");
        response.put("description", "Элемент жаңартылды — HTTP PUT");
        response.put("updated",     existing);
        response.put("timestamp",   LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────
    // HTTP DELETE — элементті жою (DELETE)
    // ─────────────────────────────────────────────────────────────
    /**
     * DELETE /api/demo/items/{id} — элементті жою.
     * HTTP DELETE: деректі жою үшін.
     */
    @DeleteMapping("/items/{id}")
    public ResponseEntity<Map<String, Object>> deleteItem(@PathVariable Long id) {
        Map<String, Object> removed = items.remove(id);
        if (removed == null) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Жойылатын элемент табылмады. ID: " + id);
            error.put("status", 404);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("method",      "DELETE");
        response.put("description", "Элемент жойылды — HTTP DELETE");
        response.put("deletedId",   id);
        response.put("deleted",     removed);
        response.put("timestamp",   LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────
    // JDBC DEMO — тақырып #8
    // ─────────────────────────────────────────────────────────────
    /**
     * GET /api/demo/jdbc/connection — JDBC DriverManager.getConnection() демосы.
     */
    @GetMapping("/jdbc/connection")
    public ResponseEntity<Map<String, Object>> getJdbcConnectionInfo() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("topic",       "Java EE #8 — JDBC Connection");
        response.put("description", "DriverManager.getConnection() арқылы тікелей байланыс");
        response.put("steps", List.of(
                "1️⃣ Connection  — DriverManager.getConnection(url, user, pass)",
                "2️⃣ Statement   — connection.prepareStatement(sql)",
                "3️⃣ Execute     — stmt.executeQuery()",
                "4️⃣ ResultSet   — while(rs.next()) { rs.getString(...) }"
        ));
        response.put("connectionInfo", jdbcDemoUtil.getDemoConnectionInfo());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/demo/jdbc/users?limit=5 — JDBC PreparedStatement + ResultSet демосы.
     */
    @GetMapping("/jdbc/users")
    public ResponseEntity<Map<String, Object>> getJdbcUsers(
            @RequestParam(defaultValue = "5") int limit) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("topic",       "Java EE #8 — JDBC PreparedStatement + ResultSet");
        response.put("description", "SQL: SELECT TOP (?) ... FROM users — параметрлі сұрау");
        response.put("limit",       limit);
        response.put("users",       jdbcDemoUtil.getDemoUsers(limit));
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/demo/jdbc/transaction — JDBC транзакция (commit/rollback) демосы.
     */
    @GetMapping("/jdbc/transaction")
    public ResponseEntity<Map<String, Object>> getJdbcTransaction() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("topic",       "Java EE #8 — JDBC Transaction (commit/rollback)");
        response.put("description", "setAutoCommit(false) → execute → commit/rollback");
        response.put("result",      jdbcDemoUtil.getDemoTransaction());
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────
    // MVC СХЕМА — тақырып #7
    // ─────────────────────────────────────────────────────────────
    /**
     * GET /api/demo/mvc-info — Spring MVC архитектурасының схемасы.
     */
    @GetMapping("/mvc-info")
    public ResponseEntity<Map<String, Object>> getMvcInfo() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("topic",        "Java EE #7 — Spring MVC Архитектурасы");
        response.put("pattern",      "Model — View — Controller");
        response.put("flow", List.of(
                "1. Client → HTTP Request",
                "2. DispatcherServlet → сұрауды қабылдайды",
                "3. Controller → логика өңдейді (@RestController)",
                "4. Service → бизнес логика (@Service)",
                "5. Repository → дерекқор операциялары (@Repository)",
                "6. Database → SQL Server / H2",
                "7. Model → деректер (Entity, DTO)",
                "8. View → JSON (REST) немесе HTML (Thymeleaf)"
        ));
        response.put("currentProject", Map.of(
                "controller", "com.techstore.bank_system.controller.*",
                "service",    "com.techstore.bank_system.service.*",
                "repository", "com.techstore.bank_system.repository.*",
                "entity",     "com.techstore.bank_system.entity.*",
                "dto",        "com.techstore.bank_system.dto.*",
                "view",       "src/main/resources/templates/*.html (Thymeleaf)"
        ));
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────
    // CONNECTION POOL — тақырып #9
    // ─────────────────────────────────────────────────────────────
    /**
     * GET /api/demo/pool-info — HikariCP Connection Pool ақпараты.
     */
    @GetMapping("/pool-info")
    public ResponseEntity<Map<String, Object>> getPoolInfo() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("topic",       "Java EE #9 — Connection Pool (HikariCP)");
        response.put("library",     "HikariCP — ең жылдам Java connection pool");
        response.put("config", Map.of(
                "maximum-pool-size",  "10 — максимал байланыс саны",
                "minimum-idle",       "2 — минимал бос байланыс",
                "connection-timeout", "30000ms — байланыс күту уақыты",
                "idle-timeout",       "600000ms — бос байланысты ұстау уақыты",
                "max-lifetime",       "1800000ms — байланыстың максимал өмір сүру уақыты"
        ));
        response.put("advantages", List.of(
                "⚡ Жылдам — байланысты жасамайды, қайта пайдаланады",
                "💾 Ресурс үнемдейді — байланыс ашу/жабу шығынын азайтады",
                "📊 Мониторинг — белсенді/бос байланыстарды бақылайды",
                "🔄 Thread-safe — бір уақытта көп сұрауды өңдейді"
        ));
        response.put("alternatives", List.of("C3P0", "DBCP2", "Tomcat JDBC Pool"));
        response.put("configFile",   "application-sqlserver.properties → spring.datasource.hikari.*");
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────
    // ЖАЛПЫ АНЫҚТАМА — барлық эндпоинттер
    // ─────────────────────────────────────────────────────────────
    /**
     * GET /api/demo — барлық demo эндпоинттердің тізімі.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDemoInfo() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("title",   "Java EE Demo API — Барлық тақырыптар");
        response.put("version", "1.0");
        response.put("endpoints", List.of(
                Map.of("method", "GET",    "url", "/api/demo/items",           "topic", "#12 REST API — барлық элементтер"),
                Map.of("method", "GET",    "url", "/api/demo/items/{id}",      "topic", "#12 REST API — бір элемент"),
                Map.of("method", "POST",   "url", "/api/demo/items",           "topic", "#12 REST API — жаңа элемент"),
                Map.of("method", "PUT",    "url", "/api/demo/items/{id}",      "topic", "#12 REST API — жаңарту"),
                Map.of("method", "DELETE", "url", "/api/demo/items/{id}",      "topic", "#12 REST API — жою"),
                Map.of("method", "GET",    "url", "/api/demo/jdbc/connection", "topic", "#8 JDBC — Connection"),
                Map.of("method", "GET",    "url", "/api/demo/jdbc/users",      "topic", "#8 JDBC — PreparedStatement + ResultSet"),
                Map.of("method", "GET",    "url", "/api/demo/jdbc/transaction","topic", "#8 JDBC — Transaction commit/rollback"),
                Map.of("method", "GET",    "url", "/api/demo/mvc-info",        "topic", "#7 Spring MVC — архитектура схемасы"),
                Map.of("method", "GET",    "url", "/api/demo/pool-info",       "topic", "#9 Connection Pool — HikariCP"),
                Map.of("method", "GET",    "url", "/servlet/info",             "topic", "#2 Servlet — lifecycle demo (doGet)"),
                Map.of("method", "POST",   "url", "/servlet/info",             "topic", "#2 Servlet — doPost demo")
        ));
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────
    // Helper method
    // ─────────────────────────────────────────────────────────────
    private Map<String, Object> addItem(String name, String description) {
        long id = idCounter.getAndIncrement();
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id",          id);
        item.put("name",        name);
        item.put("description", description);
        item.put("createdAt",   LocalDateTime.now().toString());
        items.put(id, item);
        return item;
    }
}

