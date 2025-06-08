package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.mapper.ProjectMapper;
import com.epam.indigoeln.eln.model.EntityType;
import com.epam.indigoeln.eln.model.Paging;
import com.epam.indigoeln.eln.model.UserRef;
import com.epam.indigoeln.eln.util.Conditions;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import java.util.List;

@ApplicationScoped
public class UserRepository extends BaseRepository<UserEntity> {

    protected static final Sort USER_SORT = io.quarkus.panache.common.Sort.by("displayName");

    @Inject
    ProjectMapper projectMapper;

    public UserRepository() {
        super(EntityType.USER);
    }

    public @Nullable UserEntity findByUsername(String username) {
        return find("username", username).firstResult();
    }

    public List<UserRef> suggest(@Nullable String search, Paging paging) {
        return doFind(
                new Conditions()
                        .addIfNotNull("lower(displayName) like ?", search != null ? search.toLowerCase() + '%' : null),
                paging,
                USER_SORT,
                null,
                projectMapper::userRef
        );
    }
}
