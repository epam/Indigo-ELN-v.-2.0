package com.epam.indigoeln.core.service.user;

import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.model.UserRole;

import java.util.Collection;

public interface UserService {

    void saveUser(User user);
    void deleteUser(String id);
    User getUser(String name);
    Collection<User> getUsers();

    void addUserRole(String userId, String roleId);
    void deleteUserRole(String userId, String roleId);
    Collection<UserRole> getUserRoles(String userId);
}
