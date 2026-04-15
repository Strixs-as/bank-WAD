package com.techstore.bank_system.config;

/**
 * Мини-контекст текущего HTTP-запроса (IP + User-Agent) для email-уведомлений.
 * Реализовано через ThreadLocal.
 */
public final class RequestContext {

    private static final ThreadLocal<String> CLIENT_IP = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_AGENT = new ThreadLocal<>();

    private RequestContext() {}

    public static void set(String ip, String userAgent) {
        CLIENT_IP.set(ip);
        USER_AGENT.set(userAgent);
    }

    public static String getClientIp() {
        return CLIENT_IP.get();
    }

    public static String getUserAgent() {
        return USER_AGENT.get();
    }

    public static void clear() {
        CLIENT_IP.remove();
        USER_AGENT.remove();
    }
}

