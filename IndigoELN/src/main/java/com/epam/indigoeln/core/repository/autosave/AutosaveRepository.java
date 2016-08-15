package com.epam.indigoeln.core.repository.autosave;

import com.epam.indigoeln.core.model.AutosaveItem;
import com.epam.indigoeln.core.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AutosaveRepository extends MongoRepository<AutosaveItem, String> {

    AutosaveItem findByIdAndUser(String id, User user);

}
