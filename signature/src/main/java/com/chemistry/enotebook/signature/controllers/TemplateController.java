/****************************************************************************
 * Copyright (C) 2009-2015 EPAM Systems
 * 
 * This file is part of Indigo Signature Service.
 * 
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 3 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 * 
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 ***************************************************************************/
package com.chemistry.enotebook.signature.controllers;

import com.chemistry.enotebook.signature.database.DatabaseConnector;
import com.chemistry.enotebook.signature.entity.Template;
import com.chemistry.enotebook.signature.entity.User;
import com.chemistry.enotebook.signature.Util;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;

@Controller
@RequestMapping("/")
public class TemplateController {

    @Autowired
    private DatabaseConnector database;

    @RequestMapping(value = "/api/getTemplates", method = RequestMethod.GET)
    public @ResponseBody String getTemplates(@RequestParam("username")String username) {
        return getTemplatesByUsername(username);
    }

    @RequestMapping(value = "/getTemplates", method = RequestMethod.GET)
    public @ResponseBody String getTemplates(HttpServletRequest request) {
        return getTemplatesByUsername(Util.getUsername(request));
    }

    public String getTemplatesByUsername(String username) {
        Collection<Template> templates = database.getTemplates(username);
        return Util.generateObjectContainingArray("Templates", templates).toString();
    }

    @RequestMapping(value = "/createTemplate", method = RequestMethod.POST)
    public @ResponseBody String createTemplate(HttpServletRequest request) throws IOException {
        User user = database.getUserByUsername(Util.getUsername(request));
        String jsonBody = IOUtils.toString(request.getInputStream());
        Template template = new Template(jsonBody, database);
        template.setAuthor(user);
        database.addTemplate(template);
        return template.asJson().toString();
    }

    @RequestMapping(value = "/removeTemplate", method = RequestMethod.GET)
    public @ResponseBody String removeTemplate(@RequestParam("id") String id) {
        try {
            Template template = database.getTemplate(UUID.fromString(id));
            database.removeTemplate(template.getId());
            return template.asJson().toString();
        } catch(Exception e) {
            return Util.generateErrorJsonString(e.getMessage());
        }
    }

    @RequestMapping(value = "/updateTemplate", method = RequestMethod.POST)
    public @ResponseBody String updateTemplate(HttpServletRequest request) throws IOException {
        String jsonBody = IOUtils.toString(request.getInputStream());
        Template template = new Template(jsonBody, database);
        template.setAuthor(database.getUserByUsername(Util.getUsername(request)));
        database.removeTemplate(template.getId());
        database.addTemplate(template);
        return template.asJson().toString();
    }

    @RequestMapping(value = "/getReasons", method = RequestMethod.GET)
    public @ResponseBody String getReasons() {
        Set<Map> reasons = database.getAllReasons();

        JsonArrayBuilder reasonsJsonArray = createArrayBuilder();

        for(Map reason : reasons) {
            reasonsJsonArray.add(createObjectBuilder().add("id", Integer.valueOf(reason.get("reasonid").toString())).add("name", (String) reason.get("text")));
        }

        JsonObject statusesJson = createObjectBuilder()
                .add("Reasons", reasonsJsonArray.build())
                .build();

        return statusesJson.toString();
    }

    @RequestMapping(value = "/api/getReasons", method = RequestMethod.GET)
    public @ResponseBody String getReasonsAPI() {
        return getReasons();
    }
}
