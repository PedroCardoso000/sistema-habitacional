package com.esteirahabitacional.documents.config;

import com.esteirahabitacional.documents.adapter.out.persistence.JdbcDocumentAudit;
import com.esteirahabitacional.documents.adapter.out.persistence.JdbcDocumentRepository;
import com.esteirahabitacional.documents.adapter.out.storage.LocalPrivateDocumentStorage;
import com.esteirahabitacional.documents.application.port.in.ManageDocumentsUseCase;
import com.esteirahabitacional.documents.application.port.in.QueryDocumentsUseCase;
import com.esteirahabitacional.documents.application.port.in.SubmitFinancingProcessUseCase;
import com.esteirahabitacional.documents.application.port.out.DocumentAudit;
import com.esteirahabitacional.documents.application.port.out.PrivateDocumentStorage;
import com.esteirahabitacional.documents.application.service.DocumentService;
import com.esteirahabitacional.documents.application.service.ProcessSubmissionService;
import com.esteirahabitacional.financingprocess.ActivateDraftForSubmissionUseCase;
import com.esteirahabitacional.financingprocess.FinancingProcessDocumentLookup;
import com.esteirahabitacional.identityaccess.AuthorizeOrganizationUseCase;
import com.esteirahabitacional.identityaccess.CurrentActorContextUseCase;
import com.esteirahabitacional.shared.CurrentTimeProvider;
import com.esteirahabitacional.shared.IdentifierGenerator;
import com.esteirahabitacional.shared.DomainEventPublisher;
import com.esteirahabitacional.workflow.DefineNextActionForSubmissionUseCase;
import com.esteirahabitacional.workflow.InitializeWorkflowForSubmissionUseCase;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration(proxyBeanMethods = false)
class DocumentsConfiguration {
    @Bean JdbcDocumentRepository documentRepository(JdbcClient jdbc, IdentifierGenerator identifiers) {
        return new JdbcDocumentRepository(jdbc, identifiers);
    }
    @Bean DocumentAudit documentAudit(JdbcClient jdbc, IdentifierGenerator identifiers) {
        return new JdbcDocumentAudit(jdbc, identifiers);
    }
    @Bean PrivateDocumentStorage privateDocumentStorage(
            @Value("${documents.storage.root:${java.io.tmpdir}/esteira-habitacional-private}") String root) {
        return new LocalPrivateDocumentStorage(Path.of(root));
    }
    @Bean DocumentService documentService(CurrentActorContextUseCase currentActor,
            AuthorizeOrganizationUseCase authorization, FinancingProcessDocumentLookup processes,
            JdbcDocumentRepository repository, PrivateDocumentStorage storage, DocumentAudit audit,
            DomainEventPublisher events, IdentifierGenerator identifiers, CurrentTimeProvider time) {
        return new DocumentService(currentActor, authorization, processes, repository, repository,
                repository, repository, storage, audit, events, identifiers, time);
    }
    @Bean ProcessSubmissionService processSubmissionService(AuthorizeOrganizationUseCase authorization,
            FinancingProcessDocumentLookup processes, ActivateDraftForSubmissionUseCase activation,
            InitializeWorkflowForSubmissionUseCase workflow,
            DefineNextActionForSubmissionUseCase nextAction, DocumentService documents) {
        return new ProcessSubmissionService(authorization, processes, activation, workflow, nextAction, documents);
    }
    @Bean SubmitFinancingProcessUseCase submitFinancingProcessUseCase(
            ProcessSubmissionService service, TransactionTemplate transactions) {
        return command -> transactions.execute(status -> service.submit(command));
    }
    @Bean ManageDocumentsUseCase manageDocumentsUseCase(DocumentService service, TransactionTemplate transactions) {
        return new TransactionalManagement(service, transactions);
    }
    @Bean QueryDocumentsUseCase queryDocumentsUseCase(DocumentService service) { return service::list; }

    private record TransactionalManagement(DocumentService service, TransactionTemplate transactions)
            implements ManageDocumentsUseCase {
        @Override public RequestResult request(RequestCommand command) {
            return transactions.execute(status -> service.request(command));
        }
        @Override public UploadResult createUpload(UploadCommand command) {
            return transactions.execute(status -> service.createUpload(command));
        }
        @Override public void storeUpload(StoreUploadCommand command) {
            transactions.executeWithoutResult(status -> service.storeUpload(command));
        }
        @Override public RequestResult completeUpload(CompleteUploadCommand command) {
            return transactions.execute(status -> service.completeUpload(command));
        }
        @Override public RequestResult markUnderReview(MutationCommand command) {
            return transactions.execute(status -> service.markUnderReview(command));
        }
        @Override public RequestResult approve(MutationCommand command) {
            return transactions.execute(status -> service.approve(command));
        }
        @Override public RequestResult reject(RejectCommand command) {
            return transactions.execute(status -> service.reject(command));
        }
        @Override public RequestResult requestResubmission(MutationCommand command) {
            return transactions.execute(status -> service.requestResubmission(command));
        }
        @Override public DownloadResult createDownload(DownloadCommand command) {
            return transactions.execute(status -> service.createDownload(command));
        }
        @Override public DownloadContent download(java.util.UUID grantId, String token) {
            return transactions.execute(status -> service.download(grantId, token));
        }
        @Override public CleanupResult cleanupExpired(int limit) {
            return transactions.execute(status -> service.cleanupExpired(limit));
        }
    }
}
