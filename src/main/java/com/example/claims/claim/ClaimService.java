package com.example.claims.claim;

import com.example.claims.audit.ClaimAuditLog;
import com.example.claims.audit.ClaimAuditLogRepository;
import com.example.claims.audit.dto.ClaimAuditLogDto;
import com.example.claims.claim.dto.ClaimDto;
import com.example.claims.claim.dto.CreateClaimRequest;
import com.example.claims.claim.dto.UpdateClaimStatusRequest;
import com.example.claims.common.exception.BusinessRuleViolationException;
import com.example.claims.common.exception.ResourceNotFoundException;
import com.example.claims.common.exception.UnauthorizedAccessException;
import com.example.claims.policy.Policy;
import com.example.claims.policy.PolicyRepository;
import com.example.claims.policy.PolicyType;
import com.example.claims.user.Role;
import com.example.claims.user.User;
import com.example.claims.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final PolicyRepository policyRepository;
    private final UserRepository userRepository;
    private final ClaimAuditLogRepository auditLogRepository;
    private final ClaimMapper claimMapper;
    private final ClaimAuditLogMapper auditLogMapper;

    public ClaimService(
            ClaimRepository claimRepository,
            PolicyRepository policyRepository,
            UserRepository userRepository,
            ClaimAuditLogRepository auditLogRepository,
            ClaimMapper claimMapper,
            ClaimAuditLogMapper auditLogMapper
    ) {
        this.claimRepository = claimRepository;
        this.policyRepository = policyRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.claimMapper = claimMapper;
        this.auditLogMapper = auditLogMapper;
    }

    @Transactional
    public ClaimDto createClaim(CreateClaimRequest request, User actor) {
        // Business Rule: incidentDate cannot be in the future
        if (request.getIncidentDate().isAfter(LocalDate.now())) {
            throw new BusinessRuleViolationException("Incident date cannot be in the future: " + request.getIncidentDate());
        }

        Policy policy = policyRepository.findById(request.getPolicyId())
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with ID: " + request.getPolicyId()));

        // Authorization Rule: CUSTOMER may only file claims against their own policy
        if (actor.getRole() == Role.CUSTOMER && !policy.getHolder().getId().equals(actor.getId())) {
            throw new UnauthorizedAccessException("You are not authorized to file a claim on this policy.");
        }

        // Business Rule: incidentDate must fall within [policy.validFrom, policy.validTo]
        if (request.getIncidentDate().isBefore(policy.getValidFrom()) || request.getIncidentDate().isAfter(policy.getValidTo())) {
            throw new BusinessRuleViolationException(
                    String.format("Incident date %s must fall within policy coverage period [%s to %s]",
                            request.getIncidentDate(), policy.getValidFrom(), policy.getValidTo())
            );
        }

        // Business Rule: claimedAmount must not exceed policy's coverageAmount
        if (request.getClaimedAmount().compareTo(policy.getCoverageAmount()) > 0) {
            throw new BusinessRuleViolationException(
                    String.format("Claimed amount (%s) exceeds policy coverage amount (%s)",
                            request.getClaimedAmount(), policy.getCoverageAmount())
            );
        }

        Claim claim = claimMapper.toEntity(request);
        claim.setClaimNumber(generateClaimNumber());
        claim.setStatus(ClaimStatus.DRAFT);
        claim.setPolicy(policy);

        Claim savedClaim = claimRepository.save(claim);

        // Record initial creation audit log entry
        writeAuditLog(savedClaim, null, ClaimStatus.DRAFT, actor.getId(), "Claim created in DRAFT status");

        return claimMapper.toDto(savedClaim);
    }

    @Transactional
    public ClaimDto submitClaim(Long claimId, User actor) {
        Claim claim = getClaimEntity(claimId);

        // Authorization check for customer
        validateClaimAccess(claim, actor);

        // Validate state machine transition (DRAFT -> SUBMITTED)
        ClaimStateMachine.validateTransition(claim.getStatus(), ClaimStatus.SUBMITTED);

        ClaimStatus oldStatus = claim.getStatus();
        claim.setStatus(ClaimStatus.SUBMITTED);
        claim.setSubmittedAt(Instant.now());

        Claim saved = claimRepository.save(claim);
        writeAuditLog(saved, oldStatus, ClaimStatus.SUBMITTED, actor.getId(), "Claim submitted for processing");

        return claimMapper.toDto(saved);
    }

    @Transactional
    public ClaimDto updateClaimStatus(Long claimId, UpdateClaimStatusRequest request, User actor) {
        Claim claim = getClaimEntity(claimId);
        ClaimStatus currentStatus = claim.getStatus();
        ClaimStatus targetStatus = request.getStatus();

        // Validate state machine transition
        ClaimStateMachine.validateTransition(currentStatus, targetStatus);

        // Role Permission Rule: Only HANDLER or MANAGER may move a claim out of SUBMITTED
        if (currentStatus == ClaimStatus.SUBMITTED) {
            if (actor.getRole() != Role.HANDLER && actor.getRole() != Role.MANAGER) {
                throw new UnauthorizedAccessException("Only claim handlers or managers can move claims out of SUBMITTED status.");
            }
        }

        // Business Rule: approvedAmount is required when moving to APPROVED and must be <= claimedAmount
        if (targetStatus == ClaimStatus.APPROVED) {
            if (request.getApprovedAmount() == null || request.getApprovedAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessRuleViolationException("Approved amount is required and must be greater than zero when approving a claim.");
            }
            if (request.getApprovedAmount().compareTo(claim.getClaimedAmount()) > 0) {
                throw new BusinessRuleViolationException(
                        String.format("Approved amount (%s) cannot exceed claimed amount (%s).",
                                request.getApprovedAmount(), claim.getClaimedAmount())
                );
            }
            claim.setApprovedAmount(request.getApprovedAmount());
        }

        // Set closed timestamp for terminal states REJECTED or SETTLED
        if (targetStatus == ClaimStatus.REJECTED || targetStatus == ClaimStatus.SETTLED) {
            claim.setClosedAt(Instant.now());
        }

        // If a handler updates claim status, automatically assign them if unassigned
        if (actor.getRole() == Role.HANDLER && claim.getAssignedHandler() == null) {
            claim.setAssignedHandler(actor);
        }

        claim.setStatus(targetStatus);
        Claim saved = claimRepository.save(claim);

        writeAuditLog(saved, currentStatus, targetStatus, actor.getId(), request.getRemarks());

        return claimMapper.toDto(saved);
    }

    @Transactional
    public ClaimDto reassignHandler(Long claimId, Long newHandlerId, User actor) {
        // Business Rule: Only a MANAGER may reassign a claim to a different handler
        if (actor.getRole() != Role.MANAGER) {
            throw new UnauthorizedAccessException("Only managers can reassign claims to handlers.");
        }

        Claim claim = getClaimEntity(claimId);
        User newHandler = userRepository.findById(newHandlerId)
                .orElseThrow(() -> new ResourceNotFoundException("Handler user not found with ID: " + newHandlerId));

        if (newHandler.getRole() != Role.HANDLER && newHandler.getRole() != Role.MANAGER) {
            throw new BusinessRuleViolationException("Target user must be a HANDLER or MANAGER.");
        }

        claim.setAssignedHandler(newHandler);
        Claim saved = claimRepository.save(claim);

        writeAuditLog(saved, claim.getStatus(), claim.getStatus(), actor.getId(),
                "Claim reassigned to handler: " + newHandler.getFullName());

        return claimMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public ClaimDto getClaimById(Long claimId, User actor) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + claimId));
        validateClaimAccess(claim, actor);
        return claimMapper.toDto(claim);
    }

    @Transactional(readOnly = true)
    public Page<ClaimDto> findClaims(
            ClaimStatus status,
            PolicyType policyType,
            LocalDate dateFrom,
            LocalDate dateTo,
            Pageable pageable,
            User actor
    ) {
        Long filterHolderId = null;
        Long filterHandlerId = null;

        if (actor.getRole() == Role.CUSTOMER) {
            filterHolderId = actor.getId();
        } else if (actor.getRole() == Role.HANDLER) {
            // Handlers see assigned claims or unassigned claims submitted for review
            filterHandlerId = actor.getId();
        }

        Specification<Claim> spec = ClaimSpecification.filterClaims(
                status, policyType, dateFrom, dateTo, filterHolderId, filterHandlerId
        );

        return claimRepository.findAll(spec, pageable).map(claimMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<ClaimAuditLogDto> getClaimAuditLogs(Long claimId, User actor) {
        Claim claim = getClaimEntity(claimId);
        validateClaimAccess(claim, actor);

        List<ClaimAuditLog> logs = auditLogRepository.findByClaimIdOrderByTimestampDesc(claimId);
        Map<Long, String> actorNames = userRepository.findAllById(
                logs.stream().map(ClaimAuditLog::getActorId).collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(User::getId, User::getFullName));

        return logs.stream().map(log -> {
            ClaimAuditLogDto dto = auditLogMapper.toDto(log);
            dto.setActorName(actorNames.getOrDefault(log.getActorId(), "System"));
            return dto;
        }).collect(Collectors.toList());
    }

    private Claim getClaimEntity(Long claimId) {
        return claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + claimId));
    }

    private void validateClaimAccess(Claim claim, User actor) {
        if (actor.getRole() == Role.CUSTOMER) {
            if (!claim.getPolicy().getHolder().getId().equals(actor.getId())) {
                throw new UnauthorizedAccessException("You are not authorized to access this claim.");
            }
        }
    }

    private void writeAuditLog(Claim claim, ClaimStatus fromStatus, ClaimStatus toStatus, Long actorId, String remarks) {
        ClaimAuditLog auditLog = ClaimAuditLog.builder()
                .claim(claim)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .actorId(actorId)
                .remarks(remarks)
                .build();
        auditLogRepository.save(auditLog);
    }

    private String generateClaimNumber() {
        int year = Year.now().getValue();
        int randomNum = new Random().nextInt(900000) + 100000;
        String candidate = String.format("CLM-%d-%06d", year, randomNum);
        while (claimRepository.existsByClaimNumber(candidate)) {
            randomNum = new Random().nextInt(900000) + 100000;
            candidate = String.format("CLM-%d-%06d", year, randomNum);
        }
        return candidate;
    }
}
