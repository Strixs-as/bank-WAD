package com.techstore.bank_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BankSystemApplication {

    public static void main(String[] args) {
        freePort(8080);
        SpringApplication app = new SpringApplication(BankSystemApplication.class);
        app.addInitializers(new DatabaseInitializer());
        app.run(args);
    }

    /**
     * Автоматически освобождает указанный порт перед стартом.
     * Работает на Windows (через netstat + taskkill).
     */
    private static void freePort(int port) {
        try {
            String os = System.getProperty("os.name", "").toLowerCase();
            if (os.contains("win")) {
                // Найти PID процесса на порту
                Process findProcess = Runtime.getRuntime().exec(
                    new String[]{"cmd", "/c", "netstat -ano | findstr :" + port + " | findstr LISTENING"}
                );
                String output = new String(findProcess.getInputStream().readAllBytes()).trim();
                findProcess.waitFor();

                if (!output.isEmpty()) {
                    // Последний токен в строке — PID
                    String[] parts = output.trim().split("\\s+");
                    String pid = parts[parts.length - 1];
                    if (pid.matches("\\d+") && !pid.equals("0")) {
                        Process killProcess = Runtime.getRuntime().exec(
                            new String[]{"cmd", "/c", "taskkill /PID " + pid + " /F"}
                        );
                        killProcess.waitFor();
                        System.out.println("✅ Порт " + port + " освобождён (завершён процесс PID=" + pid + ")");
                        Thread.sleep(500); // небольшая пауза после kill
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("⚠ Не удалось освободить порт " + port + ": " + e.getMessage());
        }
    }
}
