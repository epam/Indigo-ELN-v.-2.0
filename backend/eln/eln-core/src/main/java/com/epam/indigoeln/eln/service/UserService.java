package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.config.UserInfo;
import com.epam.indigoeln.common.exception.AccessDeniedException;
import com.epam.indigoeln.common.exception.EntityNotFoundException;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.mapper.UserMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.repository.UserRepository;
import com.epam.indigoeln.eln.util.ModelUtil;
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
    @Inject
    ExternalUserService externalUserService;

    public UserEntity getCurrentUser() {
        UserEntity user = userContext.get().getCurrentUser();
        if (user == null) {
            String username = userInfo.getUserName();
            userInfo.getFirstName();
            userInfo.getLastName();
            user = userRepository.findByUsername(username);
            if (user == null) {
                throw new AccessDeniedException(username);
            }
            userContext.get().setCurrentUser(user);
        }
        return user;
    }

    public UserDTO getUser(UUID id) {
        return userRepository.loadDetails(id);
    }

    public UserDTO getUser(String username) {
        UserEntity user = userRepository.findByUsername(username);
        if (user == null) {
            throw new EntityNotFoundException(EntityType.USER, username);
        }
        return userMapper.entityToDetailsDTO(user);
    }

    public UserEntity getUserEntity(@NotNull UUID userID) {
        return userRepository.get(userID);
    }

    public List<UserRef> suggestUsers(@Nullable String search, Paging paging) {
        return userRepository.suggest(search, paging);
    }

    public @NotNull @Valid UserDTO createUser(UserRequest request) {
        UserEntity entity = userMapper.requestToUser(request);
        ModelUtil.updateDates(entity, getCurrentUser());
        userRepository.persist(entity);
        externalUserService.createUser(request);
        return userMapper.entityToDetailsDTO(entity);
    }

    public @NotNull @Valid Page<UserDTO> getUsers(String search, String username, Paging paging) {
        var list = userRepository.findAll(search, username, paging);
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
