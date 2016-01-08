package com.epam.indigoeln.core.repository.experiment;

import com.epam.indigoeln.core.model.Experiment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public class ExperimentRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public Experiment saveExperiment(Experiment experiment) {
        mongoTemplate.save(experiment);
        return experiment;
    }

    public Collection<Experiment> getAllExperiments() {
        return mongoTemplate.findAll(Experiment.class);
    }

//    public Collection<Experiment> getAllUserExperiments(User user) {
//        Query query = new Query(Criteria.where("author").is(user));
//        return mongoTemplate.find(query, Experiment.class);
//    }

    public Experiment getExperiment(String id) {
        Query query = new Query(Criteria.where("id").is(id));
        return mongoTemplate.findOne(query, Experiment.class);
    }

    public void deleteExperiment(String id) {
        Query query = new Query(Criteria.where("id").is(id));
        mongoTemplate.findAndRemove(query, Experiment.class);
    }
}
