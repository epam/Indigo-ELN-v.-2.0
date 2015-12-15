package com.epam.indigoeln.repositories;

import com.epam.indigoeln.documents.RolePermission;

import java.util.Collection;

public interface RolePermissionRepository {
    RolePermission getRolePermission(String roleId, String permission);
    Collection<RolePermission> getRolesPermissions(Collection<String> rolesIds);
    void deleteRolePermission(String id);
    void saveRolePermission(RolePermission rolePermission);
}
