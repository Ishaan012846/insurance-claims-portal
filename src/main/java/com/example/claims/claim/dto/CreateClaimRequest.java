package com.example.claims.claim.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateClaimRequest {

    @NotNull(message = "Policy ID is required")
    private Long policyId;

    @NotNull(message = "Incident date is required")
    private LocalDate incidentDate;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Claimed amount is required")
    @DecimalMin(value = "0.01", message = "Claimed amount must be greater than zero")
    private BigDecimal claimedAmount;
}
