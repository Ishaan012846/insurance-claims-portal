package com.example.claims;

import com.example.claims.claim.*;
import com.example.claims.policy.Policy;
import com.example.claims.policy.PolicyRepository;
import com.example.claims.policy.PolicyType;
import com.example.claims.user.Role;
import com.example.claims.user.User;
import com.example.claims.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ClaimRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should save claim and fetch with eager relationships via @EntityGraph")
    void shouldSaveAndFetchClaimWithEntityGraph() {
        User customer = userRepository.save(User.builder()
                .email("customer@example.com")
                .passwordHash("pass")
                .fullName("Customer One")
                .role(Role.CUSTOMER)
                .build());

        User handler = userRepository.save(User.builder()
                .email("handler@example.com")
                .passwordHash("pass")
                .fullName("Handler One")
                .role(Role.HANDLER)
                .build());

        Policy policy = policyRepository.save(Policy.builder()
                .policyNumber("POL-2026-001")
                .type(PolicyType.HEALTH)
                .coverageAmount(new BigDecimal("50000.00"))
                .premium(new BigDecimal("1200.00"))
                .validFrom(LocalDate.of(2026, 1, 1))
                .validTo(LocalDate.of(2026, 12, 31))
                .holder(customer)
                .build());

        Claim claim = Claim.builder()
                .claimNumber("CLM-2026-000001")
                .incidentDate(LocalDate.of(2026, 3, 15))
                .description("Hospitalization expenses")
                .claimedAmount(new BigDecimal("4500.00"))
                .status(ClaimStatus.SUBMITTED)
                .policy(policy)
                .assignedHandler(handler)
                .build();

        Claim saved = claimRepository.save(claim);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getVersion()).isEqualTo(0L);

        Optional<Claim> fetched = claimRepository.findByClaimNumber("CLM-2026-000001");
        assertThat(fetched).isPresent();
        assertThat(fetched.get().getPolicy().getPolicyNumber()).isEqualTo("POL-2026-001");
        assertThat(fetched.get().getPolicy().getHolder().getEmail()).isEqualTo("customer@example.com");
        assertThat(fetched.get().getAssignedHandler().getEmail()).isEqualTo("handler@example.com");
    }

    @Test
    @DisplayName("Should filter claims dynamically using JPA Specification")
    void shouldFilterClaimsUsingSpecification() {
        User customer = userRepository.save(User.builder()
                .email("holder@example.com")
                .passwordHash("pass")
                .fullName("Policy Holder")
                .role(Role.CUSTOMER)
                .build());

        Policy motorPolicy = policyRepository.save(Policy.builder()
                .policyNumber("POL-MOTOR-100")
                .type(PolicyType.MOTOR)
                .coverageAmount(new BigDecimal("20000.00"))
                .premium(new BigDecimal("500.00"))
                .validFrom(LocalDate.of(2026, 1, 1))
                .validTo(LocalDate.of(2026, 12, 31))
                .holder(customer)
                .build());

        Claim claim1 = claimRepository.save(Claim.builder()
                .claimNumber("CLM-2026-000101")
                .incidentDate(LocalDate.of(2026, 2, 10))
                .description("Vehicle dent repair")
                .claimedAmount(new BigDecimal("1500.00"))
                .status(ClaimStatus.UNDER_REVIEW)
                .policy(motorPolicy)
                .build());

        Specification<Claim> spec = ClaimSpecification.filterClaims(
                ClaimStatus.UNDER_REVIEW,
                PolicyType.MOTOR,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 3, 1),
                customer.getId(),
                null
        );

        Page<Claim> results = claimRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(results.getTotalElements()).isEqualTo(1);
        assertThat(results.getContent().get(0).getClaimNumber()).isEqualTo("CLM-2026-000101");
    }
}
