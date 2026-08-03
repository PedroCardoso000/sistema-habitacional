package com.esteirahabitacional.parties.domain.model;

public record Cpf(String value) {

    public Cpf {
        value = value == null ? "" : value.replaceAll("\\D", "");
        if (!BrazilianTaxIdValidator.isValidCpf(value)) {
            throw new IllegalArgumentException("A valid CPF is required");
        }
    }
}
