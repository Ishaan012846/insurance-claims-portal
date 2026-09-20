package com.example.claims.policy;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {

    // Eagerly fetch holder using @EntityGraph to avoid N+1 queries when mapping policies with their holder details
    @EntityGraph(attributePaths = {"holder"})
    Optional<Policy> findByPolicyNumber(String policyNumber);

    // Eagerly fetch holder using @EntityGraph for list queries by holder ID
    @EntityGraph(attributePaths = {"holder"})
    Page<Policy> findByHolderId(Long holderId, Pageable pageable);

    boolean existsByPolicyNumber(String policyNumber);
}
