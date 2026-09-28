package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.common.exception.EntityNotFoundException;
import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.UserRef;
import com.epam.indigoeln.eln.common.entity.IdentifiableEntity_;
import com.epam.indigoeln.eln.common.repository.BaseRepository;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.entity.UserEntity_;
import com.epam.indigoeln.eln.entity.UserInfo;
import com.epam.indigoeln.eln.mapper.UserMapper;
import com.epam.indigoeln.eln.model.ELNEntityType;
import com.epam.indigoeln.eln.model.UserDTO;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.Tuple;
import org.hibernate.query.criteria.CriteriaDefinition;
import org.hibernate.query.criteria.JpaRoot;

import java.util.List;
import java.util.UUID;

import static com.epam.indigoeln.common.util.ModelUtil.map;

@ApplicationScoped
public class UserRepository extends BaseRepository<UserEntity> {

    @Inject
    UserMapper userMapper;

    public UserRepository() {
        super(ELNEntityType.USER, UserEntity.class);
    }

    @Nullable
    public UserInfo findByUsername(String username) {
        CriteriaDefinition<UserEntity> criteria = new CriteriaDefinition<>(em, UserEntity.class) {{
            JpaRoot<UserEntity> root = from(UserEntity.class);
            select(root);
            where(root.get(UserEntity_.username).equalTo(username));
        }};
        return map(doFindOne(criteria, em.getEntityGraph("User.info")), userMapper::convertUserInfo);
    }

    @Nullable
    public UserInfo findByID(UUID id) {
        CriteriaDefinition<UserEntity> criteria = new CriteriaDefinition<>(em, UserEntity.class) {{
            JpaRoot<UserEntity> root = from(UserEntity.class);
            select(root);
            where(root.get(IdentifiableEntity_.id).equalTo(id));
        }};
        return map(doFindOne(criteria, em.getEntityGraph("User.info")), userMapper::convertUserInfo);
    }

    public List<UserRef> suggest(@Nullable String search) {
        CriteriaDefinition<UserEntity> criteria = new CriteriaDefinition<>(em, UserEntity.class) {{
            JpaRoot<UserEntity> root = from(UserEntity.class);
            select(root);
            if (search != null) {
                String searchQuery = search.toLowerCase() + '%';
                where(or(
                        ilike(root.get(UserEntity_.displayName), searchQuery),
                        ilike(root.get(UserEntity_.firstName), searchQuery),
                        ilike(root.get(UserEntity_.lastName), searchQuery),
                        ilike(root.get(UserEntity_.username), searchQuery)
                ));
            }
            orderBy(asc(root.get(UserEntity_.displayName)));
        }};

        List<UserEntity> list = doFind(criteria, Paging.DEFAULT, null);
        return map(list, UserEntity::toInfo);
    }

    public UserDTO load(String username) {
        CriteriaDefinition<UserEntity> criteria = new CriteriaDefinition<>(em, UserEntity.class) {{
            JpaRoot<UserEntity> root = from(UserEntity.class);
            select(root);
            where(root.get(UserEntity_.username).equalTo(username));
        }};
        UserEntity user = doFindOne(criteria, em.getEntityGraph("User.details"));
        if (user == null) {
            throw new EntityNotFoundException(ELNEntityType.USER, username);
        }
        return userMapper.entityToDetailsDTO(user);
    }

    public Page<UserDTO> findAll(@Nullable String search, Paging paging) {
        CriteriaDefinition<Tuple> criteria = new CriteriaDefinition<>(em, Tuple.class) {{
            JpaRoot<UserEntity> root = from(UserEntity.class);
            select(tuple(root.id(), count(literal(1), createWindow())));
            if (search != null) {
                String searchQuery = search + '%';
                where(or(
                        ilike(root.get(UserEntity_.firstName), searchQuery),
                        ilike(root.get(UserEntity_.lastName), searchQuery),
                        ilike(root.get(UserEntity_.displayName), searchQuery),
                        ilike(root.get(UserEntity_.username), searchQuery)
                ));
            }
            orderBy(desc(root.get(UserEntity_.modifiedAt)));
        }};
        Page<UserEntity> page = doFindWithTotals(criteria, paging, null);

        return map(page, userMapper::entityToDTO);
    }

}
