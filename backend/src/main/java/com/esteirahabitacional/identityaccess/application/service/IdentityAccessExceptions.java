package com.esteirahabitacional.identityaccess.application.service;

import com.esteirahabitacional.shared.ApplicationException;

final class IdentityAccessExceptions {

    private IdentityAccessExceptions() {}

    static ApplicationException unauthenticated() {
        return new ApplicationException(401, "authentication-required", "Autenticação necessária",
                "O contexto autenticado não foi informado ou é inválido.");
    }

    static ApplicationException forbidden() {
        return new ApplicationException(403, "access-denied", "Acesso negado",
                "O usuário não possui autorização para executar esta ação na empresa.");
    }

    static ApplicationException userNotFound() {
        return new ApplicationException(404, "user-not-found", "Usuário não encontrado",
                "O usuário não foi encontrado na empresa informada.");
    }

    static ApplicationException duplicateEmail() {
        return new ApplicationException(409, "email-already-registered", "E-mail já cadastrado",
                "Já existe um usuário com este e-mail na empresa.");
    }

    static ApplicationException invalidOperation(String detail) {
        return new ApplicationException(422, "invalid-access-operation", "Operação de acesso inválida", detail);
    }
}
