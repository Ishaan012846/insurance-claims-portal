package com.example.claims.policy;

import com.example.claims.policy.dto.CreatePolicyRequest;
import com.example.claims.policy.dto.PolicyDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/policies")
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'HANDLER')")
    public ResponseEntity<PolicyDto> createPolicy(@Valid @RequestBody CreatePolicyRequest request) {
        PolicyDto createdPolicy = policyService.createPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPolicy);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PolicyDto> getPolicyById(@PathVariable Long id) {
        PolicyDto policy = policyService.getPolicyById(id);
        return ResponseEntity.ok(policy);
    }

    @GetMapping
    public ResponseEntity<Page<PolicyDto>> getPolicies(
            @RequestParam(required = false) Long holderId,
            Pageable pageable
    ) {
        Page<PolicyDto> policies = policyService.getPolicies(holderId, pageable);
        return ResponseEntity.ok(policies);
    }
}
