package com.techstore.bank_system.dto;

import jakarta.validation.constraints.NotBlank;

/** Запрос к чат-боту. */
public class ChatRequest {

    @NotBlank
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

