package com.example.claims.policy;

import com.example.claims.policy.dto.CreatePolicyRequest;
import com.example.claims.policy.dto.PolicyDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PolicyMapper {

    @Mapping(target = "holderId", source = "holder.id")
    @Mapping(target = "holderName", source = "holder.fullName")
    @Mapping(target = "holderEmail", source = "holder.email")
    PolicyDto toDto(Policy policy);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "holder", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Policy toEntity(CreatePolicyRequest request);
}
