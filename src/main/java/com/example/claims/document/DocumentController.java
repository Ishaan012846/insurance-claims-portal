package com.example.claims.document;

import com.example.claims.common.exception.ResourceNotFoundException;
import com.example.claims.common.exception.UnauthorizedAccessException;
import com.example.claims.document.dto.ClaimDocumentDto;
import com.example.claims.security.UserPrincipal;
import com.example.claims.user.User;
import com.example.claims.user.UserRepository;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class DocumentController {

    private final DocumentService documentService;
    private final UserRepository userRepository;

    public DocumentController(DocumentService documentService, UserRepository userRepository) {
        this.documentService = documentService;
        this.userRepository = userRepository;
    }

    @PostMapping("/claims/{claimId}/documents")
    public ResponseEntity<ClaimDocumentDto> uploadDocument(
            @PathVariable Long claimId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User actor = getActor(principal);
        ClaimDocumentDto dto = documentService.uploadDocument(claimId, file, actor);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/claims/{claimId}/documents")
    public ResponseEntity<List<ClaimDocumentDto>> getClaimDocuments(
            @PathVariable Long claimId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User actor = getActor(principal);
        List<ClaimDocumentDto> documents = documentService.getClaimDocuments(claimId, actor);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/documents/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long documentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User actor = getActor(principal);
        DocumentService.DownloadResourceWrapper wrapper = documentService.downloadDocument(documentId, actor);

        String contentType = wrapper.contentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + wrapper.fileName() + "\"")
                .body(wrapper.resource());
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
