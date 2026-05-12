package com.epam.indigoeln.signature.service;

import com.epam.indigoeln.common.config.UserHolder;
import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.signature.entity.UserEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Transactional
@ApplicationScoped
public class UserService {

    @Inject
    EntityManager em;
    @Inject
    UserHolder userInfo;

    // TODO cache for current request
    public UserEntity getCurrentUser() {
        return getOrCreateUser(userInfo.getUserName(), userInfo.getFirstName(), userInfo.getLastName());
    }

    public UserEntity findUser(String username) {
        List<UserEntity> foundUsers = em.createQuery("from User where username = :username", UserEntity.class)
                .setParameter("username", username)
                .setMaxResults(1)
                .getResultList();
        return !foundUsers.isEmpty() ? foundUsers.getFirst() : null;
    }

    public UserEntity getOrCreateUser(String username, String firstName, String lastName) {
        UserEntity user = findUser(username);
        boolean exists = user != null;
        if (user == null) {
            user = new UserEntity();
            user.setUsername(username);
        }
        if (user.getFirstName() == null && user.getLastName() == null) {
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setDisplayName(ModelUtil.formatUser(user.getFirstName(), user.getLastName(), username));
        }
        if (!exists) {
            em.persist(user);
        }
        return user;
    }
}
