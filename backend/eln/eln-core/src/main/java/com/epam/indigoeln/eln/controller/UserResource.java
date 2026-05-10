package com.epam.indigoeln.eln.controller;


import com.epam.indigoeln.common.model.UserRef;
import com.epam.indigoeln.eln.api.BaseAPI;
import com.epam.indigoeln.eln.api.UserAPI;
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
public class UserResource implements UserAPI {

    @Inject
    UserService userService;

    @Override
    public CurrentUserDTO getCurrentUser() {
        return userService.getCurrentUserDTO();
    }

    @Override
    public @NotNull @Valid UserDTO createUser(UserRequest request) {
        return userService.createUser(request);
    }

    @Override
    public @NotNull @Valid Page<UserDTO> getUsers(@Nullable String search, @Nullable String username, Paging paging) {
        return userService.getUsers(search, username, paging);
    }

    @Override
    public @NotNull @Valid UserDTO getUser(UUID userId) {
        return userService.getUser(userId);
    }

    @Override
    public byte[] getUserPicture(UUID userId, @Nullable Boolean large) {
        return userService.getUserPicture(userId, large);
    }

    @Override
    public List<UserRef> suggestUsers(@Nullable String search) {
        return userService.suggestUsers(search);
    }
}
