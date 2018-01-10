package com.epam.indigoeln.core.service.role;

import com.epam.indigoeln.core.model.Role;
import com.epam.indigoeln.core.repository.role.RoleRepository;
import com.epam.indigoeln.core.repository.user.UserRepository;
import com.epam.indigoeln.core.security.SecurityUtils;
import com.epam.indigoeln.core.service.exception.AlreadyInUseException;
import com.epam.indigoeln.core.service.exception.DuplicateFieldException;
import com.epam.indigoeln.core.service.exception.EntityNotFoundException;
import com.epam.indigoeln.core.util.SortedPageUtil;
import com.epam.indigoeln.web.rest.util.AuthoritiesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service class to work with user roles.
 */
@Service
public class RoleService {

    /**
     * SessionRegistry instance to work with user sessions.
     */
    private final SessionRegistry sessionRegistry;

    /**
     * RoleRepository instance to work with roles in DB.
     */
    private final RoleRepository roleRepository;

    /**
     * UserRepository instance to work with users in DB.
     */
    private final UserRepository userRepository;

    private static final SortedPageUtil<Role> ROLE_SORTED_PAGE_UTIL;

    static {
        Map<String, Function<Role, String>> functionMap = new HashMap<>();
        functionMap.put("name", Role::getName);
        ROLE_SORTED_PAGE_UTIL = new SortedPageUtil<>(functionMap);
    }

    /**
     * SecurityUtils instance to work with global permissions.
     */
    private final SecurityUtils securityUtils;

    /**
     * Create a new RoleService instance.
     *
     * @param sessionRegistry SessionRegistry instance to work with user sessions
     * @param roleRepository  RoleRepository instance to work with roles in DB
     * @param userRepository  UserRepository instance to work with users in DB
     * @param securityUtils   SecurityUtils instance to work with global permissions
     */
    @Autowired
    public RoleService(SessionRegistry sessionRegistry,
                       RoleRepository roleRepository,
                       UserRepository userRepository,
                       SecurityUtils securityUtils) {
        this.sessionRegistry = sessionRegistry;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.securityUtils = securityUtils;
    }

    /**
     * Retrieve all roles from DB according to given pagination information.
     *
     * @param pageable Paging data
     * @return all roles in application
     */
    public Page<Role> getAllRoles(Pageable pageable) {
        return ROLE_SORTED_PAGE_UTIL.getPage(roleRepository.findAll(), pageable);
    }

    /**
     * * Retrieve roles with name like {@code nameLike} from DB.
     *
     * @param nameLike Name for getting roles
     * @return all roles in application
     */
    public Page<Role> getRolesWithNameLike(String nameLike, Pageable pageable) {
        return ROLE_SORTED_PAGE_UTIL.getPage(roleRepository.findByNameLikeIgnoreCase(nameLike)
                .collect(Collectors.toList()), pageable);
    }

    /**
     * Retrieve one role by given ID.
     *
     * @param id role ID
     * @return role with given ID
     */
    public Role getRole(String id) {
        return roleRepository.findOne(id);
    }

    /**
     * Create a new role in DB.
     *
     * @param role role to create
     * @return created role with new ID
     */
    public Role createRole(Role role) {
        //reset role's id
        AuthoritiesUtil.checkAuthorities(role.getAuthorities());
        role.setId(null);
        try {
            return roleRepository.save(role);
        } catch (DuplicateKeyException e) {
            throw DuplicateFieldException.createWithRoleName(role.getName(), e);
        }
    }

    /**
     * Update an existing role in DB.
     *
     * @param role role to update
     * @return updated role
     */
    public Role updateRole(Role role) {
        Role roleFromDB = roleRepository.findOne(role.getId());
        if (roleFromDB == null) {
            throw EntityNotFoundException.createWithRoleId(role.getId());
        }
        if (roleFromDB.isSystem()) {
            throw new IllegalArgumentException("Cannot update system role.");
        }
        AuthoritiesUtil.checkAuthorities(role.getAuthorities());
        Role savedRole = roleRepository.save(role);

        // check for significant changes and perform logout for users
        securityUtils.checkAndLogoutUsers(userRepository.findByRoleId(savedRole.getId()), sessionRegistry);
        return savedRole;
    }

    /**
     * Delete role with given ID from DB.
     *
     * @param id role ID
     */
    public void deleteRole(String id) {
        Role role = roleRepository.findOne(id);
        if (role == null) {
            throw EntityNotFoundException.createWithRoleId(id);
        }
        if (role.isSystem()) {
            throw new IllegalArgumentException("Cannot delete system role.");
        }
        if (userRepository.countByRoleId(id) > 0) {
            throw AlreadyInUseException.createWithRoleId(id);
        }

        roleRepository.delete(id);
    }
}
