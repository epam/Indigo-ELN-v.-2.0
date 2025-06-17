package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.config.UserInfo;
import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.mapper.UserMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@Slf4j
@Transactional
@ApplicationScoped
public class UserService {

    @Inject
    UserInfo userInfo;
    @Inject
    UserRepository userRepository;
    @Inject
    Provider<UserContext> userContext;
    @Inject
    UserMapper userMapper;

    public UserEntity getCurrentUser() {
        UserEntity user = userContext.get().getCurrentUser();
        if (user == null) {
            user = getOrCreateUser(userInfo.getUserName(), userInfo.getFirstName(), userInfo.getLastName(), new ApplicationRole[0]);
            userContext.get().setCurrentUser(user);
        }
        return user;
    }

    public UserEntity getOrCreateUser(String username) {
        return getOrCreateUser(username, null, null, new ApplicationRole[0]);
    }

    public UserEntity getOrCreateUser(String username, @Nullable String firstName, @Nullable String lastName, ApplicationRole[] roles) {
        UserEntity user = userRepository.findByUsername(username);
        if (user == null) {
            user = new UserEntity(username, firstName, lastName, ModelUtil.formatUser(firstName, lastName, username), roles);
            userRepository.persist(user);
        }
        return user;
    }

    public UserDetailsDTO getUser(UUID id) {
        return userRepository.loadDetails(id);
    }

    public UserEntity getUserEntity(@NotNull UUID userID) {
        return userRepository.get(userID);
    }

    public List<UserRef> suggestUsers(@Nullable String search, Paging paging) {
        return userRepository.suggest(search, paging);
    }

    public @NotNull @Valid UserDetailsDTO createUser(UserRequest request) {
        var entity = userMapper.requestToUser(request);
        userRepository.persist(entity);
        return userMapper.entityToDetailsDTO(entity);
    }

    public @NotNull @Valid Page<UserDTO> getUsers(String search, Paging paging) {
        var list = userRepository.findAll(search, paging);
        return Page.of(paging, list.total(), list.list());
    }

    @Getter
    @Setter
    @RequestScoped
    public static class UserContext {

        @Nullable
        private volatile UserEntity currentUser;
    }
}
