package com.epam.indigoeln.repositories;

import com.epam.indigoeln.documents.User;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public UserRepositoryImpl(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void saveUser(User user) {
        mongoTemplate.save(user);
    }

    @Override
    public void deleteUser(String id) {
        Query query = new Query(Criteria.where("_id").is(new ObjectId(id)));
        mongoTemplate.remove(query);
    }

    @Override
    public User getUser(String name) {
        Query searchQuery = new Query(Criteria.where("name").is(name));
        return mongoTemplate.findOne(searchQuery, User.class);
    }

    @Override
    public Collection<User> getUsers() {
        return mongoTemplate.findAll(User.class);
    }
}
