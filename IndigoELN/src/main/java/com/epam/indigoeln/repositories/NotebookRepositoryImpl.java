package com.epam.indigoeln.repositories;

import com.epam.indigoeln.documents.Notebook;
import com.mongodb.DBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Collection;

@Repository
public class NotebookRepositoryImpl implements NotebookRepository {
    @Autowired
    private MongoTemplate mongoTemplate;

    public Notebook createNewNotebook(Notebook notebook) {
        mongoTemplate.save(notebook);
        return notebook;
    };

    public Collection<Notebook> getAllNotebooks(){
        return mongoTemplate.findAll(Notebook.class);
    };

    public Notebook getNotebook(String id) {
        Query searchQuery = new Query(Criteria.where("id").is(id));
        return mongoTemplate.findOne(searchQuery, Notebook.class);
    };

    public Notebook updateNotebook(Notebook notebook) {
        Query searchQuery = new Query(Criteria.where("id").is(notebook.getId()));
        //Update update = Update.fromDBObject(notebook);
        //mongoTemplate.updateFirst(searchQuery, update, Notebook.class);
        return notebook;
    };

    public void deleteNotebook(String id) {
        Query searchQuery = new Query(Criteria.where("id").is(id));
        mongoTemplate.findAndRemove(searchQuery, Notebook.class);
    };
}
