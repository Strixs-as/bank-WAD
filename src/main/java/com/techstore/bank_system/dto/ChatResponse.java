package com.techstore.bank_system.dto;

import java.util.List;

/** Ответ чат-бота. */
public class ChatResponse {

    private String reply;

    /** Быстрые кнопки-подсказки (необязательно). */
    private List<String> quickReplies;

    public ChatResponse() {
    }

    public ChatResponse(String reply, List<String> quickReplies) {
        this.reply = reply;
        this.quickReplies = quickReplies;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public List<String> getQuickReplies() {
        return quickReplies;
    }

    public void setQuickReplies(List<String> quickReplies) {
        this.quickReplies = quickReplies;
    }
}

