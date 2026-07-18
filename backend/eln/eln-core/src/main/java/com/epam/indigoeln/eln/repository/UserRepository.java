package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.common.exception.EntityNotFoundException;
import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.UserRef;
import com.epam.indigoeln.eln.common.repository.BaseRepository;
import com.epam.indigoeln.eln.common.util.Conditions;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.entity.UserInfo;
import com.epam.indigoeln.eln.mapper.UserMapper;
import com.epam.indigoeln.eln.model.ELNEntityType;
import com.epam.indigoeln.eln.model.UserDTO;
import io.quarkus.panache.common.Sort;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class UserRepository extends BaseRepository<UserEntity> {

    protected static final Sort USER_SORT = Sort.by("displayName");

    @Inject
    UserMapper userMapper;

    public UserRepository() {
        super(ELNEntityType.USER, UserEntity.class);
    }

    @Nullable
    public UserInfo findByUsername(String username) {
        return doFindOne(new Conditions().add("username=?", username), em.getEntityGraph("User.info"), userMapper::convertUserInfo);
    }

    @Nullable
    public UserInfo findByID(UUID id) {
        return doFindOne(new Conditions().add("id=?", id), em.getEntityGraph("User.info"), userMapper::convertUserInfo);
    }

    public List<UserRef> suggest(@Nullable String search) {
        Conditions conditions = new Conditions();
        if (search != null) {
            String searchQuery = search.toLowerCase() + '%';
            conditions.add("lower(displayName) like ? OR lower(firstName) like ? OR lower(lastName) like ?", searchQuery, searchQuery, searchQuery);
        }

        return doFind(
                conditions,
                Paging.DEFAULT,
                USER_SORT,
                null,
                UserEntity::toInfo
        );
    }

    public UserDTO load(String username) {
        UserDTO user = doFindOne(
                new Conditions().add("username=?", username),
                em.getEntityGraph("User.details"),
                userMapper::entityToDetailsDTO
        );
        if (user == null) {
            throw new EntityNotFoundException(ELNEntityType.USER, username);
        }
        return user;
    }

    public Page<UserDTO> findAll(@Nullable String search, Paging paging) {
        Conditions conditions = new Conditions();
        if (search != null) {
            conditions.add("firstName ilike ? or lastName ilike ? or displayName ilike ?", search + '%', search + '%', search + '%');
        }
        return doFindWithTotals(
                conditions,
                paging,
                DEFAULT_SORT,
                null,
                userMapper::entityToDTO
        );
    }

}
