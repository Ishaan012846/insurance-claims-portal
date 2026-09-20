package com.example.claims.sla;

import com.example.claims.claim.Claim;
import com.example.claims.claim.ClaimRepository;
import com.example.claims.claim.ClaimStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class SlaMonitoringScheduler {

    private static final Logger log = LoggerFactory.getLogger(SlaMonitoringScheduler.class);
    private static final long SLA_THRESHOLD_DAYS = 5;

    private final ClaimRepository claimRepository;

    public SlaMonitoringScheduler(ClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }

    // Runs every hour by default (configurable via app.sla.cron)
    @Scheduled(cron = "${app.sla.cron:0 0 * * * *}")
    @Transactional(readOnly = true)
    public void checkForSlaBreaches() {
        log.info("Starting scheduled SLA breach monitoring job...");

        Instant cutoffTime = Instant.now().minus(SLA_THRESHOLD_DAYS, ChronoUnit.DAYS);
        List<Claim> breachedClaims = claimRepository.findByStatusAndSubmittedAtBefore(
                ClaimStatus.UNDER_REVIEW,
                cutoffTime
        );

        if (breachedClaims.isEmpty()) {
            log.info("SLA Monitoring Job completed: No claims breaching 5-day turnaround time.");
            return;
        }

        log.warn("SLA MONITORING ALERT: Found {} claim(s) breaching 5-day SLA threshold in UNDER_REVIEW status!",
                breachedClaims.size());

        for (Claim claim : breachedClaims) {
            long daysOverdue = ChronoUnit.DAYS.between(claim.getSubmittedAt(), Instant.now());
            log.warn("SLA BREACH DETECTED - Claim Number: {}, Submitted At: {}, Days Overdue: {}, Handler: {}",
                    claim.getClaimNumber(),
                    claim.getSubmittedAt(),
                    daysOverdue,
                    claim.getAssignedHandler() != null ? claim.getAssignedHandler().getFullName() : "UNASSIGNED"
            );
        }

        log.info("SLA Monitoring Job completed. Processed {} breached claim notifications.", breachedClaims.size());
    }
}
