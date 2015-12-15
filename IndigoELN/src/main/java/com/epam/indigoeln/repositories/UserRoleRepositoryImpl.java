package com.epam.indigoeln.repositories;

import com.epam.indigoeln.documents.UserRole;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public class UserRoleRepositoryImpl implements UserRoleRepository {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public UserRoleRepositoryImpl(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Collection<UserRole> getUserRoles(String userId) {
        Query searchQuery = new Query(Criteria.where("user_id").is(new ObjectId(userId)));
        return mongoTemplate.find(searchQuery, UserRole.class);
    }

}
