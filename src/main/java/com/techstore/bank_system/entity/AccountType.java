package com.techstore.bank_system.entity;

public enum AccountType {
    CHECKING("Текущий счёт"),
    SAVINGS("Сберегательный счёт"),
    DEPOSIT("Депозитный счёт"),
    LOAN("Кредитный счёт");

    private final String displayName;

    AccountType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
