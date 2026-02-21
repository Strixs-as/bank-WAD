package com.techstore.bank_system.entity;

public enum LoanStatus {
    PENDING("Ожидание одобрения"),
    APPROVED("Одобрена"),
    REJECTED("Отклонена"),
    ACTIVE("Активна"),
    CLOSED("Закрыта"),
    DEFAULTED("В просрочке");

    private final String displayName;

    LoanStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
