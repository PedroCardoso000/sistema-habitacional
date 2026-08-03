package com.esteirahabitacional.identityaccess.adapter.in.web;

import com.esteirahabitacional.identityaccess.application.port.in.AssignRoleUseCase;
import com.esteirahabitacional.identityaccess.application.port.in.GetCurrentUserContextUseCase;
import com.esteirahabitacional.identityaccess.application.port.in.RegisterInternalUserUseCase;
import com.esteirahabitacional.identityaccess.application.port.in.RevokeAccessUseCase;
import com.esteirahabitacional.identityaccess.application.port.in.SuspendAccessUseCase;
import com.esteirahabitacional.identityaccess.domain.model.AccessStatus;
import com.esteirahabitacional.identityaccess.domain.model.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
class IdentityAccessController {

    private final RegisterInternalUserUseCase registerUser;
    private final AssignRoleUseCase assignRole;
    private final SuspendAccessUseCase suspendAccess;
    private final RevokeAccessUseCase revokeAccess;
    private final GetCurrentUserContextUseCase currentContext;

    IdentityAccessController(
            RegisterInternalUserUseCase registerUser,
            AssignRoleUseCase assignRole,
            SuspendAccessUseCase suspendAccess,
            RevokeAccessUseCase revokeAccess,
            GetCurrentUserContextUseCase currentContext) {
        this.registerUser = registerUser;
        this.assignRole = assignRole;
        this.suspendAccess = suspendAccess;
        this.revokeAccess = revokeAccess;
        this.currentContext = currentContext;
    }

    @PostMapping("/organizations/{organizationId}/users")
    ResponseEntity<UserResponse> register(
            @PathVariable UUID organizationId, @Valid @RequestBody RegisterUserRequest request) {
        RegisterInternalUserUseCase.Result result = registerUser.execute(
                new RegisterInternalUserUseCase.Command(
                        organizationId, request.email(), request.displayName(), request.role()));
        UserResponse response = UserResponse.from(result);
        return ResponseEntity.created(URI.create(
                        "/api/organizations/" + organizationId + "/users/" + result.userId()))
                .body(response);
    }

    @PatchMapping("/organizations/{organizationId}/users/{userId}/role")
    ResponseEntity<Void> assignRole(
            @PathVariable UUID organizationId,
            @PathVariable UUID userId,
            @Valid @RequestBody AssignRoleRequest request) {
        assignRole.execute(new AssignRoleUseCase.Command(organizationId, userId, request.role()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/organizations/{organizationId}/users/{userId}/suspension")
    ResponseEntity<Void> suspend(@PathVariable UUID organizationId, @PathVariable UUID userId) {
        suspendAccess.execute(new SuspendAccessUseCase.Command(organizationId, userId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/organizations/{organizationId}/users/{userId}/revocation")
    ResponseEntity<Void> revoke(@PathVariable UUID organizationId, @PathVariable UUID userId) {
        revokeAccess.execute(new RevokeAccessUseCase.Command(organizationId, userId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/identity/context")
    UserContextResponse context() {
        return UserContextResponse.from(currentContext.execute());
    }

    record RegisterUserRequest(
            @NotBlank @Email String email,
            @NotBlank String displayName,
            @NotNull Role role) {}

    record AssignRoleRequest(@NotNull Role role) {}

    record UserResponse(
            UUID id,
            UUID organizationId,
            String email,
            String displayName,
            Role role,
            AccessStatus status) {

        static UserResponse from(RegisterInternalUserUseCase.Result result) {
            return new UserResponse(
                    result.userId(), result.organizationId(), result.email(), result.displayName(),
                    result.role(), result.status());
        }
    }

    record UserContextResponse(
            UUID userId,
            UUID organizationId,
            String email,
            String displayName,
            Role role,
            AccessStatus status) {

        static UserContextResponse from(GetCurrentUserContextUseCase.Result result) {
            return new UserContextResponse(
                    result.userId(), result.organizationId(), result.email(), result.displayName(),
                    result.role(), result.status());
        }
    }
}
