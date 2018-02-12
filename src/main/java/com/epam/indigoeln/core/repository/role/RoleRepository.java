package com.epam.indigoeln.core.repository.role;

import com.epam.indigoeln.core.model.Role;
import com.epam.indigoeln.core.security.Authority;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.stream.Stream;

public interface RoleRepository extends MongoRepository<Role, String> {

    Stream<Role> findByNameLikeIgnoreCase(String nameLike);

    Stream<Role> findAllByAuthorities(Authority authority);
}
