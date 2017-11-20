package com.epam.indigoeln.core.service.autosave;

import com.epam.indigoeln.core.model.AutosaveItem;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.repository.autosave.AutosaveRepository;
import com.mongodb.BasicDBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The AutosaveService provides methods for auto saving
 */
@Service
public class AutosaveService {

    /**
     * AutosaveRepository instance for access to database
     */
    @Autowired
    private AutosaveRepository autosaveRepository;

    /**
     * Saves object
     * @param item Object for saving
     */
    public void save(AutosaveItem item) {
        autosaveRepository.save(item);
    }

    public BasicDBObject get(String id, User user) {
        final AutosaveItem autosaveItem = autosaveRepository.findByIdAndUser(id, user);
        return autosaveItem == null ? null : autosaveItem.getContent();
    }

    /**
     * Removes object
     * @param id Object's identifier
     */
    public void delete(String id) {
        autosaveRepository.delete(id);
    }
}
