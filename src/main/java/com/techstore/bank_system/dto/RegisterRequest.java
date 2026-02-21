package com.techstore.bank_system.dto;
import lombok.*;
import java.time.LocalDate;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    private String firstName;
    private String lastName;
    private String patronymic;
    private String email;
    private String password;
    private String phoneNumber;
    private String passportNumber;
    private LocalDate dateOfBirth;
    private String address;
}
