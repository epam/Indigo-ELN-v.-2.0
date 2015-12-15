package com.epam.indigoeln.repositories;

import com.epam.indigoeln.documents.UserRole;

import java.util.Collection;

public interface UserRoleRepository {
    Collection<UserRole> getUserRoles(String userId);
}
