package com.techstore.bank_system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnlockConfirmRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String code;
}

