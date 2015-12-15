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
    public void saveUserRole(UserRole userRole) {
        mongoTemplate.save(userRole);
    }

    @Override
    public void deleteUserRole(String id) {
        Query query = new Query(Criteria.where("_id").is(new ObjectId(id)));
        mongoTemplate.remove(query, UserRole.class);
    }

    @Override
    public UserRole getUserRole(String userId, String roleId) {
        Query searchQuery = new Query(
                Criteria.where("user_id").is(new ObjectId(userId))
                        .andOperator(Criteria.where("role_id").is(new ObjectId(roleId))));
        return mongoTemplate.findOne(searchQuery, UserRole.class);
    }

    @Override
    public Collection<UserRole> getUserRoles(String userId) {
        Query searchQuery = new Query(Criteria.where("user_id").is(new ObjectId(userId)));
        return mongoTemplate.find(searchQuery, UserRole.class);
    }

}
