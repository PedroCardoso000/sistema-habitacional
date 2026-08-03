package com.esteirahabitacional.platformadministration.adapter.in.web;

import com.esteirahabitacional.platformadministration.application.port.in.CreateAuthorizedOrganizationUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/platform/organizations")
class PlatformAdministrationController {

    private final CreateAuthorizedOrganizationUseCase createOrganization;

    PlatformAdministrationController(CreateAuthorizedOrganizationUseCase createOrganization) {
        this.createOrganization = createOrganization;
    }

    @PostMapping
    ResponseEntity<OrganizationResponse> create(@Valid @RequestBody CreateOrganizationRequest request) {
        CreateAuthorizedOrganizationUseCase.Result result = createOrganization.execute(
                new CreateAuthorizedOrganizationUseCase.Command(request.name()));
        return ResponseEntity.created(URI.create("/api/platform/organizations/" + result.id()))
                .body(new OrganizationResponse(result.id(), result.name()));
    }

    record CreateOrganizationRequest(@NotBlank String name) {}

    record OrganizationResponse(UUID id, String name) {}
}
