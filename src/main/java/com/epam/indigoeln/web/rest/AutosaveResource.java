package com.epam.indigoeln.web.rest;

import com.epam.indigoeln.core.model.AutosaveItem;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.service.autosave.AutosaveService;
import com.epam.indigoeln.core.service.user.UserService;
import com.mongodb.BasicDBObject;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api
@RestController
@RequestMapping(AutosaveResource.URL_MAPPING)
public class AutosaveResource {

    static final String URL_MAPPING = "api/autosave";

    @Autowired
    private UserService userService;

    @Autowired
    private AutosaveService autosaveService;

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public void save(@PathVariable("id") String id, @RequestBody BasicDBObject content) {
        final User user = userService.getUserWithAuthorities();
        AutosaveItem item = new AutosaveItem(id, user, content);
        autosaveService.save(item);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public BasicDBObject get(@PathVariable("id") String id) {
        final User user = userService.getUserWithAuthorities();
        return autosaveService.get(id, user);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") String id) {
        autosaveService.delete(id);
    }

}
