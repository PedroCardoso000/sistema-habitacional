package com.esteirahabitacional.documents.application.service;

import com.esteirahabitacional.financingprocess.FinancingProcessDocumentLookup;
import com.esteirahabitacional.identityaccess.AuthorizeOrganizationUseCase;
import com.esteirahabitacional.identityaccess.CurrentActorContextUseCase;
import java.util.UUID;

final class DocumentAccessService {
    private final CurrentActorContextUseCase currentActor;
    private final AuthorizeOrganizationUseCase authorization;
    private final FinancingProcessDocumentLookup processes;

    DocumentAccessService(CurrentActorContextUseCase currentActor,
            AuthorizeOrganizationUseCase authorization, FinancingProcessDocumentLookup processes) {
        this.currentActor = currentActor;
        this.authorization = authorization;
        this.processes = processes;
    }

    Access requireView(UUID organizationId, UUID processId) {
        CurrentActorContextUseCase.Actor actor = currentActor.current();
        ensureTenant(actor, organizationId);
        FinancingProcessDocumentLookup.Reference process = processes.find(organizationId, processId);
        if (isInternal(actor.role())) {
            authorization.require(organizationId, AuthorizeOrganizationUseCase.Action.VIEW_DOCUMENTS);
            return new Access(actor.userId(), actor.role(), process);
        }
        if ((actor.role() == CurrentActorContextUseCase.Role.CLIENT && process.isClient(actor.userId()))
                || (actor.role() == CurrentActorContextUseCase.Role.BROKER && process.isBroker(actor.userId()))) {
            return new Access(actor.userId(), actor.role(), process);
        }
        throw DocumentExceptions.forbidden();
    }

    Access requireManage(UUID organizationId, UUID processId) {
        CurrentActorContextUseCase.Actor actor = currentActor.current();
        ensureTenant(actor, organizationId);
        authorization.require(organizationId, AuthorizeOrganizationUseCase.Action.MANAGE_DOCUMENTS);
        return new Access(actor.userId(), actor.role(), processes.find(organizationId, processId));
    }

    Access requireUpload(UUID organizationId, UUID processId, UUID recipientId) {
        Access access = requireView(organizationId, processId);
        if (isInternal(access.role()) || access.actorId().equals(recipientId)) {
            return access;
        }
        throw DocumentExceptions.forbidden();
    }

    private static boolean isInternal(CurrentActorContextUseCase.Role role) {
        return role == CurrentActorContextUseCase.Role.MANAGER
                || role == CurrentActorContextUseCase.Role.ANALYST;
    }
    private static void ensureTenant(CurrentActorContextUseCase.Actor actor, UUID organizationId) {
        if (!actor.organizationId().equals(organizationId)) {
            throw DocumentExceptions.forbidden();
        }
    }

    record Access(UUID actorId, CurrentActorContextUseCase.Role role,
            FinancingProcessDocumentLookup.Reference process) {}
}
