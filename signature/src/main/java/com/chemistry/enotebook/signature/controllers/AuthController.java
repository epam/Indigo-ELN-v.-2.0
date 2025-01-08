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

import com.chemistry.enotebook.signature.security.LoginResult;
import com.chemistry.enotebook.signature.security.LoginUser;
import com.chemistry.enotebook.signature.security.SessionCache;
import com.chemistry.enotebook.signature.security.UserDetailsImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    AuthenticationManager authenticationManager;

    @RequestMapping(value = "/loginProcess", method = RequestMethod.POST)
    public @ResponseBody LoginResult login(@RequestBody LoginUser user, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Authentication authenticationToken = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());
        try {
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetailsImpl ud = (UserDetailsImpl) authentication.getPrincipal();

            String username = authentication.getName();
            String sessionId = request.getSession().getId();

            SessionCache.getInstance().addSession(sessionId, username);

            response.addCookie(new Cookie("InternalSessionId", sessionId));

            return new LoginResult().success().username(username).user(ud.getDbUser().asJson().toString());
        } catch (Exception e) {
            log.error("Error authenticating user: " + e.getMessage(), e);
            response.sendError(403);
            return new LoginResult().failed();
        }
    }

    @RequestMapping(value = "/me", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody LoginResult getMe() throws IOException {
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return new LoginResult().success().username(user.getUsername()).user(user.getDbUser().asJson().toString());
    }
}
