package com.epam.indigoeln.core.repository.user;


import com.epam.indigoeln.core.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Spring Data MongoDB repository for the User entity.
 */
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findOneByLogin(String login);

    @Override
    void delete(User t);

}
