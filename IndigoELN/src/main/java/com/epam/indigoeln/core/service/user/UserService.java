package com.epam.indigoeln.core.service.user;


import com.epam.indigoeln.core.model.Role;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.repository.role.RoleRepository;
import com.epam.indigoeln.core.repository.user.UserRepository;
import com.epam.indigoeln.core.security.SecurityUtils;
import com.epam.indigoeln.core.service.EntityAlreadyExistsException;
import com.epam.indigoeln.core.service.EntityNotFoundException;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing users.
 */
@Service
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;


    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User createUser(User user) {
        if (userRepository.findOneByLogin(user.getLogin()) != null) {
            throw EntityAlreadyExistsException.createWithUserLogin(user.getLogin());
        }

        String encryptedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);
        user.setActivated(true);
        // Checking for roles existence
        user.setRoles(checkRolesExistenceAndGet(user.getRoles()));

        user = userRepository.save(user);
        log.debug("Created Information for User: {}", user);

        return user;
    }

    public User updateUser(User user) {
        User userFromDB = userRepository.findOneByLogin(user.getLogin());
        if (userFromDB == null) {
            throw EntityNotFoundException.createWithUserLogin(user.getLogin());
        } else if (!userFromDB.getId().equals(user.getId())) {
            throw EntityAlreadyExistsException.createWithUserLogin(user.getLogin());
        }

        // Encoding of user's password, or getting from DB entity
        String encryptedPassword;
        if (!Strings.isNullOrEmpty(user.getPassword())) {
            encryptedPassword = passwordEncoder.encode(user.getPassword());
        } else {
            encryptedPassword = userFromDB.getPassword();
        }
        user.setPassword(encryptedPassword);

        // Checking for roles existence
        user.setRoles(checkRolesExistenceAndGet(user.getRoles()));

        user = userRepository.save(user);
        log.debug("Created Information for User: {}", user);

        return user;
    }

    public void deleteUserByLogin(String login) {
        User user = userRepository.findOneByLogin(login);
        if (user == null) {
            throw EntityNotFoundException.createWithUserLogin(login);
        }

        userRepository.delete(user);
        log.debug("Deleted User: {}", user);
    }

    public User getUserWithAuthorities() {
        User user = userRepository.findOneByLogin(SecurityUtils.getCurrentUser().getUsername());
        user.getRoles().size(); // eagerly load the association
        return user;
    }

    public User getUserWithAuthorities(String id) {
        User user = userRepository.findOne(id);
        if (user == null) {
            throw EntityNotFoundException.createWithUserId(id);
        }

        user.getRoles().size(); // eagerly load the association
        return user;
    }

    public User getUserWithAuthoritiesByLogin(String login) {
        User user = userRepository.findOneByLogin(login);
        if (user == null) {
            throw EntityNotFoundException.createWithUserLogin(login);
        }

        user.getRoles().size(); // eagerly load the association
        return user;
    }

    private List<Role> checkRolesExistenceAndGet(List<Role> roles) {
        List<Role> checkedRoles = new ArrayList<>(roles.size());
        for (Role role : roles) {
            Role roleFromDB = roleRepository.findOne(role.getId());
            if (roleFromDB == null) {
                throw EntityNotFoundException.createWithRoleId(role.getId());
            }
            checkedRoles.add(roleFromDB);
        }

        return checkedRoles;
    }
}