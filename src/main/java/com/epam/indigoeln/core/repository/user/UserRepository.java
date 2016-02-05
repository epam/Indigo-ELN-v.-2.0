package com.epam.indigoeln.core.repository.user;

import com.epam.indigoeln.core.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the User entity.
 */
public interface UserRepository extends MongoRepository<User, String> {

    User findOneByLogin(String login);
}