package com.epam.indigoeln.repositories;

import com.epam.indigoeln.documents.Role;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.stream.Collectors;

@Repository
public class RoleRepositoryImpl implements RoleRepository {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public RoleRepositoryImpl(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void addRole(Role role) {
        mongoTemplate.save(role);
    }

    @Override
    public void deleteRole(String id) {
        Query query = new Query(Criteria.where("_id").is(new ObjectId(id)));
        mongoTemplate.remove(query, Role.class);
    }

    @Override
    public Collection<Role> getRoles(Collection<String> rolesIds) {
        Query searchQuery = new Query(Criteria.where("_id").in(
                rolesIds.stream().map(ObjectId::new).collect(Collectors.toList())
        ));
        return mongoTemplate.find(searchQuery, Role.class);
    }

}
