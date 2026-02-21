package com.techstore.bank_system.entity;

public enum CurrencyType {
    KZT("Казахстанский тенге", "₸"),
    USD("Доллар США", "$"),
    EUR("Евро", "€"),
    RUB("Российский рубль", "₽");

    private final String displayName;
    private final String symbol;

    CurrencyType(String displayName, String symbol) {
        this.displayName = displayName;
        this.symbol = symbol;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSymbol() {
        return symbol;
    }
}
