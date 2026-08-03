package com.esteirahabitacional.documents.application.port.out;

public interface PrivateDocumentStorage {
    StoredObject store(String objectKey, byte[] content, String contentType);
    StoredObject metadata(String objectKey);
    byte[] read(String objectKey);
    void deleteIfExists(String objectKey);

    record StoredObject(String objectKey, String contentType, long sizeBytes, String checksum) {}
}
