package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.eln.entity.RoleEntity;
import com.epam.indigoeln.eln.mapper.RoleMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.repository.RoleRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;

import java.util.List;
import java.util.UUID;

import static com.epam.indigoeln.common.util.ModelUtil.editProperty;

@Slf4j
@Transactional
@ApplicationScoped
public class RoleService {

    @Inject
    RoleRepository roleRepository;
    @Inject
    RoleMapper roleMapper;
    @Inject
    ACLService aclService;

    public RoleDTO createRole(RoleRequest request) {
        aclService.ensureTopLevelAccess(ApplicationPermission.MANAGE_ROLES);
        RoleEntity entity = roleMapper.requestToRole(request);
        try {
            roleRepository.persist(entity);
        } catch (ConstraintViolationException e) {
            if ("application_role_name_uq".equals(e.getConstraintName())) {
                throw new InvalidRequestException("Role with name '" + request.getName() + "' already exists");
            }
            throw e;
        }
        return roleMapper.entityToDTO(entity);
    }

    public List<RoleDTO> getRoles() {
        return roleRepository.list();
    }

    public List<RoleRef> suggestRoles() {
        return roleRepository.suggest();
    }

    public RoleDTO updateRole(UUID roleID, RoleEditRequest request) {
        aclService.ensureTopLevelAccess(ApplicationPermission.MANAGE_ROLES);
        RoleEntity role = roleRepository.get(roleID);
        editProperty(request.getName(), role::setName);
        editProperty(request.getPermissions(), p -> role.setPermissions(p.toArray(ApplicationPermission[]::new)));
        return roleMapper.entityToDTO(role);
    }

    public void deleteRole(@NotNull UUID roleID) {
        aclService.ensureTopLevelAccess(ApplicationPermission.MANAGE_ROLES);
        RoleEntity role = roleRepository.get(roleID);
        roleRepository.delete(role);
    }
}
