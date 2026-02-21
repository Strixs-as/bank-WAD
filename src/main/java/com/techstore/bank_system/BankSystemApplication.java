package com.techstore.bank_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BankSystemApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BankSystemApplication.class);
        app.addInitializers(new DatabaseInitializer());
        app.run(args);
    }
}
