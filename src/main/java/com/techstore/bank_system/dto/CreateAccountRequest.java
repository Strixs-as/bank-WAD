package com.techstore.bank_system.dto;
import lombok.*;
import java.math.BigDecimal;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAccountRequest {
    private String accountType;   // CHECKING, SAVINGS, DEPOSIT, LOAN
    private String currency;      // RUB, USD, EUR, KZT
    private String currencyStr;   // альтернативное поле
    private BigDecimal initialDeposit;
}
