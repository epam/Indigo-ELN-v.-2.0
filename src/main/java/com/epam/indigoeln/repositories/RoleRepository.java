package com.epam.indigoeln.repositories;

import com.epam.indigoeln.documents.Role;
import com.epam.indigoeln.documents.UserRole;

import java.util.Collection;

public interface RoleRepository {
    Collection<Role> getRoles(Collection<String> rolesIds);
}
