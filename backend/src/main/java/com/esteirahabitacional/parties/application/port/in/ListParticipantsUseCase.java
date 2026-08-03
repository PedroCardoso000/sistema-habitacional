package com.esteirahabitacional.parties.application.port.in;

import com.esteirahabitacional.parties.domain.model.PartyStatus;
import java.util.List;
import java.util.UUID;

public interface ListParticipantsUseCase {

    Result execute(Query query);

    enum ParticipantType {
        CLIENT,
        BROKER,
        AGENCY
    }

    record Query(UUID organizationId, ParticipantType type, int page, int size) {}

    record Item(UUID id, ParticipantType type, String name, PartyStatus status) {}

    record Result(List<Item> items, int page, int size, long total) {}
}
