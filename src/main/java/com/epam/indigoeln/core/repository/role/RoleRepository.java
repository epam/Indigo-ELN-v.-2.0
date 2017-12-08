package com.epam.indigoeln.core.repository.role;

import com.epam.indigoeln.core.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoleRepository extends MongoRepository<Role, String> {

    Page<Role> findByNameLikeIgnoreCase(String nameLike, Pageable pageable);
}