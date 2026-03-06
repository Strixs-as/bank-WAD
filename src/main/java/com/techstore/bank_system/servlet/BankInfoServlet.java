package com.techstore.bank_system.servlet;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Java EE Servlet — демонстрация lifecycle (init → service → destroy).
 * URL: /servlet/info
 *
 * Servlet lifecycle:
 *  1. init()    — серверлет жүктелгенде бір рет шақырылады (инициализация)
 *  2. service() → doGet()/doPost() — әр сұрауда шақырылады
 *  3. destroy() — серверлет жойылғанда бір рет шақырылады
 */
@WebServlet("/servlet/info")
public class BankInfoServlet extends HttpServlet {

    private String servletStartTime;
    private int requestCount;

    /**
     * LIFECYCLE — 1: init()
     * Сервер іске қосылғанда БІР РЕТ орындалады.
     * Мұнда инициализация логикасы жазылады.
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        servletStartTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        requestCount = 0;
        System.out.println("✅ BankInfoServlet инициализирован в: " + servletStartTime);
    }

    /**
     * LIFECYCLE — 2: doGet()
     * Клиент GET сұрауын жіберген сайын орындалады.
     * HTTP GET → мәлімет алу үшін қолданылады.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        requestCount++;
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html lang='ru'>");
        out.println("<head>");
        out.println("  <meta charset='UTF-8'>");
        out.println("  <title>Java EE Servlet Demo</title>");
        out.println("  <style>");
        out.println("    body { font-family: 'Segoe UI', sans-serif; background: linear-gradient(135deg,#667eea,#764ba2); min-height:100vh; display:flex; justify-content:center; align-items:center; margin:0; }");
        out.println("    .card { background:white; border-radius:15px; padding:40px; max-width:600px; width:100%; box-shadow:0 20px 60px rgba(0,0,0,0.3); }");
        out.println("    h1 { color:#333; margin-bottom:20px; }");
        out.println("    .badge { display:inline-block; background:linear-gradient(135deg,#667eea,#764ba2); color:white; padding:5px 15px; border-radius:20px; font-size:0.85em; margin-bottom:20px; }");
        out.println("    table { width:100%; border-collapse:collapse; }");
        out.println("    td { padding:10px; border-bottom:1px solid #eee; }");
        out.println("    td:first-child { font-weight:bold; color:#555; width:50%; }");
        out.println("    .lifecycle { background:#f0f4ff; border-radius:10px; padding:20px; margin-top:20px; }");
        out.println("    .lifecycle h3 { color:#667eea; margin-top:0; }");
        out.println("    .step { margin:8px 0; padding:8px 12px; background:white; border-radius:5px; border-left:3px solid #667eea; }");
        out.println("    a { display:inline-block; margin-top:20px; color:#667eea; text-decoration:none; font-weight:bold; }");
        out.println("  </style>");
        out.println("</head>");
        out.println("<body>");
        out.println("  <div class='card'>");
        out.println("    <span class='badge'>Java EE Servlet Demo</span>");
        out.println("    <h1>🏦 Банковская система — Servlet Info</h1>");
        out.println("    <table>");
        out.println("      <tr><td>HTTP Метод</td><td>GET — мәлімет алу</td></tr>");
        out.println("      <tr><td>Servlet класы</td><td>BankInfoServlet extends HttpServlet</td></tr>");
        out.println("      <tr><td>Іске қосылған уақыт</td><td>" + servletStartTime + "</td></tr>");
        out.println("      <tr><td>Сұрау нөмірі</td><td>" + requestCount + "</td></tr>");
        out.println("      <tr><td>Серверлет URL</td><td>/servlet/info</td></tr>");
        out.println("      <tr><td>Жауап уақыты</td><td>" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "</td></tr>");
        out.println("    </table>");
        out.println("    <div class='lifecycle'>");
        out.println("      <h3>⚙️ Servlet Lifecycle (Тіршілік циклі)</h3>");
        out.println("      <div class='step'>1️⃣ <b>init()</b> — Серверлет іске қосылғанда БІР РЕТ шақырылады</div>");
        out.println("      <div class='step'>2️⃣ <b>service()</b> → <b>doGet() / doPost()</b> — Әр HTTP сұрауда шақырылады</div>");
        out.println("      <div class='step'>3️⃣ <b>destroy()</b> — Серверлет жойылғанда БІР РЕТ шақырылады</div>");
        out.println("    </div>");
        out.println("    <a href='/index.html'>← Басты бетке оралу</a>");
        out.println("  </div>");
        out.println("</body>");
        out.println("</html>");
    }

    /**
     * LIFECYCLE — 2: doPost()
     * HTTP POST → мәлімет жіберу үшін қолданылады.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String name = request.getParameter("name");
        if (name == null || name.isBlank()) name = "Банк клиенті";
        PrintWriter out = response.getWriter();
        out.println("{");
        out.println("  \"message\": \"Сәлем, " + name + "! POST сұрауы қабылданды.\",");
        out.println("  \"method\": \"POST\",");
        out.println("  \"requestCount\": " + (++requestCount) + ",");
        out.println("  \"timestamp\": \"" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\"");
        out.println("}");
    }

    /**
     * LIFECYCLE — 3: destroy()
     * Сервер тоқтаған кезде БІР РЕТ шақырылады — ресурстарды босату.
     */
    @Override
    public void destroy() {
        System.out.println("🛑 BankInfoServlet жойылды. Жалпы сұраулар саны: " + requestCount);
        super.destroy();
    }
}

