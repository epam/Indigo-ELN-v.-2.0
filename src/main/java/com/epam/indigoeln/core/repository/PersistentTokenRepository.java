package com.epam.indigoeln.core.repository;

import com.epam.indigoeln.core.model.PersistentToken;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the PersistentToken entity.
 */
public interface PersistentTokenRepository extends MongoRepository<PersistentToken, String> {

}
