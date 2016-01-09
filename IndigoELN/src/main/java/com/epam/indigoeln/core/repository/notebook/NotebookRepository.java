package com.epam.indigoeln.core.repository.notebook;

import com.epam.indigoeln.core.model.Notebook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
public class NotebookRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public Notebook createNewNotebook(Notebook notebook) {
        mongoTemplate.save(notebook);
        return notebook;
    }

    public Collection<Notebook> getAllNotebooks() {
        return mongoTemplate.findAll(Notebook.class);
    }

    public Notebook getNotebook(String id) {
        return mongoTemplate.findOne(query(where("id").is(id)), Notebook.class);
    }

    public Notebook updateNotebook(Notebook notebook) {
        Query searchQuery = new Query(where("id").is(notebook.getId()));
        //Update update = Update.fromDBObject(notebook);
        //mongoTemplate.updateFirst(searchQuery, update, Notebook.class);
        return notebook;
    }

    public void deleteNotebook(String id) {
        mongoTemplate.findAndRemove(query(where("id").is(id)), Notebook.class);
    }
}
