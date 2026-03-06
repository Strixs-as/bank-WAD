package com.techstore.bank_system.servlet;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Java EE тақырыбы #2 — Servlet lifecycle демосы.
 * URL: /servlet/hello?name=Naiman
 *
 * Lifecycle:
 *  1. init()    — бір рет (инициализация)
 *  2. service() → doGet() / doPost() — әр сұрауда
 *  3. destroy() — бір рет (жою)
 */
public class HelloServlet extends HttpServlet {

    private String initTime;
    private int requestCount;

    /** LIFECYCLE 1: init() */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        initTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        requestCount = 0;
        System.out.println("✅ HelloServlet init() — " + initTime);
    }

    /** LIFECYCLE 2: doGet() — GET /servlet/hello?name=Naiman */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        requestCount++;
        String name = request.getParameter("name");
        if (name == null || name.isBlank()) name = "World";

        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        // CORS
        response.setHeader("Access-Control-Allow-Origin", "*");

        PrintWriter out = response.getWriter();
        out.printf("{%n");
        out.printf("  \"message\": \"Hello, %s! 👋\"%n", escapeJson(name));
        out.printf("  \"method\": \"GET\",%n");
        out.printf("  \"servlet\": \"HelloServlet extends HttpServlet\",%n");
        out.printf("  \"lifecycle\": \"init() → service() → doGet()\",%n");
        out.printf("  \"initTime\": \"%s\",%n", initTime);
        out.printf("  \"requestCount\": %d,%n", requestCount);
        out.printf("  \"timestamp\": \"%s\"%n",
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        out.printf("}%n");
    }

    /** LIFECYCLE 2: doPost() */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        requestCount++;
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        PrintWriter out = response.getWriter();
        out.printf("{%n");
        out.printf("  \"message\": \"POST сұрауы қабылданды\",%n");
        out.printf("  \"method\": \"POST\",%n");
        out.printf("  \"requestCount\": %d,%n", requestCount);
        out.printf("  \"timestamp\": \"%s\"%n",
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        out.printf("}%n");
    }

    /** LIFECYCLE 3: destroy() */
    @Override
    public void destroy() {
        System.out.println("🛑 HelloServlet destroy() — жалпы сұраулар: " + requestCount);
        super.destroy();
    }

    private String escapeJson(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

