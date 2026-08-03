package com.esteirahabitacional.identityaccess.adapter.in.web;

import com.esteirahabitacional.identityaccess.application.port.out.CurrentActorProvider;
import com.esteirahabitacional.shared.ApplicationException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class DevHeaderCurrentActorProvider implements CurrentActorProvider {

    public static final String USER_HEADER = "X-User-Id";
    public static final String ORGANIZATION_HEADER = "X-Organization-Id";

    @Override
    public Actor current() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            throw unauthenticated();
        }
        HttpServletRequest request = attributes.getRequest();
        try {
            return new Actor(
                    UUID.fromString(request.getHeader(USER_HEADER)),
                    UUID.fromString(request.getHeader(ORGANIZATION_HEADER)));
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw unauthenticated();
        }
    }

    private ApplicationException unauthenticated() {
        return new ApplicationException(401, "authentication-required", "Autenticação necessária",
                "Informe um contexto autenticado válido para desenvolvimento.");
    }
}
