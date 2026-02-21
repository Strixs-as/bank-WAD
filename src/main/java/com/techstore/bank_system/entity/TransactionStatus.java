package com.techstore.bank_system.entity;

public enum TransactionStatus {
    PENDING("Ожидает"),
    PROCESSING("Обрабатывается"),
    COMPLETED("Завершена"),
    FAILED("Ошибка"),
    CANCELLED("Отменена"),
    REVERSED("Отменена с возвратом");

    private final String displayName;

    TransactionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
