package com.example.claims.claim.dto;

import com.example.claims.claim.ClaimStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateClaimStatusRequest {

    @NotNull(message = "Status is required")
    private ClaimStatus status;

    private BigDecimal approvedAmount;

    private String remarks;
}
