package com.example.claims.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjudicationRecommendation {
    private Long claimId;
    private String claimNumber;
    private String recommendedStatus; // APPROVE, REJECT, REQUEST_INFO
    private BigDecimal recommendedAmount;
    private BigDecimal claimedAmount;
    private BigDecimal deductedAmount;
    private String rationale;
    private List<String> appliedClauseCodes;
    private List<String> riskFlags;
}
