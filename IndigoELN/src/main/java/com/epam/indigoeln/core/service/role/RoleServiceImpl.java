package com.epam.indigoeln.core.service.role;

import com.epam.indigoeln.core.model.Role;
import com.epam.indigoeln.core.model.RolePermission;
import com.epam.indigoeln.core.repository.role.RolePermissionRepository;
import com.epam.indigoeln.core.repository.role.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Iterator;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Override
    public void addRole(Role role) {
        roleRepository.save(role);
    }

    @Override
    public void deleteRole(String id) {
        roleRepository.delete(id);
    }

    @Override
    public Iterable<Role> getRoles(Collection<String> rolesIds) {
        return roleRepository.findAll(rolesIds);
    }

    @Override
    public void addRolePermission(String roleId, String permission) {
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRoleId(roleId);
        rolePermission.setValue(permission);
        rolePermissionRepository.save(rolePermission);
    }

    @Override
    public void deleteRolePermission(String roleId, String permission) {
        Collection<RolePermission> rolePermissions = rolePermissionRepository.findByRoleIdAndValue(roleId, permission);
        if (rolePermissions != null) {
            rolePermissions.forEach(rolePermissionRepository::delete);
        }
    }

    @Override
    public Iterable<RolePermission> getRolesPermissions(Collection<String> rolesIds) {
        return rolePermissionRepository.findAll(rolesIds);
    }
}
