package com.epam.indigoeln.core.repository.user;

import com.epam.indigoeln.core.model.UserRole;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;

public interface UserRoleRepository extends MongoRepository<UserRole, String> {
    UserRole findByUserIdAndRoleId(String userId, String roleId);

    Collection<UserRole> findByUserId(String userId);
}
