package com.example.claims.claim;

import com.example.claims.policy.Policy;
import com.example.claims.policy.PolicyType;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ClaimSpecification {

    public static Specification<Claim> filterClaims(
            ClaimStatus status,
            PolicyType policyType,
            LocalDate dateFrom,
            LocalDate dateTo,
            Long policyHolderId,
            Long assignedHandlerId
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (policyType != null) {
                Join<Claim, Policy> policyJoin = root.join("policy");
                predicates.add(cb.equal(policyJoin.get("type"), policyType));
            }

            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("incidentDate"), dateFrom));
            }

            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("incidentDate"), dateTo));
            }

            if (policyHolderId != null) {
                Join<Claim, Policy> policyJoin = root.join("policy");
                predicates.add(cb.equal(policyJoin.get("holder").get("id"), policyHolderId));
            }

            if (assignedHandlerId != null) {
                predicates.add(cb.equal(root.get("assignedHandler").get("id"), assignedHandlerId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
