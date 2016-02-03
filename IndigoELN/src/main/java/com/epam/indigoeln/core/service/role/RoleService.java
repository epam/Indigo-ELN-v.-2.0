package com.epam.indigoeln.core.service.role;

import com.epam.indigoeln.core.model.Role;
import com.epam.indigoeln.core.repository.role.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    public void addRole(Role role) {
        roleRepository.save(role);
    }

    public void deleteRole(String id) {
        roleRepository.delete(id);
    }

    public Iterable<Role> getRoles(Collection<String> rolesIds) {
        return roleRepository.findAll(rolesIds);
    }

    public void addRolePermission(String roleId, String permission) {
//        RolePermission rolePermission = new RolePermission();
//        rolePermission.setRoleId(roleId);
//        rolePermission.setValue(permission);
//        rolePermissionRepository.save(rolePermission);
    }

    public void deleteRolePermission(String roleId, String permission) {
//        Collection<RolePermission> rolePermissions = rolePermissionRepository.findByRoleIdAndValue(roleId, permission);
//        if (rolePermissions != null) {
//            rolePermissions.forEach(rolePermissionRepository::delete);
//        }
    }
}