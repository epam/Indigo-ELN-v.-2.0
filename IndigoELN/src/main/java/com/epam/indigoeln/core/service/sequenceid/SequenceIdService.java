package com.epam.indigoeln.core.service.sequenceid;

import com.epam.indigoeln.core.model.SequenceId;
import com.epam.indigoeln.core.repository.sequenceid.SequenceIdRepository;
import com.epam.indigoeln.core.service.exception.EntityNotFoundException;
import com.epam.indigoeln.core.util.SequenceIdUtil;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class SequenceIdService {

    private static final String FIELD_SEQ_ID = "sequence";

    @Autowired
    private SequenceIdRepository repository;

    /**
     * Generates next project id and persists it into sequence collection as new document
     * @return new value of project id
     */
    public String getNextProjectId() {
        Pageable request = new PageRequest(0, 1, new Sort(Sort.Direction.DESC, FIELD_SEQ_ID));
        Page<SequenceId> page = repository.findAll(request);
        SequenceId newSequenceId = new SequenceId(page.getContent().isEmpty() ? 1L : page.getContent().get(0).getSequence() + 1L);
        return  repository.save(newSequenceId).getSequence().toString();
    }

    /**
     * Generates next notebook id and persists it as new embedded document within project into sequence collection
     * Full notebook id looks like "{projectId}-{nextNotebookSeqId}"
     * @param projectId parent project id
     * @return new value of notebook id (full)
     */
    public String getNextNotebookId(String projectId) {

        SequenceId projectSequenceId = getProjectSequenceId(projectId);

        if(projectSequenceId.getChildren() == null) {
            projectSequenceId.setChildren(new ArrayList<>());
        }

        Long nextNotebookId = projectSequenceId.getChildren().stream().mapToLong(SequenceId::getSequence).max().orElse(0L) + 1L;
        projectSequenceId.getChildren().add(new SequenceId(ObjectId.get().toHexString(), nextNotebookId));
        repository.save(projectSequenceId);

        return SequenceIdUtil.buildFullId(projectId, nextNotebookId.toString());
    }

    /**
     * Generates next experiment id and persists it as new embedded sub document within project notebook into sequence collection
     * Full experiment id looks like "{projectId}-{notebookSeqId}-{nextExperimentSeqId}"
     *
     * @param projectId project id
     * @param notebookId notebook seq id
     *
     * @return new value of experiment id (full)
     */
    public String getNextExperimentId(String projectId, String notebookId) {

        SequenceId projectSequenceId = getProjectSequenceId(projectId);
        SequenceId notebookSequenceId = getNotebookSequenceId(projectSequenceId, notebookId);

        if(notebookSequenceId.getChildren() == null) {
            notebookSequenceId.setChildren(new ArrayList<>());
        }

        Long nextExperimentId = notebookSequenceId.getChildren().stream().mapToLong(SequenceId::getSequence).max().orElse(0L) + 1L;
        notebookSequenceId.getChildren().add(new SequenceId(ObjectId.get().toHexString(), nextExperimentId));
        repository.save(projectSequenceId);

        return SequenceIdUtil.buildFullId(projectId, notebookId, nextExperimentId.toString());
    }

    public String getNotebookBatchNumber(String projectId, String notebookId, String experimentId) {

        SequenceId projectSequenceId = getProjectSequenceId(projectId);
        SequenceId experimentSequenceId = getExperimentSequenceId(projectSequenceId, notebookId, experimentId);
        Long nextSequenceNumber;
        if(experimentSequenceId.getChildren() == null) {
            experimentSequenceId.setChildren(new ArrayList<>());
            nextSequenceNumber = 1L;
            experimentSequenceId.getChildren().add(new SequenceId(ObjectId.get().toHexString(), nextSequenceNumber));
        } else {
            SequenceId sequenceId = experimentSequenceId.getChildren().get(0);
            nextSequenceNumber = sequenceId.getSequence() + 1L;
            sequenceId.setSequence(nextSequenceNumber);
        }

        repository.save(projectSequenceId);
        return SequenceIdUtil.formatBatchNumber(nextSequenceNumber);
    }

    private SequenceId getProjectSequenceId(String projectId) {
        return repository.findBySequence(Long.valueOf(projectId)).
                orElseThrow(() -> new EntityNotFoundException("Can't find sequence id for project", projectId));
    }

    private SequenceId getNotebookSequenceId(SequenceId projectSequenceId, String notebookId) {
        List<SequenceId> children = projectSequenceId.getChildren() != null ? projectSequenceId.getChildren() : Collections.emptyList();

        return  children.stream().filter(s -> notebookId.equals(s.getSequence().toString())).findAny().
                     orElseThrow(() -> new EntityNotFoundException("Can't find sequence id for notebook", notebookId));

    }

    private SequenceId getExperimentSequenceId(SequenceId projectSequenceId, String notebookId, String experimentId) {
        SequenceId notebookSequenceId = getNotebookSequenceId(projectSequenceId, notebookId);
        List<SequenceId> children = notebookSequenceId.getChildren() != null ? notebookSequenceId.getChildren() : Collections.emptyList();

        return children.stream().filter(s -> experimentId.equals(s.getSequence().toString())).findAny().
                    orElseThrow(() -> new EntityNotFoundException("Can't find sequence id for notebook", notebookId));

    }
}
