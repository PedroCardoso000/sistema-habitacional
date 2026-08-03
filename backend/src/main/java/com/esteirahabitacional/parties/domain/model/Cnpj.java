package com.esteirahabitacional.parties.domain.model;

public record Cnpj(String value) {

    public Cnpj {
        value = value == null ? "" : value.replaceAll("\\D", "");
        if (!BrazilianTaxIdValidator.isValidCnpj(value)) {
            throw new IllegalArgumentException("A valid CNPJ is required");
        }
    }
}
