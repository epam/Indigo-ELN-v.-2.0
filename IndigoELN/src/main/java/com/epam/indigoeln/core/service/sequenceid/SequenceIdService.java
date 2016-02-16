package com.epam.indigoeln.core.service.sequenceid;

import com.epam.indigoeln.core.model.SequenceId;
import com.epam.indigoeln.core.repository.sequenceid.SequenceIdRepository;
import com.epam.indigoeln.core.service.exception.EntityNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class SequenceIdService {

    private static final String FIELD_SEQ_ID = "id";

    @Autowired
    private SequenceIdRepository repository;

    public String getNextProjectId() {

        Pageable request = new PageRequest(0, 1, new Sort(Sort.Direction.DESC, FIELD_SEQ_ID));
        Page<SequenceId> page = repository.findAll(request);
        SequenceId newSequenceId = new SequenceId(page.getContent().isEmpty() ? 1L : page.getContent().get(0).getId() + 1L);
        return  repository.save(newSequenceId).getId().toString();
    }

    public String getNextNotebookId(String projectId) {

        SequenceId sequenceId = Optional.ofNullable(repository.findOne(Long.valueOf(projectId))).
                orElseThrow(() -> new EntityNotFoundException("Can't find sequence id for project", projectId));

        if(sequenceId.getChildren() == null) {
            sequenceId.setChildren(new ArrayList<>());
        }

        Long nextNotebookId = sequenceId.getChildren().stream().mapToLong(SequenceId::getId).max().orElse(0L) + 1L;
        sequenceId.getChildren().add(new SequenceId(nextNotebookId));
        repository.save(sequenceId);

        return projectId + "-" + nextNotebookId;
    }

//    public String getNextExperimentId(String projectId, String notebookId) {
//
//        SequenceId sequenceId = Optional.ofNullable(
//                mongoOperations.findOne(new Query(Criteria.where(FIELD_SEQ).is(projectId)), SequenceId.class)).
//                orElseThrow(() -> new EntityNotFoundException("Can't find sequence id for project", projectId));
//
//        if(sequenceId.getChildren() == null) {
//            sequenceId.setChildren(new HashMap<>());
//        }
//
//        Long nextExperimentId = sequenceId.getChildren().containsKey(notebookId) ? sequenceId.getChildren().get(notebookId) + 1L : 1L;
//        sequenceId.getChildren().put(Long.valueOf(notebookId), nextExperimentId);
//        mongoOperations.save(sequenceId);
//        return projectId + "-" + notebookId + "-" + nextExperimentId;
//    }
}
