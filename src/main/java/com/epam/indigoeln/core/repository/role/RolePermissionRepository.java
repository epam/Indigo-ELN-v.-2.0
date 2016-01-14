package com.epam.indigoeln.core.repository.role;

import com.epam.indigoeln.core.model.RolePermission;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;

public interface RolePermissionRepository extends MongoRepository<RolePermission, String> {

    Collection<RolePermission> findByRoleIdAndValue(String roleId, String value);

}
