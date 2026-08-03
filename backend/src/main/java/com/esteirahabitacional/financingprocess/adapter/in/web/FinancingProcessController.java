package com.esteirahabitacional.financingprocess.adapter.in.web;

import com.esteirahabitacional.financingprocess.application.port.in.ManageFinancingProcessUseCase;
import com.esteirahabitacional.financingprocess.application.port.in.QueryFinancingProcessUseCase;
import com.esteirahabitacional.financingprocess.domain.model.ParticipantType;
import com.esteirahabitacional.financingprocess.domain.model.ProcessOrigin;
import com.esteirahabitacional.financingprocess.domain.model.ProcessPriority;
import com.esteirahabitacional.financingprocess.domain.model.ProcessStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organizations/{organizationId}/processes")
class FinancingProcessController {
    private final ManageFinancingProcessUseCase management;
    private final QueryFinancingProcessUseCase queries;
    FinancingProcessController(ManageFinancingProcessUseCase management, QueryFinancingProcessUseCase queries) {
        this.management = management;
        this.queries = queries;
    }

    @PostMapping
    ResponseEntity<MutationResponse> create(@PathVariable UUID organizationId,
            @Valid @RequestBody CreateRequest request) {
        var result = management.create(new ManageFinancingProcessUseCase.CreateCommand(
                organizationId, request.origin(), request.brokerId(), request.mainClientId()));
        return ResponseEntity.created(URI.create("/api/organizations/" + organizationId + "/processes/" + result.id()))
                .body(MutationResponse.from(result));
    }

    @GetMapping("/{processId}")
    DetailResponse find(@PathVariable UUID organizationId, @PathVariable UUID processId) {
        return DetailResponse.from(queries.find(organizationId, processId));
    }

    @GetMapping
    PageResponse list(@PathVariable UUID organizationId,
            @RequestParam(required = false) ProcessOrigin origin,
            @RequestParam(required = false) ProcessPriority priority,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return PageResponse.from(queries.list(
                new QueryFinancingProcessUseCase.ListQuery(organizationId, origin, priority, page, size)));
    }

    @PatchMapping("/{processId}/main-client")
    MutationResponse defineMainClient(@PathVariable UUID organizationId, @PathVariable UUID processId,
            @Valid @RequestBody MainClientRequest request) {
        return MutationResponse.from(management.defineMainClient(new ManageFinancingProcessUseCase.MainClientCommand(
                organizationId, processId, request.clientId(), request.expectedVersion())));
    }

    @PutMapping("/{processId}/participants")
    MutationResponse associateParticipant(@PathVariable UUID organizationId, @PathVariable UUID processId,
            @Valid @RequestBody ParticipantRequest request) {
        return MutationResponse.from(management.associateParticipant(
                new ManageFinancingProcessUseCase.ParticipantCommand(organizationId, processId,
                        request.type(), request.participantId(), request.expectedVersion())));
    }

    @PutMapping("/{processId}/property")
    MutationResponse associateProperty(@PathVariable UUID organizationId, @PathVariable UUID processId,
            @Valid @RequestBody PropertyRequest request) {
        return MutationResponse.from(management.associateProperty(new ManageFinancingProcessUseCase.PropertyCommand(
                organizationId, processId, request.addressLine(), request.city(), request.state(),
                request.postalCode(), request.expectedVersion())));
    }

    @PatchMapping("/{processId}/priority")
    MutationResponse changePriority(@PathVariable UUID organizationId, @PathVariable UUID processId,
            @Valid @RequestBody PriorityRequest request) {
        return MutationResponse.from(management.changePriority(new ManageFinancingProcessUseCase.PriorityCommand(
                organizationId, processId, request.priority(), request.expectedVersion())));
    }

    record CreateRequest(@NotNull ProcessOrigin origin, UUID brokerId, UUID mainClientId) {}
    record MainClientRequest(@NotNull UUID clientId, @PositiveOrZero long expectedVersion) {}
    record ParticipantRequest(@NotNull ParticipantType type, @NotNull UUID participantId,
                              @PositiveOrZero long expectedVersion) {}
    record PropertyRequest(@NotBlank String addressLine, @NotBlank String city, @NotBlank String state,
                           @NotBlank String postalCode, @PositiveOrZero long expectedVersion) {}
    record PriorityRequest(@NotNull ProcessPriority priority, @PositiveOrZero long expectedVersion) {}
    record MutationResponse(UUID id, String processNumber, long version) {
        static MutationResponse from(ManageFinancingProcessUseCase.Result result) {
            return new MutationResponse(result.id(), result.processNumber(), result.version());
        }
    }
    record ParticipantResponse(ParticipantType type, UUID participantId) {}
    record PropertyResponse(int sequence, String addressLine, String city, String state, String postalCode,
                            UUID associatedBy, Instant associatedAt) {}
    record DetailResponse(UUID id, String processNumber, ProcessStatus status, ProcessOrigin origin,
                          UUID authorUserId, UUID brokerId, UUID responsibleUserId, UUID mainClientId,
                          ProcessPriority priority, List<ParticipantResponse> participants,
                          List<PropertyResponse> propertyHistory, long version, Instant createdAt, Instant updatedAt) {
        static DetailResponse from(QueryFinancingProcessUseCase.Detail detail) {
            return new DetailResponse(detail.id(), detail.processNumber(), detail.status(), detail.origin(),
                    detail.authorUserId(), detail.brokerId(), detail.responsibleUserId(), detail.mainClientId(),
                    detail.priority(), detail.participants().stream()
                            .map(item -> new ParticipantResponse(item.type(), item.participantId())).toList(),
                    detail.propertyHistory().stream().map(item -> new PropertyResponse(item.sequence(),
                            item.addressLine(), item.city(), item.state(), item.postalCode(),
                            item.associatedBy(), item.associatedAt())).toList(), detail.version(),
                    detail.createdAt(), detail.updatedAt());
        }
    }
    record PageResponse(List<DetailResponse> items, int page, int size, long total) {
        static PageResponse from(QueryFinancingProcessUseCase.Page page) {
            return new PageResponse(page.items().stream().map(DetailResponse::from).toList(),
                    page.page(), page.size(), page.total());
        }
    }
}
