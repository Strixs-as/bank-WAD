package com.techstore.bank_system.entity;

public enum CardType {
    DEBIT_VISA("Дебетовая Visa"),
    DEBIT_MASTERCARD("Дебетовая Mastercard"),
    DEBIT_MIR("Дебетовая МИР"),
    CREDIT_VISA("Кредитная Visa"),
    CREDIT_MASTERCARD("Кредитная Mastercard"),
    CREDIT_MIR("Кредитная МИР"),
    PREPAID("Предоплаченная карта"),
    VIRTUAL("Виртуальная карта");

    private final String displayName;

    CardType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isCredit() {
        return this == CREDIT_VISA || this == CREDIT_MASTERCARD || this == CREDIT_MIR;
    }

    public boolean isDebit() {
        return this == DEBIT_VISA || this == DEBIT_MASTERCARD || this == DEBIT_MIR;
    }
}

