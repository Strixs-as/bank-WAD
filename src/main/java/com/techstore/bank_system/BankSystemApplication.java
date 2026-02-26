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
                // Найти PID через PowerShell (надёжнее чем netstat)
                ProcessBuilder pbFind = new ProcessBuilder(
                    "powershell", "-Command",
                    "Get-NetTCPConnection -LocalPort " + port +
                    " -State Listen -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess"
                );
                pbFind.redirectErrorStream(true);
                Process findProcess = pbFind.start();
                String output = new String(findProcess.getInputStream().readAllBytes()).trim();
                findProcess.waitFor();

                if (!output.isEmpty()) {
                    for (String line : output.split("\\r?\\n")) {
                        String pid = line.trim();
                        if (pid.matches("\\d+") && !pid.equals("0")) {
                            ProcessBuilder pbKill = new ProcessBuilder(
                                "taskkill", "/PID", pid, "/F"
                            );
                            pbKill.redirectErrorStream(true);
                            Process killProcess = pbKill.start();
                            killProcess.waitFor();
                            System.out.println("✅ Порт " + port + " освобождён (завершён процесс PID=" + pid + ")");
                        }
                    }
                    Thread.sleep(2000); // увеличена пауза для надёжного освобождения порта
                }
            }
        } catch (Exception e) {
            System.err.println("⚠ Не удалось освободить порт " + port + ": " + e.getMessage());
        }
    }
}
