package com.epam.indigoeln.core.repository.user;

import com.epam.indigoeln.core.model.Role;
import com.epam.indigoeln.core.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

/**
 * Spring Data MongoDB repository for the User entity.
 */
public interface UserRepository extends MongoRepository<User, String> {

    User findOneByLogin(String login);

    @Query(value = "{'roles': {'$ref': '" + Role.COLLECTION_NAME + "', '$id': ?0}}", count = true)
    long countUsersByRoleId(String roleId);
}