package com.esteirahabitacional.parties.application.service;

import com.esteirahabitacional.shared.ApplicationException;

final class PartyExceptions {

    private PartyExceptions() {}

    static ApplicationException duplicate(String type) {
        return new ApplicationException(409, "party-already-registered", "Participante já cadastrado",
                "Já existe " + type + " com o identificador informado nesta empresa.");
    }

    static ApplicationException notFound(String type) {
        return new ApplicationException(404, "party-not-found", "Participante não encontrado",
                type + " não foi encontrado nesta empresa.");
    }

    static ApplicationException invalid(String detail) {
        return new ApplicationException(422, "invalid-party", "Participante inválido", detail);
    }
}
