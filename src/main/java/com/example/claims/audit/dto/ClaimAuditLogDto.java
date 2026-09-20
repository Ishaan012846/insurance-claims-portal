package com.example.claims.audit.dto;

import com.example.claims.claim.ClaimStatus;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimAuditLogDto {

    private Long id;
    private Long claimId;
    private ClaimStatus fromStatus;
    private ClaimStatus toStatus;
    private Long actorId;
    private String actorName;
    private String remarks;
    private Instant timestamp;
}
