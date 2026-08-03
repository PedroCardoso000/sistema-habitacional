package com.esteirahabitacional.shared.adapter.out;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UuidIdentifierGeneratorTest {

    @Test
    void shouldGenerateDistinctIdentifiers() {
        var generator = new UuidIdentifierGenerator();

        assertThat(generator.generate()).isNotEqualTo(generator.generate());
    }
}

