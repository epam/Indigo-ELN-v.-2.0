package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.config.UserHolder;
import com.epam.indigoeln.common.exception.EntityNotFoundException;
import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.UserRef;
import com.epam.indigoeln.eln.entity.RoleEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.entity.UserInfo;
import com.epam.indigoeln.eln.mapper.UserMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.repository.RoleRepository;
import com.epam.indigoeln.eln.repository.UserRepository;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.vertx.mutiny.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.epam.indigoeln.common.util.ModelUtil.firstNotNull;
import static com.epam.indigoeln.common.util.ModelUtil.loadResource;
import static com.epam.indigoeln.eln.util.ModelUtil.updateDates;
import static com.google.common.base.Preconditions.checkArgument;

@Slf4j
@Transactional
@ApplicationScoped
public class UserService {

    private static final byte[] DEFAULT_PICTURE_SMALL = loadResource(UserService.class, "/user-default-picture-small.png");
    private static final byte[] DEFAULT_PICTURE_LARGE = loadResource(UserService.class, "/user-default-picture.png");

    @Inject
    UserHolder userHolder;
    @Inject
    UserRepository userRepository;
    @Inject
    UserMapper userMapper;
    @Inject
    ExternalUserService externalUserService;
    @Inject
    RoleRepository roleRepository;
    @Inject
    ACLService aclService;
    @Inject
    @CacheName("users.byUsername")
    Cache cacheByUsername;
    @Inject
    @CacheName("users.byID")
    Cache cacheByID;
    @Inject
    Vertx vertx;

    @Transactional(Transactional.TxType.SUPPORTS) // cached methods don't require transaction
    public UserInfo getCurrentUser() {
        return getUserInfo(userHolder.getUserName());
    }

    @Transactional(Transactional.TxType.SUPPORTS) // cached methods don't require transaction
    public CurrentUserDTO getCurrentUserDTO() {
        return userMapper.infoToCurrentUserDTO(getCurrentUser());
    }

    public UserEntity getCurrentUserEntity() {
        return userRepository.getReference(getCurrentUser().getId());
    }

    public UserEntity getEntity(String username) {
        return userRepository.getReference(getUserInfo(username).getId());
    }

    @Transactional(Transactional.TxType.SUPPORTS) // cached methods don't require transaction
    public UserInfo getUserInfo(UserRef ref) {
        return getUserInfo(ref.getUsername());
    }

    @Transactional(Transactional.TxType.SUPPORTS) // cached methods don't require transaction
    public UserInfo getUserInfo(String username) {
        return cacheByUsername.get(username, u -> doLoadUser(null, u))
                .await().indefinitely();
    }

    @Transactional(Transactional.TxType.SUPPORTS) // cached methods don't require transaction
    public UserRef getUserInfo(UUID id) {
        return cacheByID.get(id, i -> doLoadUser(i, null))
                .await().indefinitely();
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    UserInfo doLoadUser(@Nullable UUID id, @Nullable String username) {
        checkArgument(id != null || username != null);
        UserInfo user = id != null ? userRepository.findByID(id) : userRepository.findByUsername(username);
        if (user == null) {
            throw new EntityNotFoundException(ELNEntityType.USER, firstNotNull(id, username));
        }
        return user;
    }

    public UserDTO getUser(String username) {
        return userRepository.load(username);
    }

    public byte[] getUserPicture(String username, @Nullable Boolean large) {
        // TODO support user picture
        return Boolean.TRUE.equals(large) ? DEFAULT_PICTURE_LARGE : DEFAULT_PICTURE_SMALL;
    }

    public List<UserRef> suggestUsers(@Nullable String search) {
        return userRepository.suggest(search);
    }

    public UserDTO createUser(UserRequest request) {
        aclService.ensureTopLevelAccess(ApplicationPermission.MANAGE_USERS);
        UserEntity entity = userMapper.requestToUser(request);
        Set<RoleEntity> roles = StreamEx.of(firstNotNull(request.getRoles(), List.of()))
                .map(ref -> roleRepository.get(ref.getId()))
                .toSet();
        entity.getRoles().addAll(roles);
        updateDates(entity, getCurrentUserEntity());
        userRepository.persist(entity);
        userRepository.flush(); // make sure all constraints hold
        externalUserService.createUser(request);
        cacheByUsername.invalidate(request.getUsername()).await().indefinitely();
        cacheByID.invalidate(entity.getId()).await().indefinitely();
        return userMapper.entityToDetailsDTO(entity);
    }

    public Page<UserDTO> getUsers(@Nullable String search, Paging paging) {
        return userRepository.findAll(search, paging);
    }
}
