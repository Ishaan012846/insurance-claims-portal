package com.example.claims;

import com.example.claims.claim.Claim;
import com.example.claims.claim.ClaimRepository;
import com.example.claims.claim.ClaimStatus;
import com.example.claims.policy.Policy;
import com.example.claims.policy.PolicyRepository;
import com.example.claims.policy.PolicyType;
import com.example.claims.user.Role;
import com.example.claims.user.User;
import com.example.claims.user.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ClaimConcurrencyTest extends AbstractRepositoryTest {

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    @DisplayName("Simultaneous updates on same claim must fail with OptimisticLockingFailureException for one transaction")
    void testOptimisticLockingOnConcurrentApprovals() throws Exception {
        // 1. Setup prerequisite entities
        User customer = userRepository.save(User.builder()
                .email("holder.concurrency@example.com")
                .passwordHash("pass")
                .fullName("Holder Concurrency")
                .role(Role.CUSTOMER)
                .build());

        Policy policy = policyRepository.save(Policy.builder()
                .policyNumber("POL-CONC-001")
                .type(PolicyType.HEALTH)
                .coverageAmount(new BigDecimal("10000.00"))
                .premium(new BigDecimal("500.00"))
                .validFrom(LocalDate.of(2026, 1, 1))
                .validTo(LocalDate.of(2026, 12, 31))
                .holder(customer)
                .build());

        Claim claim = claimRepository.save(Claim.builder()
                .claimNumber("CLM-CONC-000001")
                .incidentDate(LocalDate.of(2026, 2, 1))
                .description("Test concurrency claim")
                .claimedAmount(new BigDecimal("2000.00"))
                .status(ClaimStatus.UNDER_REVIEW)
                .policy(policy)
                .build());

        Long claimId = claim.getId();
        assertThat(claim.getVersion()).isEqualTo(0L);

        // 2. Prepare concurrent threads attempting to update the same claim row
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        AtomicReference<Exception> exceptionThread1 = new AtomicReference<>();
        AtomicReference<Exception> exceptionThread2 = new AtomicReference<>();

        Runnable updateTask1 = () -> {
            try {
                startLatch.await();
                EntityManager em = entityManagerFactory.createEntityManager();
                em.getTransaction().begin();

                Claim c1 = em.find(Claim.class, claimId);
                c1.setStatus(ClaimStatus.APPROVED);
                c1.setApprovedAmount(new BigDecimal("2000.00"));

                Thread.sleep(100); // Simulate processing time window
                em.getTransaction().commit();
                em.close();
            } catch (Exception e) {
                exceptionThread1.set(e);
            }
        };

        Runnable updateTask2 = () -> {
            try {
                startLatch.await();
                EntityManager em = entityManagerFactory.createEntityManager();
                em.getTransaction().begin();

                Claim c2 = em.find(Claim.class, claimId);
                c2.setStatus(ClaimStatus.APPROVED);
                c2.setApprovedAmount(new BigDecimal("1800.00"));

                Thread.sleep(100); // Simulate processing time window
                em.getTransaction().commit();
                em.close();
            } catch (Exception e) {
                exceptionThread2.set(e);
            }
        };

        Future<?> f1 = executor.submit(updateTask1);
        Future<?> f2 = executor.submit(updateTask2);

        // Signal both threads to start simultaneously
        startLatch.countDown();

        f1.get();
        f2.get();
        executor.shutdown();

        // 3. Verify that exactly one transaction succeeded and one threw OptimisticLocking failure
        Exception ex1 = exceptionThread1.get();
        Exception ex2 = exceptionThread2.get();

        boolean oneFailedWithOptimisticLock = (ex1 != null && isOptimisticLockException(ex1)) ||
                (ex2 != null && isOptimisticLockException(ex2));

        assertThat(oneFailedWithOptimisticLock)
                .as("One of the concurrent transactions must fail due to @Version optimistic locking mismatch")
                .isTrue();
    }

    private boolean isOptimisticLockException(Exception e) {
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof jakarta.persistence.OptimisticLockException ||
                cause instanceof JpaOptimisticLockingFailureException ||
                cause instanceof org.hibernate.StaleObjectStateException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
