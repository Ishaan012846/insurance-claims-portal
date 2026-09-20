package com.example.claims.document;

import com.example.claims.claim.Claim;
import com.example.claims.claim.ClaimRepository;
import com.example.claims.common.exception.ResourceNotFoundException;
import com.example.claims.common.exception.UnauthorizedAccessException;
import com.example.claims.document.dto.ClaimDocumentDto;
import com.example.claims.user.Role;
import com.example.claims.user.User;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private final ClaimDocumentRepository documentRepository;
    private final ClaimRepository claimRepository;
    private final DocumentStorageService storageService;
    private final ClaimDocumentMapper documentMapper;

    public DocumentService(
            ClaimDocumentRepository documentRepository,
            ClaimRepository claimRepository,
            DocumentStorageService storageService,
            ClaimDocumentMapper documentMapper
    ) {
        this.documentRepository = documentRepository;
        this.claimRepository = claimRepository;
        this.storageService = storageService;
        this.documentMapper = documentMapper;
    }

    @Transactional
    public ClaimDocumentDto uploadDocument(Long claimId, MultipartFile file, User actor) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + claimId));

        validateClaimAccess(claim, actor);

        String storedFilename = storageService.storeFile(file);

        ClaimDocument document = ClaimDocument.builder()
                .fileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .sizeBytes(file.getSize())
                .storagePath(storedFilename)
                .claim(claim)
                .build();

        ClaimDocument saved = documentRepository.save(document);
        return documentMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<ClaimDocumentDto> getClaimDocuments(Long claimId, User actor) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + claimId));

        validateClaimAccess(claim, actor);

        return documentRepository.findByClaimId(claimId).stream()
                .map(documentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DownloadResourceWrapper downloadDocument(Long documentId, User actor) {
        ClaimDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with ID: " + documentId));

        validateClaimAccess(document.getClaim(), actor);

        Resource resource = storageService.loadAsResource(document.getStoragePath());
        return new DownloadResourceWrapper(resource, document.getFileName(), document.getContentType());
    }

    private void validateClaimAccess(Claim claim, User actor) {
        if (actor.getRole() == Role.CUSTOMER) {
            if (!claim.getPolicy().getHolder().getId().equals(actor.getId())) {
                throw new UnauthorizedAccessException("You are not authorized to access documents for this claim.");
            }
        }
    }

    public record DownloadResourceWrapper(Resource resource, String fileName, String contentType) {}
}
