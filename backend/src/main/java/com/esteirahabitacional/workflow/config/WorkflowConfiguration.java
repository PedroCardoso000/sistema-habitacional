package com.esteirahabitacional.workflow.config;

import com.esteirahabitacional.financingprocess.FinancingProcessWorkflowLookup;
import com.esteirahabitacional.identityaccess.AuthorizeOrganizationUseCase;
import com.esteirahabitacional.identityaccess.InternalUserReferenceLookup;
import com.esteirahabitacional.shared.CurrentTimeProvider;
import com.esteirahabitacional.shared.DomainEventPublisher;
import com.esteirahabitacional.shared.IdentifierGenerator;
import com.esteirahabitacional.workflow.InitializeWorkflowForSubmissionUseCase;
import com.esteirahabitacional.workflow.DefineNextActionForSubmissionUseCase;
import com.esteirahabitacional.workflow.adapter.out.persistence.JdbcWorkflowAudit;
import com.esteirahabitacional.workflow.adapter.out.persistence.JdbcWorkflowRepository;
import com.esteirahabitacional.workflow.application.port.in.EnsureInitialWorkflowModelUseCase;
import com.esteirahabitacional.workflow.application.port.in.ManageWorkflowJourneyUseCase;
import com.esteirahabitacional.workflow.application.port.in.QueryWorkflowJourneyUseCase;
import com.esteirahabitacional.workflow.application.port.out.WorkflowAudit;
import com.esteirahabitacional.workflow.application.service.WorkflowService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration(proxyBeanMethods = false)
class WorkflowConfiguration {
    @Bean JdbcWorkflowRepository workflowRepository(JdbcClient jdbc) {
        return new JdbcWorkflowRepository(jdbc);
    }
    @Bean WorkflowAudit workflowAudit(JdbcClient jdbc) { return new JdbcWorkflowAudit(jdbc); }

    @Bean
    WorkflowService workflowService(AuthorizeOrganizationUseCase authorization,
            InternalUserReferenceLookup internalUsers, FinancingProcessWorkflowLookup processes,
            JdbcWorkflowRepository repository, WorkflowAudit audit, DomainEventPublisher events,
            IdentifierGenerator identifiers, CurrentTimeProvider time) {
        return new WorkflowService(authorization, internalUsers, processes, repository, repository,
                audit, events, identifiers, time);
    }

    @Bean
    EnsureInitialWorkflowModelUseCase ensureInitialWorkflowModelUseCase(
            WorkflowService service, TransactionTemplate transactions) {
        return organizationId -> transactions.execute(status -> service.ensureInitialModel(organizationId));
    }

    @Bean
    InitializeWorkflowForSubmissionUseCase initializeWorkflowForSubmissionUseCase(
            WorkflowService service, TransactionTemplate transactions) {
        return command -> transactions.execute(status -> service.initialize(command));
    }

    @Bean
    DefineNextActionForSubmissionUseCase defineNextActionForSubmissionUseCase(
            WorkflowService service, TransactionTemplate transactions) {
        return command -> transactions.executeWithoutResult(status -> service.defineNextAction(
                new ManageWorkflowJourneyUseCase.NextActionCommand(command.organizationId(),
                        command.processId(), command.description(), command.responsibleUserId(),
                        command.dueAt(), command.expectedWorkflowVersion())));
    }

    @Bean
    ManageWorkflowJourneyUseCase manageWorkflowJourneyUseCase(
            WorkflowService service, TransactionTemplate transactions) {
        return new TransactionalManagement(service, transactions);
    }

    @Bean
    QueryWorkflowJourneyUseCase queryWorkflowJourneyUseCase(WorkflowService service) {
        return service::find;
    }

    private record TransactionalManagement(WorkflowService service, TransactionTemplate transactions)
            implements ManageWorkflowJourneyUseCase {
        @Override public Result advance(AdvanceCommand command) {
            return transactions.execute(status -> service.advance(command));
        }
        @Override public Result returnStage(MoveCommand command) {
            return transactions.execute(status -> service.returnStage(command));
        }
        @Override public Result moveWithException(MoveCommand command) {
            return transactions.execute(status -> service.moveWithException(command));
        }
        @Override public Result block(BlockCommand command) {
            return transactions.execute(status -> service.block(command));
        }
        @Override public Result unblock(BlockCommand command) {
            return transactions.execute(status -> service.unblock(command));
        }
        @Override public Result defineNextAction(NextActionCommand command) {
            return transactions.execute(status -> service.defineNextAction(command));
        }
    }
}
