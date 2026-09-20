package com.example.claims.sla.dto;

import com.example.claims.claim.ClaimStatus;
import com.example.claims.claim.dto.ClaimDto;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaMetricsDto {

    private Map<ClaimStatus, Long> statusCounts;
    private long totalClaims;
    private long slaBreachedCount; // UNDER_REVIEW > 5 days
    private long slaAtRiskCount;   // UNDER_REVIEW between 3 and 5 days
    private List<ClaimDto> breachedClaims;
    private List<ClaimDto> atRiskClaims;
}
