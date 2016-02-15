package com.epam.indigoeln.core.repository.sequenceid;

import com.epam.indigoeln.core.model.SequenceId;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SequenceIdRepository  extends MongoRepository<SequenceId, Long> {
//
//    public Long getNextSequenceId(String key) {
//        Query query = new Query(Criteria.where(FIELD_ID).is(key));
//
//        Update update = new Update();
//        update.inc(FIELD_SEQ, 1);
//
//        FindAndModifyOptions options = new FindAndModifyOptions();
//        options.returnNew(true);
//
//        SequenceId seqId = mongoOperations.findAndModify(query, update, options, SequenceId.class);
//        return seqId.getSeq();
//    }
//
//    public Long getNextExperimentId(){
//        return getNextSequenceId(EXPERIMENT_SEQ_KEY);
//    }
//
//    public Long getNextProjectId(){
////        return getNextSequenceId(PROJECT_SEQ_KEY);
//        Query query = new Query();
//        query.with(new Sort(Sort.Direction.DESC, FIELD_SEQ)).limit(1);
//        SequenceId latest = mongoOperations.findOne(query, SequenceId.class);
//        SequenceId sequenceId = new SequenceId();
//        sequenceId.setSeq(latest != null ? latest.getSeq() : 1L);
//
//        mongoOperations.save(sequenceId);
//
//
//    }
//
//    public Long getNextNotebookId(){
//        return getNextSequenceId(NOTEBOOK_SEQ_KEY);
//    }
//
//    public Long getNextTemplateId(){
//        return getNextSequenceId(TEMPLATE_SEQ_KEY);
//    }
}
