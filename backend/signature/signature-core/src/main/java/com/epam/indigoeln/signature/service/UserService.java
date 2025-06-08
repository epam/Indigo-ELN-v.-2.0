package com.epam.indigoeln.signature.service;

import com.epam.indigoeln.common.config.UserInfo;
import com.epam.indigoeln.signature.entity.UserEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
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
    UserInfo userInfo;

    // TODO cache for current request
    public UserEntity getCurrentUser() {
        return getOrCreateUser(userInfo.getUserName(), userInfo.getFirstName(), userInfo.getLastName());
    }

    public UserEntity findUser(String username) {
        List<UserEntity> foundUsers = em.createQuery("from UserAccount where username = :username", UserEntity.class)
                .setParameter("username", username)
                .setMaxResults(1)
                .getResultList();
        return !foundUsers.isEmpty() ? foundUsers.getFirst() : null;
    }

    public UserEntity getOrCreateUser(String username, String firstName, String lastName) {
        log.info("!!! getOrCreateUser: username={}, firstName={}, lastName={}", username, firstName, lastName);
        UserEntity user = findUser(username);
        if (user == null) {
            user = new UserEntity();
            user.setUsername(username);
        }
        if (user.getFirstName() == null && user.getLastName() == null) {
            user.setFirstName(firstName);
            user.setLastName(lastName);
        }
        if (user.getId() == null) {
            em.persist(user);
        }
        return user;
    }

    @RequestScoped
    public static class UserContext {

    }
}
