package com.epam.indigoeln.eln.controller;


import com.epam.indigoeln.eln.api.*;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.service.UserService;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Path;

import java.util.List;
import java.util.UUID;

@Path(BaseAPI.BASE_PATH)
public class UsersResource implements UsersAPI {

    @Inject
    UserService userService;


    @Override
    public @NotNull @Valid UserDetailsDTO createUser(UserRequest request) {
        return userService.createUser(request);
    }

    @Override
    public @NotNull @Valid Page<UserDTO> getUsers(@Nullable String search, Paging paging) {
        return userService.getUsers(search, paging);
    }

    @Override
    public @NotNull @Valid UserDetailsDTO getUser(UUID userId) {
        return userService.getUser(userId);
    }

    @Override
    public @NotNull @Valid List<ACLEntryDTO> updateUserAccess(UUID userId, List<AccessForm> form) {
        return List.of();
    }

    @Override
    public List<UserRef> suggestUsers(@Nullable String search, Paging paging) {
        return userService.suggestUsers(search, paging);
    }
}
