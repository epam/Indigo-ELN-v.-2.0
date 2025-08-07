package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.config.UserInfo;
import com.epam.indigoeln.common.exception.AccessDeniedException;
import com.epam.indigoeln.common.exception.EntityNotFoundException;
import com.epam.indigoeln.eln.entity.RoleEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.mapper.UserMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.repository.RoleRepository;
import com.epam.indigoeln.eln.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.epam.indigoeln.common.util.ModelUtil.loadResource;
import static com.epam.indigoeln.eln.util.ModelUtil.updateDates;

@Slf4j
@Transactional
@ApplicationScoped
public class UserService {

    private static final byte[] DEFAULT_PICTURE_SMALL = loadResource(UserService.class, "/user-default-picture-small.png");
    private static final byte[] DEFAULT_PICTURE_LARGE = loadResource(UserService.class, "/user-default-picture.png");

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
    @Inject
    RoleRepository roleRepository;

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

    public byte[] getUserPicture(UUID userId, @Nullable Boolean large) {
        return Boolean.TRUE.equals(large) ? DEFAULT_PICTURE_LARGE : DEFAULT_PICTURE_SMALL;
    }

    public UserEntity getUserEntity(UUID userID) {
        return userRepository.get(userID);
    }

    public List<UserRef> suggestUsers(@Nullable String search) {
        return userRepository.suggest(search);
    }

    public UserDTO createUser(UserRequest request) {
        UserEntity entity = userMapper.requestToUser(request);
        Set<RoleEntity> roles = StreamEx.ofNullable(request.getRoles())
                .flatMap(Collection::stream)
                .map(ref -> roleRepository.get(ref.getId()))
                .toSet();
        entity.setRoles(roles);
        updateDates(entity, getCurrentUser());
        userRepository.persist(entity);
        externalUserService.createUser(request);
        return userMapper.entityToDetailsDTO(entity);
    }

    public Page<UserDTO> getUsers(String search, String username, Paging paging) {
        return userRepository.findAll(search, username, paging);
    }

    @Getter
    @Setter
    @RequestScoped
    public static class UserContext {

        @Nullable
        private volatile UserEntity currentUser;
    }
}
