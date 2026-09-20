package com.example.claims.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimAuditLogRepository extends JpaRepository<ClaimAuditLog, Long> {

    List<ClaimAuditLog> findByClaimIdOrderByTimestampDesc(Long claimId);
}
