package com.esteirahabitacional.shared.adapter.out;

import com.esteirahabitacional.shared.IdentifierGenerator;
import java.util.UUID;

public final class UuidIdentifierGenerator implements IdentifierGenerator {

    @Override
    public UUID generate() {
        return UUID.randomUUID();
    }
}
