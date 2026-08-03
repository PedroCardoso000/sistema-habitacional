package com.esteirahabitacional.documents.config;

import com.esteirahabitacional.documents.application.port.in.ManageDocumentsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
final class DocumentCleanupJob {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentCleanupJob.class);
    private final ManageDocumentsUseCase documents;

    DocumentCleanupJob(ManageDocumentsUseCase documents) { this.documents = documents; }

    @Scheduled(initialDelayString = "${documents.cleanup.initial-delay:PT1H}",
            fixedDelayString = "${documents.cleanup.fixed-delay:PT1H}")
    void cleanup() {
        ManageDocumentsUseCase.CleanupResult result = documents.cleanupExpired(100);
        LOGGER.info("Document orphan cleanup completed: processed={}, objectsRemoved={}",
                result.processed(), result.objectsRemoved());
    }
}
