package com.techstore.bank_system.entity;

public enum LoanType {
    PERSONAL("Потребительский кредит"),
    MORTGAGE("Ипотека"),
    AUTO("Автокредит"),
    BUSINESS("Бизнес кредит"),
    STUDENT("Студенческий кредит"),
    CREDIT_LINE("Кредитная линия");

    private final String displayName;

    LoanType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

