package com.example.claims.ai.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CopilotQueryRequest {
    @NotBlank(message = "Query cannot be blank")
    private String query;
    private String policyType; // HEALTH, MOTOR, PROPERTY
    private Long claimId;
}
