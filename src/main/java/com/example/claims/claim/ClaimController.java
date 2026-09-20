package com.example.claims.claim;

import com.example.claims.audit.dto.ClaimAuditLogDto;
import com.example.claims.claim.dto.ClaimDto;
import com.example.claims.claim.dto.CreateClaimRequest;
import com.example.claims.claim.dto.ReassignHandlerRequest;
import com.example.claims.claim.dto.UpdateClaimStatusRequest;
import com.example.claims.common.exception.ResourceNotFoundException;
import com.example.claims.common.exception.UnauthorizedAccessException;
import com.example.claims.policy.PolicyType;
import com.example.claims.security.UserPrincipal;
import com.example.claims.user.User;
import com.example.claims.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/claims")
public class ClaimController {

    private final ClaimService claimService;
    private final UserRepository userRepository;

    public ClaimController(ClaimService claimService, UserRepository userRepository) {
        this.claimService = claimService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ClaimDto> createClaim(
            @Valid @RequestBody CreateClaimRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User actor = getActor(principal);
        ClaimDto createdClaim = claimService.createClaim(request, actor);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdClaim);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ClaimDto> submitClaim(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User actor = getActor(principal);
        ClaimDto submittedClaim = claimService.submitClaim(id, actor);
        return ResponseEntity.ok(submittedClaim);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('HANDLER', 'MANAGER')")
    public ResponseEntity<ClaimDto> updateClaimStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateClaimStatusRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User actor = getActor(principal);
        ClaimDto updatedClaim = claimService.updateClaimStatus(id, request, actor);
        return ResponseEntity.ok(updatedClaim);
    }

    @PatchMapping("/{id}/reassign")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ClaimDto> reassignHandler(
            @PathVariable Long id,
            @Valid @RequestBody ReassignHandlerRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User actor = getActor(principal);
        ClaimDto reassignedClaim = claimService.reassignHandler(id, request.getHandlerId(), actor);
        return ResponseEntity.ok(reassignedClaim);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClaimDto> getClaimById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User actor = getActor(principal);
        ClaimDto claim = claimService.getClaimById(id, actor);
        return ResponseEntity.ok(claim);
    }

    @GetMapping
    public ResponseEntity<Page<ClaimDto>> getClaims(
            @RequestParam(required = false) ClaimStatus status,
            @RequestParam(required = false) PolicyType policyType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User actor = getActor(principal);
        Page<ClaimDto> claims = claimService.findClaims(status, policyType, dateFrom, dateTo, pageable, actor);
        return ResponseEntity.ok(claims);
    }

    @GetMapping("/{id}/audit-logs")
    public ResponseEntity<List<ClaimAuditLogDto>> getClaimAuditLogs(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User actor = getActor(principal);
        List<ClaimAuditLogDto> auditLogs = claimService.getClaimAuditLogs(id, actor);
        return ResponseEntity.ok(auditLogs);
    }

    private User getActor(UserPrincipal principal) {
        if (principal != null) {
            return userRepository.findById(principal.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + principal.getId()));
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            if (authentication.getPrincipal() instanceof UserPrincipal up) {
                return userRepository.findById(up.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + up.getId()));
            }
            if (authentication.getName() != null) {
                return userRepository.findByEmail(authentication.getName())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + authentication.getName()));
            }
        }
        throw new UnauthorizedAccessException("Authentication required to perform this action.");
    }
}
