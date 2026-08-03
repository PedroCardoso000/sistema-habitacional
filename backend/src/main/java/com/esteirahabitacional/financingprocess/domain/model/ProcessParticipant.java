package com.esteirahabitacional.financingprocess.domain.model;

import java.util.Objects;
import java.util.UUID;

public record ProcessParticipant(ParticipantType type, UUID participantId) {
    public ProcessParticipant {
        Objects.requireNonNull(type, "type is required");
        Objects.requireNonNull(participantId, "participantId is required");
    }
}
