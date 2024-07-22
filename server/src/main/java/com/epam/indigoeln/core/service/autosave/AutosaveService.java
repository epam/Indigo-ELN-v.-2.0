/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.core.service.autosave;

import com.epam.indigoeln.core.model.AutosaveItem;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.repository.autosave.AutosaveRepository;
import com.mongodb.BasicDBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The AutosaveService provides methods for auto saving.
 */
@Service
public class AutosaveService {

    /**
     * AutosaveRepository instance for access to database.
     */
    @Autowired
    private AutosaveRepository autosaveRepository;

    /**
     * Saves object.
     *
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
     * Removes object.
     *
     * @param id Object's identifier
     */
    public void delete(String id) {
        autosaveRepository.delete(id);
    }
}
