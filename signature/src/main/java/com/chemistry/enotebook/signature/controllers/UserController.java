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
import com.chemistry.enotebook.signature.entity.User;
import com.chemistry.enotebook.signature.Util;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.json.JsonObject;
import java.io.IOException;
import java.util.Collection;

@Controller
@RequestMapping("/")
public class UserController {

    @Autowired
    private DatabaseConnector database;

    @RequestMapping(value = "/findUsers", method = RequestMethod.GET)
    public @ResponseBody String findUsers(@RequestParam(value = "particle", defaultValue = "", required = false) String particle, @RequestParam(value = "limit", defaultValue = "0", required = false) int limit) {
        Collection<User> users = database.getUsersByPartOfName(particle, limit);
        return Util.generateObjectContainingArray("Users", users).toString();
    }

    @RequestMapping(value = "/createUser", method = RequestMethod.POST)
    public @ResponseBody String createUser(HttpServletRequest request) {
        try {
            JsonObject requestJson = Util.requestBodyToJsonObject(request);
            User user = User.generateUserFromJson(requestJson);
            database.createUser(user);
            return user.asJson().toString();
        } catch(Exception e) {
            return Util.generateErrorJsonString(e.getMessage());
        }
    }

    @RequestMapping(value = "/updateUser", method = RequestMethod.POST)
    public @ResponseBody String updateUser(HttpServletRequest request) {
        try {
            JsonObject requestJson = Util.requestBodyToJsonObject(request);
            User user = User.generateUserFromJson(requestJson);
            database.updateUser(user);
            return user.asJson().toString();
        } catch(Exception e) {
            return Util.generateErrorJsonString(e.getMessage());
        }
    }

    @RequestMapping(value = "/getUser", method = RequestMethod.GET)
    public @ResponseBody String getUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = Util.getUsername(request);

        if (username != null) {
            return database.getUserByUsername(username).asJson().toString();
        }

        response.sendError(401);
        return "";
    }
}
