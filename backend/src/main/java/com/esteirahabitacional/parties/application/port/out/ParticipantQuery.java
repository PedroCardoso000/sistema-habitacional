package com.esteirahabitacional.parties.application.port.out;

import com.esteirahabitacional.parties.application.port.in.ListParticipantsUseCase.ParticipantType;
import com.esteirahabitacional.parties.domain.model.PartyStatus;
import java.util.List;
import java.util.UUID;

public interface ParticipantQuery {

    Page find(UUID organizationId, ParticipantType type, int page, int size);

    record Row(UUID id, ParticipantType type, String name, PartyStatus status) {}

    record Page(List<Row> rows, long total) {}
}
