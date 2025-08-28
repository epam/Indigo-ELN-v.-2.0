package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.mapper.UserMapper;
import com.epam.indigoeln.eln.model.*;
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
        super(EntityType.USER);
    }

    public @Nullable UserEntity findByUsername(String username) {
        return find("username", username).firstResult();
    }

    public List<UserRef> suggest(@Nullable String search) {
        return doFind(
                new Conditions()
                        .addIfNotNull("lower(displayName) like ?", search != null ? search.toLowerCase() + '%' : null),
                Paging.DEFAULT,
                USER_SORT,
                null,
                userMapper::userRef
        );
    }

    public UserDTO loadDetails(UUID id) {
        return doLoadDetails(
                id,
                em.getEntityGraph("User.details"),
                userMapper::entityToDetailsDTO
        );
    }

    public Page<UserDTO> findAll(@Nullable String search, String username, Paging paging) {
        return doFindWithTotals(
                new Conditions()
                        .addIfNotNull("full_text_search(searchVector, to_tsquery('english', ?))", search)
                        .addIfNotNull("username = ?", username),
                paging,
                DEFAULT_SORT,
                em.getEntityGraph("Project.list"),
                userMapper::entityToDTO
        );
    }

}
