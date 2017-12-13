package com.epam.indigoeln.core.service.user;

import com.epam.indigoeln.core.model.Role;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.repository.role.RoleRepository;
import com.epam.indigoeln.core.repository.user.UserRepository;
import com.epam.indigoeln.core.security.SecurityUtils;
import com.epam.indigoeln.core.service.exception.DuplicateFieldException;
import com.epam.indigoeln.core.service.exception.EntityNotFoundException;
import com.epam.indigoeln.core.service.exception.OperationDeniedException;
import com.google.common.base.Strings;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

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

    @Autowired
    public UserService(SessionRegistry sessionRegistry,
                       PasswordEncoder passwordEncoder,
                       UserRepository userRepository,
                       RoleRepository roleRepository,
                       @Value("${password.validation}") String passwordRegex) {
        this.sessionRegistry = sessionRegistry;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        passwordValidationPattern = Pattern.compile(passwordRegex);
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

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
                && (user.getRoles().size() != executingUser.getRoles().size()
                || !user.getRoles().containsAll(executingUser.getRoles()))) {
            throw OperationDeniedException.createUserDeleteOperation(executingUser.getId());
        }

        User savedUser = userRepository.save(user);
        LOGGER.debug("Created Information for User: {}", savedUser);

        // check for significant changes and perform logout for user
        SecurityUtils.checkAndLogoutUser(savedUser, sessionRegistry);
        return user;
    }

    private void checkUserPassword(String password) {
        if (!passwordValidationPattern.matcher(password).matches()) {
            throw OperationDeniedException.createUserWithNotValidPassword();
        }
    }

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

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    public User getUserWithAuthorities() {
        User user = userRepository.findOneByLogin(SecurityUtils.getCurrentUser().getUsername());
        user.getRoles().size(); // eagerly load the association
        return user;
    }

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    public User getUserWithAuthorities(String id) {
        User user = userRepository.findOne(id);
        if (user == null) {
            throw EntityNotFoundException.createWithUserId(id);
        }

        user.getRoles().size(); // eagerly load the association
        return user;
    }

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    public User getUserWithAuthoritiesByLogin(String login) {
        User user = userRepository.findOneByLogin(login);
        if (user == null) {
            throw EntityNotFoundException.createWithUserLogin(login);
        }

        user.getRoles().size(); // eagerly load the association
        return user;
    }

    public Page<User> searchUserByLoginOrFirstNameOrLastNameOrSystemRoleNameWithPaging(
            String loginOrFirstNameOrLastNameOrRoleName, Pageable pageable
    ) {
        List<String> roleIds = roleRepository
                .findByNameLikeIgnoreCase(loginOrFirstNameOrLastNameOrRoleName)
                .map(Role::getId).collect(toList());

        return userRepository.findByLoginIgnoreCaseLikeOrFirstNameIgnoreCaseLikeOrLastNameIgnoreCaseLikeOrRolesIdIn(
                loginOrFirstNameOrLastNameOrRoleName,
                loginOrFirstNameOrLastNameOrRoleName,
                loginOrFirstNameOrLastNameOrRoleName,
                roleIds,
                pageable);
    }

    private Set<Role> checkRolesExistenceAndGet(Set<Role> roles) {
        Set<Role> checkedRoles = new HashSet<>(roles.size());
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