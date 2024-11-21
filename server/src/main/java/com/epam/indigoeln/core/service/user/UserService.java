/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.core.service.user;

import com.epam.indigoeln.core.model.Role;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.repository.role.RoleRepository;
import com.epam.indigoeln.core.repository.user.UserRepository;
import com.epam.indigoeln.core.security.Authority;
import com.epam.indigoeln.core.security.SecurityUtils;
import com.epam.indigoeln.core.service.exception.DuplicateFieldException;
import com.epam.indigoeln.core.service.exception.EntityNotFoundException;
import com.epam.indigoeln.core.service.exception.OperationDeniedException;
import com.epam.indigoeln.core.util.SortedPageUtil;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Service class for managing users.
 */
@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    private final SessionRegistry sessionRegistry;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final Pattern passwordValidationPattern;
    private final SecurityUtils securityUtils;

    static {
        Map<String, Function<User, String>> functionMap = new HashMap<>();
        functionMap.put("login", User::getLogin);
        functionMap.put("firstName", User::getFirstName);
        functionMap.put("lastName", User::getLastName);
        functionMap.put("email", User::getEmail);
        functionMap.put("group", User::getGroup);
        functionMap.put(
                "role",
                u -> u.getRoles().stream().findFirst().map(Role::getName).orElse(""));

        USER_SORTED_PAGE_UTIL = new SortedPageUtil<>(functionMap, "login");
    }

    private static final SortedPageUtil<User> USER_SORTED_PAGE_UTIL;


    @Autowired
    public UserService(SessionRegistry sessionRegistry,
                       PasswordEncoder passwordEncoder,
                       UserRepository userRepository,
                       RoleRepository roleRepository,
                       @Value("${password.validation}") String passwordRegex,
                       SecurityUtils securityUtils) {
        this.sessionRegistry = sessionRegistry;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        passwordValidationPattern = Pattern.compile(passwordRegex);
        this.securityUtils = securityUtils;
    }

    public List<String> getAllUsersByIds(List<String> coAuthorsIds) {
        return userRepository.findAllById(coAuthorsIds).stream()
                .map(User::getFullName)
                .collect(Collectors.toList());
    }

    /**
     * Gets users from DB according to given pagination information.
     *
     * @param pageable pagination information to retrieve users
     * @return page with found users
     */
    public Page<User> getAllUsers(Pageable pageable) {
        return USER_SORTED_PAGE_UTIL.getPage(userRepository.findAll(), pageable);
    }

    /**
     * Gets all users from DB.
     *
     * @return list of all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Saves new user in DB.
     *
     * @param user user to save
     * @return created user
     */
    public User createUser(User user) {
        checkUserPassword(user.getPassword());

        String encryptedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);
        user.setActivated(user.getActivated());
        user.setId(user.getLogin());

        // checking for roles existence
        user.setRoles(checkRolesExistenceAndGet(user.getRoles()));

        User savedUser;
        try {
            savedUser = userRepository.save(user);
        } catch (DuplicateKeyException e) {
            LOGGER.error("Duplicate key detected", e);
            throw DuplicateFieldException.createWithUserLogin(user.getLogin());
        }
        LOGGER.debug("Created Information for User: {}", savedUser);

        return savedUser;
    }

    /**
     * Updates user information in DB.
     *
     * @param user          user to update
     * @param executingUser user performing action
     * @return updated user
     */
    public User updateUser(User user, User executingUser) {
        User userFromDB = userRepository.findOneByLogin(user.getLogin());
        if (userFromDB == null) {
            throw EntityNotFoundException.createWithUserLogin(user.getLogin());
        }
        if (userFromDB.isSystem()) {
            throw new IllegalArgumentException("Cannot update system user.");
        }
        // encoding of user's password, or getting from DB entity
        String encryptedPassword;
        if (!Strings.isNullOrEmpty(user.getPassword())) {
            checkUserPassword(user.getPassword());
            encryptedPassword = passwordEncoder.encode(user.getPassword());
        } else {
            encryptedPassword = userFromDB.getPassword();
        }
        user.setPassword(encryptedPassword);

        // checking for roles existence and for disallowed operation for current user
        user.setRoles(checkRolesExistenceAndGet(user.getRoles()));
        if (user.getId().equals(executingUser.getId())
                && !user.getRoles().containsAll(executingUser.getRoles())) {
            throw OperationDeniedException.createUserDeleteOperation(executingUser.getId());
        }

        User savedUser = userRepository.save(user);
        LOGGER.debug("Created Information for User: {}", savedUser);

        // check for significant changes and perform logout for user
        securityUtils.checkAndLogoutUser(savedUser, sessionRegistry);
        return user;
    }

    private void checkUserPassword(String password) {
        if (!passwordValidationPattern.matcher(password).matches()) {
            throw OperationDeniedException.createUserWithNotValidPassword();
        }
    }

    /**
     * Deletes the user from DB if the user exists and it is allowed to delete that user.
     * It is not allowed to delete system users and user cannot delete himself.
     *
     * @param login         login of the user to delete
     * @param executingUser user performing action
     */
    public void deleteUserByLogin(String login, User executingUser) {
        User userByLogin = userRepository.findOneByLogin(login);
        if (userByLogin == null) {
            throw EntityNotFoundException.createWithUserLogin(login);
        }
        if (userByLogin.isSystem()) {
            throw new IllegalArgumentException("Cannot delete system user.");
        }
        // checking for disallowed operation for current user
        if (userByLogin.getId().equals(executingUser.getId())) {
            throw OperationDeniedException.createUserDeleteOperation(executingUser.getId());
        }

        //TODO check for projects, notebooks, experiments with him and use AlreadyInUseException

        userRepository.delete(userByLogin);
        LOGGER.debug("Deleted User: {}", userByLogin);
    }

    /**
     * Gets current user from DB with eagerly loaded authorities.
     *
     * @return current user
     */
    public User getUserWithAuthorities() {
        User user = userRepository.findOneByLogin(SecurityUtils.getCurrentUser().getUsername());
        user.getRoles().size(); // eagerly load the association
        return user;
    }

    /**
     * Gets user from DB by ID with eagerly loaded authorities.
     *
     * @param id user ID
     * @return user with given ID
     */
    public User getUserWithAuthorities(String id) {
        User user = userRepository.findOneById(id);
        if (user == null) {
            throw EntityNotFoundException.createWithUserId(id);
        }

        user.getRoles().size(); // eagerly load the association
        return user;
    }

    /**
     * Gets user from DB by login with eagerly loaded authorities.
     *
     * @param login login of the user
     * @return user with given login
     */
    public User getUserWithAuthoritiesByLogin(String login) {
        User user = userRepository.findOneByLogin(login);
        if (user == null) {
            throw EntityNotFoundException.createWithUserLogin(login);
        }

        user.getRoles().size(); // eagerly load the association
        return user;
    }

    /**
     * Gets user from DB by his identity.
     *
     * @param username user identity
     * @return user from DB by his identity
     */
    public User getUserById(String username) {
        return userRepository.findOneByLogin(username);
    }

    /**
     * Search user by parameters.
     *
     * @param loginOrFirstNameOrLastNameOrRoleName parameter
     * @param pageable                             Pagination information
     * @return Found user
     */
    public Page<User> searchUserByLoginOrFirstNameOrLastNameOrSystemRoleNameWithPaging(
            String loginOrFirstNameOrLastNameOrRoleName, Pageable pageable
    ) {
        List<String> roleIds = roleRepository
                .findByNameLikeIgnoreCase(loginOrFirstNameOrLastNameOrRoleName)
                .map(Role::getId).collect(toList());

        List<User> users = userRepository
                .findByLoginIgnoreCaseLikeOrFirstNameIgnoreCaseLikeOrLastNameIgnoreCaseLikeOrRolesIdIn(
                        loginOrFirstNameOrLastNameOrRoleName,
                        loginOrFirstNameOrLastNameOrRoleName,
                        loginOrFirstNameOrLastNameOrRoleName,
                        roleIds);

        return USER_SORTED_PAGE_UTIL.getPage(users, pageable);
    }

    private Set<Role> checkRolesExistenceAndGet(Set<Role> roles) {
        Set<Role> checkedRoles = new HashSet<>(roles.size());
        for (Role role : roles) {
            Role roleFromDB = roleRepository.findById(role.getId())
                    .orElseThrow(() -> EntityNotFoundException.createWithRoleId(role.getId()));
            checkedRoles.add(roleFromDB);
        }

        return checkedRoles;
    }

    /**
     * Check if user with {@code login} does <b>not</b> present in BD.
     *
     * @param login login to check
     * @return {@code true} if there is no user with the login in BB
     * {@code false} in other case
     */
    public boolean isNew(String login) {
        return !userRepository.existsByLogin(login);
    }


    /**
     * Gets users from DB with authorities of CONTENT_EDITOR.
     *
     * @return all users with CONTENT_EDITOR authorities.
     */
    public Set<User> getContentEditors() {

        List<String> contentEditorRoles = roleRepository
                .findAllByAuthorities(Authority.CONTENT_EDITOR)
                .map(Role::getId)
                .collect(toList());

        return userRepository.findAllByRolesIdIn(contentEditorRoles);
    }
}
