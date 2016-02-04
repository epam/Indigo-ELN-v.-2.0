package com.epam.indigoeln.core.service.role;

import com.epam.indigoeln.core.model.Role;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.repository.role.RoleRepository;
import com.epam.indigoeln.core.service.EntityAlreadyExistsException;
import com.epam.indigoeln.core.service.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    public Collection<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role getRole(String id) {
        return roleRepository.findOne(id);
    }

    public Role createRole(Role role) {
        // check for duplication of name
        if (roleRepository.findByName(role.getName()) != null) {
            throw EntityAlreadyExistsException.createWithRoleName(role.getName());
        }

        //reset role's id
        role.setId(null);
        return roleRepository.save(role);
    }

    public Role updateRole(Role role, User user) {
        Role roleFromDB = roleRepository.findOne(role.getId());
        if (roleFromDB == null) {
            throw EntityNotFoundException.createWithRoleId(role.getId());
        }

        // check for duplication of name
        Role roleByName = roleRepository.findByName(role.getName());
        if (roleByName != null && !roleByName.getId().equals(role.getId())) {
            throw EntityAlreadyExistsException.createWithRoleName(role.getName());
        }

        return roleRepository.save(role);
    }

    public void deleteRole(String id, User user) {
        Role role = roleRepository.findOne(id);
        if (role == null) {
            throw EntityNotFoundException.createWithRoleId(id);
        }

        roleRepository.delete(id);
    }
}