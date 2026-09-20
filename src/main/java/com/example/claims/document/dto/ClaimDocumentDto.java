package com.example.claims.document.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimDocumentDto {

    private Long id;
    private Long claimId;
    private String fileName;
    private String contentType;
    private Long sizeBytes;
    private Instant uploadedAt;
}
