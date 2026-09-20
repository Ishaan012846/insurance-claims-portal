package com.example.claims.sla;

import com.example.claims.claim.Claim;
import com.example.claims.claim.ClaimMapper;
import com.example.claims.claim.ClaimRepository;
import com.example.claims.claim.ClaimStatus;
import com.example.claims.claim.dto.ClaimDto;
import com.example.claims.sla.dto.SlaMetricsDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SlaService {

    private final ClaimRepository claimRepository;
    private final ClaimMapper claimMapper;

    public SlaService(ClaimRepository claimRepository, ClaimMapper claimMapper) {
        this.claimRepository = claimRepository;
        this.claimMapper = claimMapper;
    }

    @Transactional(readOnly = true)
    public SlaMetricsDto getSlaMetrics() {
        // 1. Calculate status counts
        Map<ClaimStatus, Long> statusCounts = new EnumMap<>(ClaimStatus.class);
        for (ClaimStatus status : ClaimStatus.values()) {
            statusCounts.put(status, 0L);
        }

        List<Object[]> results = claimRepository.countClaimsByStatus();
        long totalClaims = 0;
        for (Object[] row : results) {
            ClaimStatus status = (ClaimStatus) row[0];
            Long count = (Long) row[1];
            statusCounts.put(status, count);
            totalClaims += count;
        }

        // 2. SLA threshold calculations
        Instant now = Instant.now();
        Instant fiveDaysAgo = now.minus(5, ChronoUnit.DAYS);
        Instant threeDaysAgo = now.minus(3, ChronoUnit.DAYS);

        // Fetch claims currently in UNDER_REVIEW status
        List<Claim> underReviewClaims = claimRepository.findByStatusAndSubmittedAtBefore(ClaimStatus.UNDER_REVIEW, now);

        List<ClaimDto> breachedClaims = new ArrayList<>();
        List<ClaimDto> atRiskClaims = new ArrayList<>();

        for (Claim claim : underReviewClaims) {
            Instant submittedAt = claim.getSubmittedAt();
            if (submittedAt != null) {
                if (submittedAt.isBefore(fiveDaysAgo)) {
                    breachedClaims.add(claimMapper.toDto(claim));
                } else if (submittedAt.isBefore(threeDaysAgo)) {
                    atRiskClaims.add(claimMapper.toDto(claim));
                }
            }
        }

        return SlaMetricsDto.builder()
                .statusCounts(statusCounts)
                .totalClaims(totalClaims)
                .slaBreachedCount(breachedClaims.size())
                .slaAtRiskCount(atRiskClaims.size())
                .breachedClaims(breachedClaims)
                .atRiskClaims(atRiskClaims)
                .build();
    }
}
