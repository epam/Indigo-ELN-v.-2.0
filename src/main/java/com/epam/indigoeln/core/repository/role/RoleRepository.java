package com.epam.indigoeln.core.repository.role;

import com.epam.indigoeln.core.model.Role;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
public class RoleRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public void addRole(Role role) {
        mongoTemplate.save(role);
    }

    public void deleteRole(String id) {
        mongoTemplate.remove(query(where("_id").is(new ObjectId(id))), Role.class);
    }

    public Collection<Role> getRoles(Collection<String> rolesIds) {
        Query searchQuery = new Query(where("_id").in(
                rolesIds.stream().map(ObjectId::new).collect(Collectors.toList())
        ));
        return mongoTemplate.find(searchQuery, Role.class);
    }
}
