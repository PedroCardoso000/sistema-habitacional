package com.esteirahabitacional.workflow.application.service;

import com.esteirahabitacional.shared.ApplicationException;

final class WorkflowExceptions {
    private WorkflowExceptions() {}
    static ApplicationException notFound() {
        return new ApplicationException(404, "workflow-not-found", "Jornada não encontrada",
                "O processo ainda não possui uma jornada operacional nesta empresa.");
    }
    static ApplicationException invalid(String detail) {
        return new ApplicationException(422, "invalid-workflow-transition", "Transição inválida", detail);
    }
    static ApplicationException conflict() {
        return new ApplicationException(409, "workflow-version-conflict", "Conflito de versão",
                "A jornada foi alterada por outra operação. Recarregue os dados e tente novamente.");
    }
}
