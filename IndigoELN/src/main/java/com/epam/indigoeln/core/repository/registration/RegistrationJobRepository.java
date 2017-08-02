package com.epam.indigoeln.core.repository.registration;

import com.epam.indigoeln.core.model.RegistrationJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class RegistrationJobRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public RegistrationJob save(RegistrationJob registrationJob) {
        mongoTemplate.save(registrationJob);
        return registrationJob;
    }

    public RegistrationJob findOneForCheck() {
        return mongoTemplate.findAndModify(
                Query.query(Criteria.where("registrationStatus").is(RegistrationStatus.Status.IN_PROGRESS.toString())),
                Update.update("registrationStatus", RegistrationStatus.Status.IN_CHECK.toString()),
                RegistrationJob.class);
    }
}
