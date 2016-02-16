package com.epam.indigoeln.core.service.role;

import com.epam.indigoeln.core.model.Role;
import com.epam.indigoeln.core.repository.role.RoleRepository;
import com.epam.indigoeln.core.repository.user.UserRepository;
import com.epam.indigoeln.core.security.SecurityUtils;
import com.epam.indigoeln.core.service.exception.AlreadyInUseException;
import com.epam.indigoeln.core.service.exception.DuplicateFieldException;
import com.epam.indigoeln.core.service.exception.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class RoleService {

    @Autowired
    private SessionRegistry sessionRegistry;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    public Collection<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role getRole(String id) {
        return roleRepository.findOne(id);
    }

    public Role createRole(Role role) {
        //reset role's id
        role.setId(null);
        try {
            return roleRepository.save(role);
        } catch (DuplicateKeyException e) {
            throw DuplicateFieldException.createWithRoleName(role.getName());
        }
    }

    public Role updateRole(Role role) {
        Role roleFromDB = roleRepository.findOne(role.getId());
        if (roleFromDB == null) {
            throw EntityNotFoundException.createWithRoleId(role.getId());
        }
        role = roleRepository.save(role);

        // check for significant changes and perform logout for users
        SecurityUtils.checkAndLogoutUsers(userRepository.findByRoleId(role.getId()), sessionRegistry);
        return role;
    }

    public void deleteRole(String id) {
        Role role = roleRepository.findOne(id);
        if (role == null) {
            throw EntityNotFoundException.createWithRoleId(id);
        }

        if (userRepository.countByRoleId(id) > 0) {
            throw AlreadyInUseException.createWithRoleId(id);
        }

        roleRepository.delete(id);
    }
}