package com.epam.indigoeln.web.controller;

import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/service")
public class UserController {
    @Autowired
    private UserService service;

    @RequestMapping(value = "/getUsers", method = RequestMethod.GET)
    public List<String> getAllUsers() {
        return service.getUsers().stream().map(User::getName).collect(Collectors.toList());
    }
}



