package com.example.miniwallet.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRequestDto {
    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;
}
