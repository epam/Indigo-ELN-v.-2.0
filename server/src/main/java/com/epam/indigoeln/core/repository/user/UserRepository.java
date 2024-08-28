/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.core.repository.user;

import com.epam.indigoeln.core.model.Role;
import com.epam.indigoeln.core.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Spring Data MongoDB repository for the User entity.
 */
public interface UserRepository extends MongoRepository<User, String> {

    User findOneByLogin(String login);

    User findOneByLoginAndActivated(String login, boolean activated);

    boolean existsByLogin(String login);

    @Query(value = "{'roles': {'$ref': '" + Role.COLLECTION_NAME + "', '$id': ?0}}", count = true)
    long countByRoleId(String roleId);

    @Query(value = "{'roles': {'$ref': '" + Role.COLLECTION_NAME + "', '$id': ?0}}")
    Collection<User> findByRoleId(String roleId);

    Set<User> findAllByRolesIdIn(List<String> roleIds);

    List<User> findAll(Iterable<String> ids);

    List<User> findByLoginIgnoreCaseLikeOrFirstNameIgnoreCaseLikeOrLastNameIgnoreCaseLikeOrRolesIdIn(
            String login,
            String firstName,
            String lastName,
            List<String> roleId
    );
}
