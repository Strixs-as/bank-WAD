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
            String url = "http://localhost:" + port + "/index.html";

            System.out.println("🌐 Открываю браузер: " + url);

            // Способ 1: java.awt.Desktop (работает в большинстве случаев)
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                return;
            }

            // Способ 2: через cmd (Windows fallback)
            String os = System.getProperty("os.name", "").toLowerCase();
            if (os.contains("win")) {
                Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", url});
            }
        } catch (Exception e) {
            System.err.println("⚠ Не удалось открыть браузер: " + e.getMessage());
        }
    }
}
