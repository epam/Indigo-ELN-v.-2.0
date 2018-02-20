package com.epam.indigoeln.web.rest.util.permission.helpers;

import com.epam.indigoeln.core.model.BasicModelObject;
import com.epam.indigoeln.core.model.UserPermission;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.emptySet;

@Data
public class PermissionChanges<E extends BasicModelObject> {

    private final Set<UserPermission> newPermissions;
    private final Set<UserPermission> updatedPermissions;
    private final Set<UserPermission> removedPermissions;
    private final E entity;

    public PermissionChanges(E entity, Set<UserPermission> newPermissions, Set<UserPermission> updatedPermissions) {
        this(entity, newPermissions, updatedPermissions, new HashSet<>());
    }

    public PermissionChanges(E entity, Set<UserPermission> newPermissions,
                             Set<UserPermission> updatedPermissions,
                             Set<UserPermission> removedPermissions) {
        this.newPermissions = newPermissions;
        this.updatedPermissions = updatedPermissions;
        this.removedPermissions = removedPermissions;
        this.entity = entity;
    }

    public PermissionChanges(E entity) {
        this(entity, new HashSet<>(), new HashSet<>());
    }

    public static <E extends BasicModelObject> PermissionChanges<E> ofUpdatedPermissions(E entity, Set<UserPermission> updatedPermissions) {
        return new PermissionChanges<>(entity, emptySet(), updatedPermissions);
    }

    public static <E extends BasicModelObject> PermissionChanges<E> ofCreatedPermissions(E entity, Set<UserPermission> createdPermissions) {
        return new PermissionChanges<>(entity, createdPermissions, emptySet());
    }

    public static <E extends BasicModelObject> PermissionChanges<E> ofRemovedPermissions(E entity, Set<UserPermission> removedPermissions) {
        return new PermissionChanges<>(entity, emptySet(), emptySet(), removedPermissions);
    }

    public PermissionChanges<E> merge(PermissionChanges<E> other) {
        if (!this.entity.equals(other.entity)) {
            throw new IllegalArgumentException("can't merge changes of different entities");
        }
        this.newPermissions.addAll(other.newPermissions);
        this.updatedPermissions.addAll(other.updatedPermissions);
        this.removedPermissions.addAll(other.removedPermissions);
        return this;
    }

    public boolean hadChanged() {
        return !(newPermissions.isEmpty() && updatedPermissions.isEmpty() && removedPermissions.isEmpty());
    }
}
