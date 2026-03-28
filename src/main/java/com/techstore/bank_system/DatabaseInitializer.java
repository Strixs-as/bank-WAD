package com.techstore.bank_system;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Создаёт базу данных BankSystem в SQL Server ДО инициализации JPA.
 */
public class DatabaseInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final String MASTER_URL =
        "jdbc:sqlserver://localhost:1433;databaseName=master;encrypt=false;trustServerCertificate=true";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "sa";

    private static final String BANK_URL =
        "jdbc:sqlserver://localhost:1433;databaseName=BankSystem;encrypt=false;trustServerCertificate=true";

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            // 1. Создаём БД если не существует
            try (Connection conn = DriverManager.getConnection(MASTER_URL, USERNAME, PASSWORD);
                 Statement stmt = conn.createStatement()) {
                stmt.execute(
                    "IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'BankSystem') " +
                    "BEGIN CREATE DATABASE [BankSystem]; END"
                );
                System.out.println("✅ База данных BankSystem готова (SQL Server localhost:1433)");
            }
            // 2. Применяем миграции к BankSystem (исправление схемы)
            try (Connection conn = DriverManager.getConnection(BANK_URL, USERNAME, PASSWORD);
                 Statement stmt = conn.createStatement()) {
                // Делаем patronymic nullable
                try {
                    stmt.execute(
                        "IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS " +
                        "WHERE TABLE_NAME='users' AND COLUMN_NAME='patronymic' AND IS_NULLABLE='NO') " +
                        "BEGIN ALTER TABLE users ALTER COLUMN patronymic NVARCHAR(255) NULL; END"
                    );
                } catch (Exception ignored) { }
                // Делаем address nullable
                try {
                    stmt.execute(
                        "IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS " +
                        "WHERE TABLE_NAME='users' AND COLUMN_NAME='address' AND IS_NULLABLE='NO') " +
                        "BEGIN ALTER TABLE users ALTER COLUMN address NVARCHAR(255) NULL; END"
                    );
                } catch (Exception ignored) { }
                // Делаем phone_number nullable
                try {
                    stmt.execute(
                        "IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS " +
                        "WHERE TABLE_NAME='users' AND COLUMN_NAME='phone_number' AND IS_NULLABLE='NO') " +
                        "BEGIN ALTER TABLE users ALTER COLUMN phone_number NVARCHAR(255) NULL; END"
                    );
                } catch (Exception ignored) { }
                // Делаем passport_number nullable
                try {
                    stmt.execute(
                        "IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS " +
                        "WHERE TABLE_NAME='users' AND COLUMN_NAME='passport_number' AND IS_NULLABLE='NO') " +
                        "BEGIN ALTER TABLE users ALTER COLUMN passport_number NVARCHAR(255) NULL; END"
                    );
                } catch (Exception ignored) { }
                System.out.println("✅ Миграции схемы применены");
            } catch (Exception e2) {
                // BankSystem ещё не создана — Hibernate создаст её сам
            }
        } catch (Exception e) {
            System.err.println("⚠ SQL Server недоступен или ошибка создания БД: " + e.getMessage());
            System.err.println("  Убедитесь: SQL Server запущен, TCP/IP включён, логин sa активен, пароль sa верный.");
        }
    }
}
