package com.esteirahabitacional.financingprocess.application.port.out;

import java.util.UUID;

public interface ProcessNumberGenerator {
    String next(UUID organizationId);
}
