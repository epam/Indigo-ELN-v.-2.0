package com.epam.indigoeln.core.repository.sequenceid;

import com.epam.indigoeln.core.model.SequenceId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class SequenceIdRepository {

    private static final String FIELD_SEQ = "seq";
    private static final String FIELD_ID = "_id";

    public static final String EXPERIMENT_SEQ_KEY = "experiment";
    public static final String NOTEBOOK_SEQ_KEY   = "notebook";
    public static final String PROJECT_SEQ_KEY    = "project";

    @Autowired
    private MongoOperations mongoOperations;

    public Long getNextSequenceId(String key) {
        Query query = new Query(Criteria.where(FIELD_ID).is(key));

        Update update = new Update();
        update.inc(FIELD_SEQ, 1);

        FindAndModifyOptions options = new FindAndModifyOptions();
        options.returnNew(true);

        SequenceId seqId = mongoOperations.findAndModify(query, update, options, SequenceId.class);
        return seqId.getSeq();
    }

    public Long getNextExperimentId(){
        return getNextSequenceId(EXPERIMENT_SEQ_KEY);
    }

    public Long getNextProjectId(){
        return getNextSequenceId(PROJECT_SEQ_KEY);
    }

    public Long getNextNotebookId(){
        return getNextSequenceId(NOTEBOOK_SEQ_KEY);
    }

}
