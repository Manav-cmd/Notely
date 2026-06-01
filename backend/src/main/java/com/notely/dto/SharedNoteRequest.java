package com.notely.dto;

import com.notely.entity.SharePermission;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SharedNoteRequest {
    @NotBlank(message = "Recipient email is required")
    @Email(message = "Recipient email must be valid")
    private String email;

    @NotNull(message = "Permission is required")
    private SharePermission permission;
}
