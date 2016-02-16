package com.epam.indigoeln.core.repository.sequenceid;

import com.epam.indigoeln.core.model.SequenceId;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SequenceIdRepository  extends MongoRepository<SequenceId, Long> {

    Optional<SequenceId> findBySequence(Long sequence);
}
