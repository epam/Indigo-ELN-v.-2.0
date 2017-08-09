package com.epam.indigoeln.core.repository.signature;

import com.epam.indigoeln.core.model.ExperimentStatus;
import com.epam.indigoeln.core.model.SignatureJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class SignatureJobRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public SignatureJob save(SignatureJob signatureJob) {
        mongoTemplate.save(signatureJob);
        return signatureJob;
    }

    public SignatureJob findOneForCheck() {
        return mongoTemplate.findAndModify(
                Query.query(Criteria.where("experimentStatus").is(ExperimentStatus.SUBMITTED)),
                Update.update("experimentStatus", ExperimentStatus.IN_CHECK),
                SignatureJob.class);
    }
}
