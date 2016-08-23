package com.epam.indigoeln.core.service.autosave;

import com.epam.indigoeln.core.model.AutosaveItem;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.repository.autosave.AutosaveRepository;
import com.mongodb.BasicDBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AutosaveService {

    @Autowired
    private AutosaveRepository autosaveRepository;

    public void save(AutosaveItem item) {
        autosaveRepository.save(item);
    }

    public BasicDBObject get(String id, User user) {
        final AutosaveItem autosaveItem = autosaveRepository.findByIdAndUser(id, user);
        return autosaveItem == null ? null : autosaveItem.getContent();
    }

    public void delete(String id) {
        autosaveRepository.delete(id);
    }
}
