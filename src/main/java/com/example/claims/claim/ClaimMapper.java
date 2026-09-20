package com.example.claims.claim;

import com.example.claims.claim.dto.ClaimDto;
import com.example.claims.claim.dto.CreateClaimRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClaimMapper {

    @Mapping(target = "policyId", source = "policy.id")
    @Mapping(target = "policyNumber", source = "policy.policyNumber")
    @Mapping(target = "policyType", source = "policy.type")
    @Mapping(target = "holderId", source = "policy.holder.id")
    @Mapping(target = "holderName", source = "policy.holder.fullName")
    @Mapping(target = "assignedHandlerId", source = "assignedHandler.id")
    @Mapping(target = "assignedHandlerName", source = "assignedHandler.fullName")
    ClaimDto toDto(Claim claim);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "claimNumber", ignore = true)
    @Mapping(target = "approvedAmount", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "policy", ignore = true)
    @Mapping(target = "assignedHandler", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "closedAt", ignore = true)
    Claim toEntity(CreateClaimRequest request);
}
