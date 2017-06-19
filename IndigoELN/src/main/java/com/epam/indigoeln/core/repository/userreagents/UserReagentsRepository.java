package com.epam.indigoeln.core.repository.userreagents;

import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.model.UserReagents;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the UserReagents entity.
 */
public interface UserReagentsRepository extends MongoRepository<UserReagents, String> {

    UserReagents findByUser(User user);

}