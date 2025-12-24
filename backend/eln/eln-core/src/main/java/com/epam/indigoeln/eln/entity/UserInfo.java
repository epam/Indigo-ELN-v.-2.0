package com.epam.indigoeln.eln.entity;

import com.epam.indigoeln.eln.model.ApplicationPermission;
import lombok.Value;

import java.util.Set;
import java.util.UUID;

@Value
public class UserInfo {

    UUID id;
    String username;
    String displayName;
    Set<ApplicationPermission> permissions;
}
