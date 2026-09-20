package com.example.claims.document;

import com.example.claims.document.dto.ClaimDocumentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClaimDocumentMapper {

    @Mapping(target = "claimId", source = "claim.id")
    ClaimDocumentDto toDto(ClaimDocument document);
}
