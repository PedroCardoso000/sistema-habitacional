package com.esteirahabitacional.parties.application.service;

import com.esteirahabitacional.identityaccess.AuthorizeOrganizationUseCase;
import com.esteirahabitacional.identityaccess.AuthorizeOrganizationUseCase.Action;
import com.esteirahabitacional.parties.application.port.in.ListParticipantsUseCase;
import com.esteirahabitacional.parties.application.port.out.ParticipantQuery;

public class ParticipantQueryService implements ListParticipantsUseCase {

    private static final int MAXIMUM_PAGE_SIZE = 100;
    private final AuthorizeOrganizationUseCase authorization;
    private final ParticipantQuery participants;

    public ParticipantQueryService(
            AuthorizeOrganizationUseCase authorization, ParticipantQuery participants) {
        this.authorization = authorization;
        this.participants = participants;
    }

    @Override
    public Result execute(Query query) {
        authorization.require(query.organizationId(), Action.VIEW_PARTIES);
        if (query.page() < 0 || query.size() < 1 || query.size() > MAXIMUM_PAGE_SIZE) {
            throw PartyExceptions.invalid("Page must be non-negative and size must be between 1 and 100");
        }
        ParticipantQuery.Page page = participants.find(
                query.organizationId(), query.type(), query.page(), query.size());
        return new Result(
                page.rows().stream()
                        .map(row -> new Item(row.id(), row.type(), row.name(), row.status()))
                        .toList(),
                query.page(),
                query.size(),
                page.total());
    }
}
