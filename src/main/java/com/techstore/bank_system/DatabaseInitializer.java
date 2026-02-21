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

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            try (Connection conn = DriverManager.getConnection(MASTER_URL, USERNAME, PASSWORD);
                 Statement stmt = conn.createStatement()) {

                stmt.execute(
                    "IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'BankSystem') " +
                    "BEGIN CREATE DATABASE [BankSystem]; END"
                );
                System.out.println("✅ База данных BankSystem готова (SQL Server localhost:1433)");
            }
        } catch (Exception e) {
            System.err.println("⚠ SQL Server недоступен или ошибка создания БД: " + e.getMessage());
            System.err.println("  Убедитесь: SQL Server запущен, TCP/IP включён, логин sa активен, пароль sa верный.");
        }
    }
}
