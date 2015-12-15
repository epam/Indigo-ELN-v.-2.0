package com.epam.indigoeln.repositories;

import com.epam.indigoeln.documents.UserRole;

import java.util.Collection;

public interface UserRoleRepository {
    void saveUserRole(UserRole userRole);
    void deleteUserRole(String id);
    UserRole getUserRole(String userId, String roleId);
    Collection<UserRole> getUserRoles(String userId);
}
