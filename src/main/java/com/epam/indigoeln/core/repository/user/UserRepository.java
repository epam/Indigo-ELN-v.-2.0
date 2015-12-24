package com.epam.indigoeln.core.repository.user;

import com.epam.indigoeln.core.model.User;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public class UserRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public void saveUser(User user) {
        mongoTemplate.save(user);
    }

    public void deleteUser(String id) {
        Query query = new Query(Criteria.where("_id").is(new ObjectId(id)));
        mongoTemplate.remove(query);
    }

    public User getUser(String name) {
        Query searchQuery = new Query(Criteria.where("name").is(name));
        return mongoTemplate.findOne(searchQuery, User.class);
    }

    public Collection<User> getUsers() {
        return mongoTemplate.findAll(User.class);
    }
}
