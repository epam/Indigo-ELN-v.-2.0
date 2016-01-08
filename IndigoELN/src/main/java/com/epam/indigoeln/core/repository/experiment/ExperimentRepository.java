package com.epam.indigoeln.core.repository.experiment;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
public class ExperimentRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public Experiment saveExperiment(Experiment experiment) {
        mongoTemplate.save(experiment);
        return experiment;
    }

    public Experiment findExperiment(String id) {
        return mongoTemplate.findOne(query(where("id").is(id)), Experiment.class);
    }

    public Collection<Experiment> findAllExperiments() {
        return mongoTemplate.findAll(Experiment.class);
    }

    public Collection<Experiment> findUserExperiments(User user) {
        return mongoTemplate.find(query(where("author").is(user)), Experiment.class);
    }

    public void deleteExperiment(String id) {
        mongoTemplate.findAndRemove(query(where("id").is(id)), Experiment.class);
    }

}
