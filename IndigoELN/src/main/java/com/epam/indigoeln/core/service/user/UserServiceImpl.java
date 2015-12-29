package com.epam.indigoeln.core.service.user;

import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.model.UserRole;
import com.epam.indigoeln.core.repository.user.UserRepository;
import com.epam.indigoeln.core.repository.user.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Override
    public void saveUser(User user) {
        userRepository.saveUser(user);
    }

    @Override
    public void deleteUser(String id) {
        userRepository.deleteUser(id);
    }

    @Override
    public User getUser(String name) {
        return userRepository.getUser(name);
    }

    @Override
    public Collection<User> getUsers() {
        return userRepository.getUsers();
    }

    @Override
    public void addUserRole(String userId, String roleId) {
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRoleRepository.saveUserRole(userRole);
    }

    @Override
    public void deleteUserRole(String userId, String roleId) {
        UserRole userRole = userRoleRepository.getUserRole(userId, roleId);
        if (userRole != null) {
            userRoleRepository.deleteUserRole(userRole.getId());
        }
    }

    @Override
    public Collection<UserRole> getUserRoles(String userId) {
        return userRoleRepository.getUserRoles(userId);
    }
}
