package com.esteirahabitacional.documents.application.service;

import com.esteirahabitacional.documents.application.port.in.SubmitFinancingProcessUseCase;
import com.esteirahabitacional.financingprocess.ActivateDraftForSubmissionUseCase;
import com.esteirahabitacional.financingprocess.FinancingProcessDocumentLookup;
import com.esteirahabitacional.identityaccess.AuthorizeOrganizationUseCase;
import com.esteirahabitacional.workflow.DefineNextActionForSubmissionUseCase;
import com.esteirahabitacional.workflow.InitializeWorkflowForSubmissionUseCase;

public final class ProcessSubmissionService {
    private final AuthorizeOrganizationUseCase authorization;
    private final FinancingProcessDocumentLookup processes;
    private final ActivateDraftForSubmissionUseCase activation;
    private final InitializeWorkflowForSubmissionUseCase workflow;
    private final DefineNextActionForSubmissionUseCase nextAction;
    private final DocumentService documents;

    public ProcessSubmissionService(AuthorizeOrganizationUseCase authorization,
            FinancingProcessDocumentLookup processes, ActivateDraftForSubmissionUseCase activation,
            InitializeWorkflowForSubmissionUseCase workflow,
            DefineNextActionForSubmissionUseCase nextAction, DocumentService documents) {
        this.authorization = authorization;
        this.processes = processes;
        this.activation = activation;
        this.workflow = workflow;
        this.nextAction = nextAction;
        this.documents = documents;
    }

    public SubmitFinancingProcessUseCase.Result submit(SubmitFinancingProcessUseCase.Command command) {
        var actor = authorization.require(command.organizationId(),
                AuthorizeOrganizationUseCase.Action.MANAGE_PROCESSES);
        var before = processes.find(command.organizationId(), command.processId());
        if (before.lifecycle() != FinancingProcessDocumentLookup.Lifecycle.DRAFT) {
            throw DocumentExceptions.invalid("Only a draft process can be submitted");
        }
        var active = activation.activate(new ActivateDraftForSubmissionUseCase.Command(
                command.organizationId(), command.processId(), command.expectedVersion(), actor.userId()));
        var initialized = workflow.initialize(new InitializeWorkflowForSubmissionUseCase.Command(
                command.organizationId(), command.processId(), actor.userId()));
        var checklist = documents.generateChecklist(command.organizationId(), command.processId(),
                active.mainClientId(), actor.userId());
        nextAction.define(new DefineNextActionForSubmissionUseCase.Command(command.organizationId(),
                command.processId(), "Realizar triagem inicial", active.responsibleUserId(), null, 0));
        return new SubmitFinancingProcessUseCase.Result(command.processId(), "ACTIVE",
                initialized.currentStageCode(), checklist.size(), active.version());
    }
}
