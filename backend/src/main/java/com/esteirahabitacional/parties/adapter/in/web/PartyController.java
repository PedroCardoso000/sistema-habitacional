package com.esteirahabitacional.parties.adapter.in.web;

import com.esteirahabitacional.parties.application.port.in.AssociateBrokerAgencyUseCase;
import com.esteirahabitacional.parties.application.port.in.ChangePartnerStatusUseCase;
import com.esteirahabitacional.parties.application.port.in.FindClientByCpfUseCase;
import com.esteirahabitacional.parties.application.port.in.ListParticipantsUseCase;
import com.esteirahabitacional.parties.application.port.in.RegisterAgencyUseCase;
import com.esteirahabitacional.parties.application.port.in.RegisterBrokerUseCase;
import com.esteirahabitacional.parties.application.port.in.RegisterClientUseCase;
import com.esteirahabitacional.parties.application.port.in.UpdatePartyContactUseCase;
import com.esteirahabitacional.parties.domain.model.PartyStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.net.URI;
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
@RequestMapping("/api/organizations/{organizationId}/parties")
class PartyController {

    private final RegisterClientUseCase registerClient;
    private final FindClientByCpfUseCase findClient;
    private final RegisterBrokerUseCase registerBroker;
    private final RegisterAgencyUseCase registerAgency;
    private final AssociateBrokerAgencyUseCase associateBrokerAgency;
    private final UpdatePartyContactUseCase updateContact;
    private final ChangePartnerStatusUseCase changeStatus;
    private final ListParticipantsUseCase listParticipants;

    PartyController(
            RegisterClientUseCase registerClient,
            FindClientByCpfUseCase findClient,
            RegisterBrokerUseCase registerBroker,
            RegisterAgencyUseCase registerAgency,
            AssociateBrokerAgencyUseCase associateBrokerAgency,
            UpdatePartyContactUseCase updateContact,
            ChangePartnerStatusUseCase changeStatus,
            ListParticipantsUseCase listParticipants) {
        this.registerClient = registerClient;
        this.findClient = findClient;
        this.registerBroker = registerBroker;
        this.registerAgency = registerAgency;
        this.associateBrokerAgency = associateBrokerAgency;
        this.updateContact = updateContact;
        this.changeStatus = changeStatus;
        this.listParticipants = listParticipants;
    }

    @PostMapping("/clients")
    ResponseEntity<ClientResponse> registerClient(
            @PathVariable UUID organizationId, @Valid @RequestBody RegisterPersonRequest request) {
        RegisterClientUseCase.Result result = registerClient.execute(new RegisterClientUseCase.Command(
                organizationId, request.cpf(), request.name(), request.email(), request.phone()));
        return ResponseEntity.created(location(organizationId, "clients", result.id()))
                .body(ClientResponse.from(result));
    }

    @PostMapping("/clients/search")
    ClientResponse findClient(
            @PathVariable UUID organizationId, @Valid @RequestBody FindClientRequest request) {
        return ClientResponse.from(findClient.execute(
                new FindClientByCpfUseCase.Query(organizationId, request.cpf())));
    }

    @PostMapping("/brokers")
    ResponseEntity<BrokerResponse> registerBroker(
            @PathVariable UUID organizationId, @Valid @RequestBody RegisterPersonRequest request) {
        RegisterBrokerUseCase.Result result = registerBroker.execute(new RegisterBrokerUseCase.Command(
                organizationId, request.cpf(), request.name(), request.email(), request.phone()));
        return ResponseEntity.created(location(organizationId, "brokers", result.id()))
                .body(BrokerResponse.from(result));
    }

    @PostMapping("/agencies")
    ResponseEntity<AgencyResponse> registerAgency(
            @PathVariable UUID organizationId, @Valid @RequestBody RegisterAgencyRequest request) {
        RegisterAgencyUseCase.Result result = registerAgency.execute(new RegisterAgencyUseCase.Command(
                organizationId, request.cnpj(), request.legalName(), request.email(), request.phone()));
        return ResponseEntity.created(location(organizationId, "agencies", result.id()))
                .body(AgencyResponse.from(result));
    }

