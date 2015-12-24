package com.epam.indigoeln.core.repository.user;

import com.epam.indigoeln.core.model.UserRole;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public class UserRoleRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public void saveUserRole(UserRole userRole) {
        mongoTemplate.save(userRole);
    }

    public void deleteUserRole(String id) {
        Query query = new Query(Criteria.where("_id").is(new ObjectId(id)));
        mongoTemplate.remove(query, UserRole.class);
    }

    public UserRole getUserRole(String userId, String roleId) {
        Query searchQuery = new Query(
                Criteria.where("user_id").is(new ObjectId(userId))
                        .andOperator(Criteria.where("role_id").is(new ObjectId(roleId))));
        return mongoTemplate.findOne(searchQuery, UserRole.class);
    }

    public Collection<UserRole> getUserRoles(String userId) {
        Query searchQuery = new Query(Criteria.where("user_id").is(new ObjectId(userId)));
        return mongoTemplate.find(searchQuery, UserRole.class);
    }
}
