package com.epam.indigoeln.core.service.role;

import com.epam.indigoeln.core.model.Role;
import com.epam.indigoeln.core.repository.role.RoleRepository;
import com.epam.indigoeln.core.repository.user.UserRepository;
import com.epam.indigoeln.core.service.AlreadyInUseException;
import com.epam.indigoeln.core.service.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class RoleService {

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
        return roleRepository.save(role);
    }

    public Role updateRole(Role role) {
        Role roleFromDB = roleRepository.findOne(role.getId());
        if (roleFromDB == null) {
            throw EntityNotFoundException.createWithRoleId(role.getId());
        }

        return roleRepository.save(role);
    }

    public void deleteRole(String id) {
        Role role = roleRepository.findOne(id);
        if (role == null) {
            throw EntityNotFoundException.createWithRoleId(id);
        }

        if (userRepository.countUsersByRoleId(id) > 0) {
            throw AlreadyInUseException.createWithRoleId(id);
        }

        roleRepository.delete(id);
    }
}