    @PutMapping("/brokers/{brokerId}/agency")
    ResponseEntity<Void> associateBrokerAgency(
            @PathVariable UUID organizationId,
            @PathVariable UUID brokerId,
            @Valid @RequestBody AssociateAgencyRequest request) {
        associateBrokerAgency.execute(
                new AssociateBrokerAgencyUseCase.Command(organizationId, brokerId, request.agencyId()));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{type}/{partyId}/contact")
    ResponseEntity<Void> updateContact(
            @PathVariable UUID organizationId,
            @PathVariable UpdatePartyContactUseCase.PartyType type,
            @PathVariable UUID partyId,
            @Valid @RequestBody ContactRequest request) {
        updateContact.execute(new UpdatePartyContactUseCase.Command(
                organizationId, type, partyId, request.email(), request.phone()));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/partners/{type}/{partnerId}/status")
    ResponseEntity<Void> changeStatus(
            @PathVariable UUID organizationId,
            @PathVariable ChangePartnerStatusUseCase.PartnerType type,
            @PathVariable UUID partnerId,
            @Valid @RequestBody StatusRequest request) {
        changeStatus.execute(new ChangePartnerStatusUseCase.Command(
                organizationId, type, partnerId, request.status()));
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    ParticipantPageResponse list(
            @PathVariable UUID organizationId,
            @RequestParam ListParticipantsUseCase.ParticipantType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ParticipantPageResponse.from(listParticipants.execute(
                new ListParticipantsUseCase.Query(organizationId, type, page, size)));
    }

    private URI location(UUID organizationId, String collection, UUID id) {
        return URI.create("/api/organizations/" + organizationId + "/parties/" + collection + "/" + id);
    }

    record RegisterPersonRequest(
            @NotBlank @Pattern(regexp = "[0-9.\\-]{11,14}") String cpf,
            @NotBlank String name,
            @Email String email,
            String phone) {}

    record FindClientRequest(@NotBlank @Pattern(regexp = "[0-9.\\-]{11,14}") String cpf) {}

    record RegisterAgencyRequest(
            @NotBlank @Pattern(regexp = "[0-9./\\-]{14,18}") String cnpj,
            @NotBlank String legalName,
            @Email String email,
            String phone) {}

    record AssociateAgencyRequest(@NotNull UUID agencyId) {}

    record ContactRequest(@Email String email, String phone) {}

    record StatusRequest(@NotNull PartyStatus status) {}

    record ClientResponse(UUID id, String fullName, String email, String phone, PartyStatus status) {

        static ClientResponse from(RegisterClientUseCase.Result result) {
            return new ClientResponse(
                    result.id(), result.fullName(), result.email(), result.phone(), result.status());
        }

        static ClientResponse from(FindClientByCpfUseCase.Result result) {
            return new ClientResponse(
                    result.id(), result.fullName(), result.email(), result.phone(), result.status());
        }
    }

    record BrokerResponse(
            UUID id,
            String fullName,
            String email,
            String phone,
            UUID realEstateAgencyId,
            PartyStatus status) {

        static BrokerResponse from(RegisterBrokerUseCase.Result result) {
            return new BrokerResponse(
                    result.id(), result.fullName(), result.email(), result.phone(),
                    result.realEstateAgencyId(), result.status());
        }
    }

    record AgencyResponse(UUID id, String legalName, String email, String phone, PartyStatus status) {

        static AgencyResponse from(RegisterAgencyUseCase.Result result) {
            return new AgencyResponse(
                    result.id(), result.legalName(), result.email(), result.phone(), result.status());
        }
    }

    record ParticipantItemResponse(UUID id, String type, String name, PartyStatus status) {}

    record ParticipantPageResponse(
            List<ParticipantItemResponse> items, int page, int size, long total) {

        static ParticipantPageResponse from(ListParticipantsUseCase.Result result) {
            return new ParticipantPageResponse(
                    result.items().stream()
                            .map(item -> new ParticipantItemResponse(
                                    item.id(), item.type().name(), item.name(), item.status()))
                            .toList(),
                    result.page(), result.size(), result.total());
        }
    }
}
