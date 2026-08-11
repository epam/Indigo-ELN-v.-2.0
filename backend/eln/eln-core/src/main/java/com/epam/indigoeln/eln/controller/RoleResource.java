package com.epam.indigoeln.eln.controller;


import com.epam.indigoeln.eln.api.BaseAPI;
import com.epam.indigoeln.eln.api.RoleAPI;
import com.epam.indigoeln.eln.model.RoleDTO;
import com.epam.indigoeln.eln.model.RoleEditRequest;
import com.epam.indigoeln.eln.model.RoleRef;
import com.epam.indigoeln.eln.model.RoleRequest;
import com.epam.indigoeln.eln.service.RoleService;
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

    @NotNull
    @Override
    public List<@Valid RoleDTO> getRoles() {
        return roleService.getRoles();
    }

    @NotNull
    @Override
    public List<@Valid RoleRef> suggestRoles() {
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
