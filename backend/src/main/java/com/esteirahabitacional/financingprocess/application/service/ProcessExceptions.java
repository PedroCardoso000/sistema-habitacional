package com.esteirahabitacional.financingprocess.application.service;

import com.esteirahabitacional.shared.ApplicationException;

final class ProcessExceptions {
    private ProcessExceptions() {}
    static ApplicationException notFound() {
        return new ApplicationException(404, "process-not-found", "Processo não encontrado",
                "O processo não existe nesta empresa.");
    }
    static ApplicationException conflict() {
        return new ApplicationException(409, "process-version-conflict", "Conflito de versão",
                "O processo foi alterado por outra operação. Recarregue os dados e tente novamente.");
    }
    static ApplicationException invalid(String detail) {
        return new ApplicationException(422, "invalid-process-draft", "Rascunho inválido", detail);
    }
}
