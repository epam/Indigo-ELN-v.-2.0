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
package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.AutosaveItem;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.service.autosave.AutosaveService;
import com.epam.indigoeln.core.service.user.UserService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AutosaveResource.URL_MAPPING)
public class AutosaveResource {

    static final String URL_MAPPING = "api/autosave";

    @Autowired
    private UserService userService;

    @Autowired
    private AutosaveService autosaveService;

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public void save(@PathVariable("id") String id, @RequestBody Document content) {
        final User user = userService.getUserWithAuthorities();
        AutosaveItem item = new AutosaveItem(id, user, content);
        autosaveService.save(item);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Document get(@PathVariable("id") String id) {
        final User user = userService.getUserWithAuthorities();
        return autosaveService.get(id, user);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") String id) {
        autosaveService.delete(id);
    }

}
