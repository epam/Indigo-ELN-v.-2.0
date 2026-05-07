package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.entity.UserInfo;
import com.epam.indigoeln.eln.mapper.UserMapper;
import com.epam.indigoeln.eln.model.EntityType;
import com.epam.indigoeln.eln.model.Page;
import com.epam.indigoeln.eln.model.Paging;
import com.epam.indigoeln.eln.model.UserDTO;
import com.epam.indigoeln.eln.util.Conditions;
import io.quarkus.panache.common.Sort;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class UserRepository extends BaseRepository<UserEntity> {

    protected static final Sort USER_SORT = io.quarkus.panache.common.Sort.by("displayName");

    @Inject
    UserMapper userMapper;

    public UserRepository() {
        super(EntityType.USER, UserEntity.class);
    }

    public @Nullable UserInfo findByUsername(String username) {
        return doFindOne(new Conditions().add("username=?", username), em.getEntityGraph("User.info"), userMapper::entityToInfo);
    }

    public List<com.epam.indigoeln.common.model.UserRef> suggest(@Nullable String search) {
        return doFind(
                new Conditions()
                        .addIfNotNull("lower(displayName) like ?", search != null ? search.toLowerCase() + '%' : null),
                Paging.DEFAULT,
                USER_SORT,
                null,
                UserEntity::toRef
        );
    }

    public UserDTO loadDetails(UUID id) {
        return doLoadDetails(
                id,
                em.getEntityGraph("User.details"),
                userMapper::entityToDetailsDTO
        );
    }

    public Page<UserDTO> findAll(@Nullable String search, @Nullable String username, Paging paging) {
        return doFindWithTotals(
                new Conditions()
                        .addIfNotNull("full_text_search(searchVector, websearch_to_tsquery('english', ?))", search)
                        .addIfNotNull("username = ?", username),
                paging,
                DEFAULT_SORT,
                null,
                userMapper::entityToDTO
        );
    }

}
