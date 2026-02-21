package com.techstore.bank_system.dto;
import lombok.*;
import java.math.BigDecimal;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDepositRequest {
    private BigDecimal amount;
    private Integer durationMonths;
    private String currency;
    private Long accountId;
}
