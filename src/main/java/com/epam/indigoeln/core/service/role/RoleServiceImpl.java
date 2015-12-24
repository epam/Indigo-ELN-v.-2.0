package com.epam.indigoeln.core.service.role;

import com.epam.indigoeln.core.model.Role;
import com.epam.indigoeln.core.model.RolePermission;
import com.epam.indigoeln.core.repository.role.RolePermissionRepository;
import com.epam.indigoeln.core.repository.role.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Override
    public void addRole(Role role) {
        roleRepository.addRole(role);
    }

    @Override
    public void deleteRole(String id) {
        roleRepository.deleteRole(id);
    }

    @Override
    public Collection<Role> getRoles(Collection<String> rolesIds) {
        return roleRepository.getRoles(rolesIds);
    }

    @Override
    public void addRolePermission(String roleId, String permission) {
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRoleId(roleId);
        rolePermission.setValue(permission);
        rolePermissionRepository.saveRolePermission(rolePermission);
    }

    @Override
    public void deleteRolePermission(String roleId, String permission) {
        RolePermission rolePermission = rolePermissionRepository.getRolePermission(roleId, permission);
        if (rolePermission != null) {
            rolePermissionRepository.deleteRolePermission(rolePermission.getId());
        }
    }

    @Override
    public Collection<RolePermission> getRolesPermissions(Collection<String> rolesIds) {
        return rolePermissionRepository.getRolesPermissions(rolesIds);
    }
}
