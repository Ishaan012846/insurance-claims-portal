package com.example.claims.claim.dto;

import com.example.claims.claim.ClaimStatus;
import com.example.claims.policy.PolicyType;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimDto {

    private Long id;
    private String claimNumber;
    private LocalDate incidentDate;
    private String description;
    private BigDecimal claimedAmount;
    private BigDecimal approvedAmount;
    private ClaimStatus status;
    private Long policyId;
    private String policyNumber;
    private PolicyType policyType;
    private Long holderId;
    private String holderName;
    private Long assignedHandlerId;
    private String assignedHandlerName;
    private Long version;
    private Instant createdAt;
    private Instant submittedAt;
    private Instant closedAt;
}
