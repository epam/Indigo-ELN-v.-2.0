package com.epam.indigoeln.services;

import com.epam.indigoeln.documents.Role;
import com.epam.indigoeln.documents.UserRole;
import com.epam.indigoeln.repositories.RoleRepository;
import com.epam.indigoeln.repositories.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;


    @Autowired
    public RoleServiceImpl(final UserRoleRepository userRoleRepository, final RoleRepository roleRepository) {
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public Collection<Role> getRoles(String userId) {
        Collection<UserRole> userRoles = userRoleRepository.getUserRoles(userId);
        return roleRepository.getRoles(userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList()));
    }

}
