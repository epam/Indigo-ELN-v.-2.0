package com.epam.indigoeln.services;

import com.epam.indigoeln.documents.User;
import com.epam.indigoeln.documents.UserRole;

import java.util.Collection;

public interface UserService {

    void saveUser(User user);
    void deleteUser(String id);
    User getUser(String name);

    void addUserRole(String userId, String roleId);
    void deleteUserRole(String userId, String roleId);
    Collection<UserRole> getUserRoles(String userId);

}
