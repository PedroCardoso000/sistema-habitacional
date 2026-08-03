package com.esteirahabitacional.documents.adapter.out.storage;

import com.esteirahabitacional.documents.application.port.out.PrivateDocumentStorage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class LocalPrivateDocumentStorage implements PrivateDocumentStorage {
    private final Path root;

    public LocalPrivateDocumentStorage(Path root) {
        this.root = root.toAbsolutePath().normalize();
    }

    @Override
    public StoredObject store(String objectKey, byte[] content, String contentType) {
        String detected = detect(content);
        if (!detected.equals(contentType)) {
            throw new IllegalArgumentException("File content does not match content type");
        }
        Path target = resolve(objectKey);
        try {
            Files.createDirectories(target.getParent());
            Path temporary = Files.createTempFile(target.getParent(), ".upload-", ".tmp");
            Files.write(temporary, content);
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.AtomicMoveNotSupportedException exception) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return new StoredObject(objectKey, detected, content.length, checksum(content));
        } catch (IOException exception) {
            throw new IllegalStateException("Private storage write failed", exception);
        }
    }

    @Override
    public StoredObject metadata(String objectKey) {
        byte[] content = read(objectKey);
        return new StoredObject(objectKey, detect(content), content.length, checksum(content));
    }

    @Override
    public byte[] read(String objectKey) {
        try {
            Path path = resolve(objectKey);
            if (!Files.isRegularFile(path)) {
                throw new IllegalStateException("Stored object was not found");
            }
            return Files.readAllBytes(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Private storage read failed", exception);
        }
    }

    @Override
    public void deleteIfExists(String objectKey) {
        try {
            Files.deleteIfExists(resolve(objectKey));
        } catch (IOException exception) {
            throw new IllegalStateException("Private storage cleanup failed", exception);
        }
    }

    private Path resolve(String objectKey) {
        Path resolved = root.resolve(objectKey).normalize();
        if (!resolved.startsWith(root)) {
            throw new IllegalArgumentException("Invalid storage key");
        }
        return resolved;
    }
    private static String detect(byte[] content) {
        if (content.length >= 5 && content[0] == '%' && content[1] == 'P'
                && content[2] == 'D' && content[3] == 'F' && content[4] == '-') {
            return "application/pdf";
        }
        if (content.length >= 3 && (content[0] & 0xff) == 0xff
                && (content[1] & 0xff) == 0xd8 && (content[2] & 0xff) == 0xff) {
            return "image/jpeg";
        }
        if (content.length >= 8 && (content[0] & 0xff) == 0x89 && content[1] == 'P'
                && content[2] == 'N' && content[3] == 'G') {
            return "image/png";
        }
        throw new IllegalArgumentException("File content is not an accepted document format");
    }
    private static String checksum(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
