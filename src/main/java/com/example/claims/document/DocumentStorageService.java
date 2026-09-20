package com.example.claims.document;

import com.example.claims.common.exception.BusinessRuleViolationException;
import com.example.claims.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class DocumentStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/png",
            "image/jpeg",
            "image/jpg"
    );

    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10MB limit

    private final Path rootLocation;

    public DocumentStorageService(@Value("${app.storage.upload-dir:./uploads}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize storage directory at: " + this.rootLocation, e);
        }
    }

    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleViolationException("Uploaded file cannot be empty.");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BusinessRuleViolationException("File size exceeds maximum limit of 10MB.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessRuleViolationException("Invalid file type. Only PDF, PNG, and JPEG documents are permitted.");
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }

        String storedFilename = UUID.randomUUID() + extension;
        Path destinationFile = this.rootLocation.resolve(storedFilename).normalize().toAbsolutePath();

        if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
            throw new BusinessRuleViolationException("Cannot store file outside target directory.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file on disk: " + originalFilename, e);
        }

        return storedFilename;
    }

    public Resource loadAsResource(String filename) {
        try {
            Path file = rootLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("Could not read file resource: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Could not find file resource: " + filename);
        }
    }
}
