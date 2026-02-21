package com.techstore.bank_system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.net.URI;

@Component
public class StartupBrowserOpener implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private Environment env;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            String port = env.getProperty("server.port", "8080");
            String context = env.getProperty("server.servlet.context-path", "");
            if (context == null || context.equals("/")) context = "";
            String url = "http://localhost:" + port + context + "/";
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception e) {
            // ignore - don't prevent application from starting
            System.err.println("Failed to open browser: " + e.getMessage());
        }
    }
}

