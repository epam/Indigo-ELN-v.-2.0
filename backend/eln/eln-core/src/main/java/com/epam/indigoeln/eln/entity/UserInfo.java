package com.epam.indigoeln.eln.entity;

import com.epam.indigoeln.common.model.UserRef;
import com.epam.indigoeln.eln.model.ApplicationPermission;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

import java.util.Set;
import java.util.UUID;

@Getter
public class UserInfo extends UserRef {

    @JsonIgnore
    private final UUID id;

    @JsonIgnore
    private final Set<ApplicationPermission> permissions;

    public UserInfo(String username, String displayName, UUID id, Set<ApplicationPermission> permissions) {
        super(username, displayName);
        this.id = id;
        this.permissions = permissions;
    }
}
