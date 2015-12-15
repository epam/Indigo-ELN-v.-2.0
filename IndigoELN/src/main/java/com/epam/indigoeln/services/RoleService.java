package com.epam.indigoeln.services;

import com.epam.indigoeln.documents.Role;
import com.epam.indigoeln.documents.UserRole;

import java.util.Collection;

public interface RoleService {
    Collection<Role> getRoles(String userId);
}
