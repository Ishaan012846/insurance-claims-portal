package com.example.claims.claim;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long>, JpaSpecificationExecutor<Claim> {

    // Eagerly fetch policy, holder, and assignedHandler using @EntityGraph to avoid N+1 queries when retrieving claim details
    @EntityGraph(attributePaths = {"policy", "policy.holder", "assignedHandler"})
    Optional<Claim> findByClaimNumber(String claimNumber);

    // Eagerly fetch graph by ID
    @Override
    @EntityGraph(attributePaths = {"policy", "policy.holder", "assignedHandler"})
    Optional<Claim> findById(Long id);

    // Eagerly fetch relationships when executing specification page queries
    @Override
    @EntityGraph(attributePaths = {"policy", "policy.holder", "assignedHandler"})
    Page<Claim> findAll(Specification<Claim> spec, Pageable pageable);

    // Query for SLA monitoring background job: claims in UNDER_REVIEW status submitted before a cutoff timestamp
    @EntityGraph(attributePaths = {"policy", "assignedHandler"})
    List<Claim> findByStatusAndSubmittedAtBefore(ClaimStatus status, Instant cutoffTime);

    // Count claims grouped by status for SLA/Dashboard metrics
    @Query("SELECT c.status, COUNT(c) FROM Claim c GROUP BY c.status")
    List<Object[]> countClaimsByStatus();

    // Check if claim exists by claim number
    boolean existsByClaimNumber(String claimNumber);
}
