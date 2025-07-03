package com.epam.indigoeln.eln.controller;


import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.api.BaseAPI;
import com.epam.indigoeln.eln.api.RoleAPI;
import com.epam.indigoeln.eln.api.UserAPI;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.service.RoleService;
import com.epam.indigoeln.eln.service.UserService;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Path;

import java.util.List;
import java.util.UUID;

@Path(BaseAPI.BASE_PATH)
public class RoleResource implements RoleAPI {

    @Inject
    RoleService roleService;

    @Valid
    @NotNull
    @Override
    public RoleDTO createRole(@Valid @NotNull RoleRequest request) {
        return roleService.createRole(request);
    }

    @Valid
    @NotNull
    @Override
    public List<RoleDTO> getRoles() {
        return roleService.getRoles();
    }

    @Valid
    @NotNull
    @Override
    public List<RoleRef> suggestRoles() {
        return roleService.suggestRoles();
    }

    @Valid
    @NotNull
    @Override
    public RoleDTO updateRole(@NotNull UUID roleID, @Valid @NotNull RoleEditRequest request) {
        return roleService.updateRole(roleID, request);
    }

    @Override
    public void deleteRole(@NotNull UUID roleID) {
        roleService.deleteRole(roleID);
    }
}
