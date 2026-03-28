package com.techstore.bank_system.entity;

public enum DepositStatus {
    PENDING("Ожидание одобрения"),
    ACTIVE("Активен"),
    CLOSED("Закрыт"),
    REJECTED("Отклонён");

    private final String displayName;

    DepositStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

