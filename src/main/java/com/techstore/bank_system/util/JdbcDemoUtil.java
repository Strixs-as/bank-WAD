package com.techstore.bank_system.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC Demo — Java EE тақырыбы #8: JDBC арқылы дерекқорға қосылу.
 *
 * Классикалық JDBC қадамдары:
 *  1️⃣  Connection     — дерекқорға байланыс орнату
 *  2️⃣  Statement      — SQL сұрауын дайындау (PreparedStatement)
 *  3️⃣  Execute query  — SQL орындау
 *  4️⃣  ResultSet      — нәтижені өңдеу
 *
 * Spring Bean ретінде тіркелген, бірақ Spring Data JPA-ны ПАЙДАЛАНБАЙДЫ —
 * тек таза JDBC арқылы жұмыс жасайды (оқыту мақсатында).
 */
@Component
public class JdbcDemoUtil {

    // application.properties / application-sqlserver.properties мәндері
    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    /**
     * JDBC #1 — Connection: дерекқорға тікелей байланыс орнату.
     * HikariCP пулы орнатылған жағдайда Spring DataSource пайдаланылады,
     * бірақ мұнда оқыту мақсатында DriverManager.getConnection() қолданылады.
     *
     * @return байланыс ақпараты
     */
    public Map<String, String> getDemoConnectionInfo() {
        Map<String, String> info = new LinkedHashMap<>();

        // JDBC ҚАДАМ 1: Connection орнату
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {

            DatabaseMetaData meta = connection.getMetaData();
            info.put("status",          "✅ Байланыс сәтті орнатылды");
            info.put("databaseProduct", meta.getDatabaseProductName());
            info.put("databaseVersion", meta.getDatabaseProductVersion());
            info.put("driverName",      meta.getDriverName());
            info.put("url",             meta.getURL());
            info.put("autoCommit",      String.valueOf(connection.getAutoCommit()));

        } catch (SQLException e) {
            info.put("status", "❌ Байланыс қатесі: " + e.getMessage());
            info.put("sqlState", e.getSQLState());
            info.put("errorCode", String.valueOf(e.getErrorCode()));
        }

        return info;
    }

    /**
     * JDBC #2, #3, #4 — PreparedStatement + Execute + ResultSet.
     * Пайдаланушылар кестесінен деректерді оқу (тек оқу, өзгерту жоқ).
     *
     * @param limit нешеуін алу
     * @return пайдаланушылар тізімі (email, firstName, lastName)
     */
    public List<Map<String, String>> getDemoUsers(int limit) {
        List<Map<String, String>> users = new ArrayList<>();

        // JDBC ҚАДАМ 1: Connection
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {

            // JDBC ҚАДАМ 2: PreparedStatement — SQL инъекциясынан қорғайды
            String sql = "SELECT TOP (?) id, email, first_name, last_name, is_active FROM users";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {

                // Параметр орнату
                stmt.setInt(1, limit);

                // JDBC ҚАДАМ 3: Execute — SQL орындау
                try (ResultSet rs = stmt.executeQuery()) {

                    // JDBC ҚАДАМ 4: ResultSet — нәтижені оқу
                    while (rs.next()) {
                        Map<String, String> row = new LinkedHashMap<>();
                        row.put("id",        String.valueOf(rs.getLong("id")));
                        row.put("email",     rs.getString("email"));
                        row.put("firstName", rs.getString("first_name"));
                        row.put("lastName",  rs.getString("last_name"));
                        row.put("isActive",  rs.getBoolean("is_active") ? "✅ Белсенді" : "❌ Блокталған");
                        users.add(row);
                    }
                }
            }

        } catch (SQLException e) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "SQL қатесі: " + e.getMessage());
            users.add(error);
        }

        return users;
    }

    /**
     * JDBC транзакция демосы — commit / rollback.
     * Оқыту мақсатында транзакция механизмін көрсетеді.
     *
     * @return транзакция нәтижесі
     */
    public Map<String, String> getDemoTransaction() {
        Map<String, String> result = new LinkedHashMap<>();

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {

            // Авто-commit өшіру — транзакцияны қолмен басқару
            connection.setAutoCommit(false);
            result.put("autoCommit", "false — транзакция басталды");

            try {
                // Демо: тек SELECT (кестеге жазу жоқ)
                try (PreparedStatement stmt = connection.prepareStatement(
                        "SELECT COUNT(*) AS cnt FROM users")) {
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            result.put("userCount", "Пайдаланушылар саны: " + rs.getInt("cnt"));
                        }
                    }
                }

                // COMMIT — өзгерістерді сақтау
                connection.commit();
                result.put("transaction", "✅ COMMIT — транзакция сәтті аяқталды");

            } catch (SQLException e) {
                // ROLLBACK — қате болса өзгерістерді болдырмау
                connection.rollback();
                result.put("transaction", "❌ ROLLBACK — транзакция кері қайтарылды: " + e.getMessage());
            }

        } catch (SQLException e) {
            result.put("error", "Байланыс қатесі: " + e.getMessage());
        }

        return result;
    }
}

