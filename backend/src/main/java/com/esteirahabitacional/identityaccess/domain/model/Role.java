package com.esteirahabitacional.identityaccess.domain.model;

import java.util.Set;

public enum Role {
    MANAGER(Set.of(Permission.MANAGE_INTERNAL_USERS, Permission.VIEW_CURRENT_CONTEXT), true),
    ANALYST(Set.of(Permission.VIEW_CURRENT_CONTEXT), true),
    BROKER(Set.of(Permission.VIEW_CURRENT_CONTEXT), false),
    CLIENT(Set.of(Permission.VIEW_CURRENT_CONTEXT), false),
    SELLER(Set.of(Permission.VIEW_CURRENT_CONTEXT), false),
    PLATFORM_ADMIN(Set.of(
            Permission.CREATE_ORGANIZATION,
            Permission.MANAGE_INTERNAL_USERS,
            Permission.VIEW_CURRENT_CONTEXT), false);

    private final Set<Permission> permissions;
    private final boolean internal;

    Role(Set<Permission> permissions, boolean internal) {
        this.permissions = permissions;
        this.internal = internal;
    }

    public boolean grants(Permission permission) {
        return permissions.contains(permission);
    }

    public boolean isInternal() {
        return internal;
    }
}
