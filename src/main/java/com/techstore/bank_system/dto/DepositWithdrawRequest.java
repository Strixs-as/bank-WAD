package com.techstore.bank_system.dto;
import lombok.*;
import java.math.BigDecimal;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositWithdrawRequest {
    private String accountNumber;
    private BigDecimal amount;
    private String description;
}
