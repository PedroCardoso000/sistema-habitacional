package com.esteirahabitacional.documents.domain.model;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record DocumentType(UUID id, UUID organizationId, String code, String name,
        Set<String> allowedExtensions, Set<String> allowedContentTypes, long maximumBytes,
        boolean validityRequired) {
    public DocumentType {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(organizationId, "organizationId is required");
        code = required(code, "code").toUpperCase(Locale.ROOT);
        name = required(name, "name");
        allowedExtensions = Set.copyOf(allowedExtensions).stream()
                .map(value -> value.toLowerCase(Locale.ROOT)).collect(java.util.stream.Collectors.toUnmodifiableSet());
        allowedContentTypes = Set.copyOf(allowedContentTypes);
        if (allowedExtensions.isEmpty() || allowedContentTypes.isEmpty() || maximumBytes < 1) {
            throw new IllegalArgumentException("File policy is required");
        }
    }

    public void validate(String fileName, String contentType, long size) {
        String extension = extension(fileName);
        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("File extension is not allowed");
        }
        if (!allowedContentTypes.contains(contentType)) {
            throw new IllegalArgumentException("File content type is not allowed");
        }
        if (size < 1 || size > maximumBytes) {
            throw new IllegalArgumentException("File size is not allowed");
        }
    }

    private static String extension(String name) {
        String value = required(name, "fileName");
        int dot = value.lastIndexOf('.');
        if (dot < 0 || dot == value.length() - 1) {
            throw new IllegalArgumentException("File extension is required");
        }
        return value.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}
