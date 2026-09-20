package com.example.claims.policy;

import com.example.claims.common.exception.BusinessRuleViolationException;
import com.example.claims.common.exception.ResourceNotFoundException;
import com.example.claims.policy.dto.CreatePolicyRequest;
import com.example.claims.policy.dto.PolicyDto;
import com.example.claims.user.User;
import com.example.claims.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final UserRepository userRepository;
    private final PolicyMapper policyMapper;

    public PolicyService(PolicyRepository policyRepository, UserRepository userRepository, PolicyMapper policyMapper) {
        this.policyRepository = policyRepository;
        this.userRepository = userRepository;
        this.policyMapper = policyMapper;
    }

    @Transactional
    public PolicyDto createPolicy(CreatePolicyRequest request) {
        if (policyRepository.existsByPolicyNumber(request.getPolicyNumber())) {
            throw new BusinessRuleViolationException("Policy number already exists: " + request.getPolicyNumber());
        }

        if (request.getValidTo().isBefore(request.getValidFrom())) {
            throw new BusinessRuleViolationException("Policy validTo date cannot be before validFrom date.");
        }

        User holder = userRepository.findById(request.getHolderId())
                .orElseThrow(() -> new ResourceNotFoundException("Policy holder user not found with ID: " + request.getHolderId()));

        Policy policy = policyMapper.toEntity(request);
        policy.setHolder(holder);

        Policy savedPolicy = policyRepository.save(policy);
        return policyMapper.toDto(savedPolicy);
    }

    @Transactional(readOnly = true)
    public PolicyDto getPolicyById(Long id) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with ID: " + id));
        return policyMapper.toDto(policy);
    }

    @Transactional(readOnly = true)
    public Page<PolicyDto> getPolicies(Long holderId, Pageable pageable) {
        if (holderId != null) {
            return policyRepository.findByHolderId(holderId, pageable).map(policyMapper::toDto);
        }
        return policyRepository.findAll(pageable).map(policyMapper::toDto);
    }
}
