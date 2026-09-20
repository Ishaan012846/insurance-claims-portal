package com.example.claims.policy.dto;

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
public class PolicyDto {

    private Long id;
    private String policyNumber;
    private PolicyType type;
    private BigDecimal coverageAmount;
    private BigDecimal premium;
    private LocalDate validFrom;
    private LocalDate validTo;
    private Long holderId;
    private String holderName;
    private String holderEmail;
    private Instant createdAt;
}
