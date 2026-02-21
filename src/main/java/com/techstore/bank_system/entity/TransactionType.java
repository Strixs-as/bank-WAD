package com.techstore.bank_system.entity;

public enum TransactionType {
    TRANSFER("Перевод"),
    DEPOSIT("Пополнение"),
    WITHDRAWAL("Снятие"),
    LOAN_DISBURSEMENT("Выдача кредита"),
    LOAN_REPAYMENT("Погашение кредита"),
    INTEREST("Проценты"),
    FEE("Комиссия"),
    REFUND("Возврат");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
