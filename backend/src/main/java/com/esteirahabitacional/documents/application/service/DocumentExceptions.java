package com.esteirahabitacional.documents.application.service;

import com.esteirahabitacional.shared.ApplicationException;

final class DocumentExceptions {
    private DocumentExceptions() {}
    static ApplicationException invalid(String detail) {
        return new ApplicationException(422, "document-invalid", "Operação documental inválida", detail);
    }
    static ApplicationException notFound() {
        return new ApplicationException(404, "document-not-found", "Documento não encontrado",
                "O recurso documental não existe nesta empresa.");
    }
    static ApplicationException forbidden() {
        return new ApplicationException(403, "document-access-denied", "Acesso negado",
                "O usuário não possui vínculo ou permissão para acessar este documento.");
    }
    static ApplicationException conflict() {
        return new ApplicationException(409, "document-version-conflict", "Conflito de versão",
                "O documento foi alterado por outra operação.");
    }
    static ApplicationException storage() {
        return new ApplicationException(503, "document-storage-failure", "Armazenamento indisponível",
                "Não foi possível confirmar o arquivo no armazenamento privado.");
    }
}
