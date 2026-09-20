package com.example.claims.claim;

import com.example.claims.audit.ClaimAuditLog;
import com.example.claims.audit.dto.ClaimAuditLogDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClaimAuditLogMapper {

    @Mapping(target = "claimId", source = "claim.id")
    @Mapping(target = "actorName", ignore = true)
    ClaimAuditLogDto toDto(ClaimAuditLog auditLog);
}
