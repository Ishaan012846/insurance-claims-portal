package com.example.claims;

import com.example.claims.claim.Claim;
import com.example.claims.claim.ClaimRepository;
import com.example.claims.claim.ClaimStatus;
import com.example.claims.sla.SlaMonitoringScheduler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SlaMonitoringSchedulerTest {

    @Mock
    private ClaimRepository claimRepository;

    @InjectMocks
    private SlaMonitoringScheduler scheduler;

    @Test
    @DisplayName("checkForSlaBreaches should query repository for UNDER_REVIEW claims older than 5 days")
    void checkForSlaBreaches_ShouldQueryBreachedClaims() {
        Claim breachedClaim = Claim.builder()
                .id(1L)
                .claimNumber("CLM-2026-999999")
                .status(ClaimStatus.UNDER_REVIEW)
                .submittedAt(Instant.now().minus(6, ChronoUnit.DAYS))
                .build();

        given(claimRepository.findByStatusAndSubmittedAtBefore(eq(ClaimStatus.UNDER_REVIEW), any(Instant.class)))
                .willReturn(List.of(breachedClaim));

        scheduler.checkForSlaBreaches();

        verify(claimRepository).findByStatusAndSubmittedAtBefore(eq(ClaimStatus.UNDER_REVIEW), any(Instant.class));
    }
}
