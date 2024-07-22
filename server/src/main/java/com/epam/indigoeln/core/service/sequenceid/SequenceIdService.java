/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.core.service.sequenceid;

import com.epam.indigoeln.core.model.SequenceId;
import com.epam.indigoeln.core.repository.sequenceid.SequenceIdRepository;
import com.epam.indigoeln.core.service.exception.EntityNotFoundException;
import com.epam.indigoeln.core.service.experiment.ExperimentService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.core.util.BatchComponentUtil;
import com.epam.indigoeln.core.util.SequenceIdUtil;
import com.epam.indigoeln.web.rest.dto.ExperimentDTO;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Provides functionality for generating ids.
 */
@Service
public class SequenceIdService {

    private static final String FIELD_SEQ_ID = "sequence";

    private final Lock batchNumberLock = new ReentrantLock();

    /**
     * SequenceIdRepository instance.
     */
    @Autowired
    private SequenceIdRepository repository;

    /**
     * ExperimentService instance for working with experiments.
     */
    @Autowired
    private ExperimentService experimentService;

    /**
     * UserService instance for working with users.
     */
    @Autowired
    private UserService userService;

    /**
     * Generates next project id and persists it into sequence collection as new document.
     *
     * @return new value of project id
     */
    public String getNextProjectId() {
        Pageable request = new PageRequest(0, 1, new Sort(Sort.Direction.DESC, FIELD_SEQ_ID));
        Page<SequenceId> page = repository.findAll(request);
        SequenceId newSequenceId = new SequenceId(page.getContent().isEmpty()
                ? 1L : page.getContent().get(0).getSequence() + 1L);
        return repository.save(newSequenceId).getSequence().toString();
    }

    /**
     * Generates next notebook id and persists it as new embedded document within project into sequence collection.
     * Full notebook id looks like "{projectId}-{nextNotebookSeqId}".
     *
     * @param projectId parent project id
     * @return new value of notebook id (full)
     */
    public String getNextNotebookId(String projectId) {

        SequenceId projectSequenceId = getProjectSequenceId(projectId);

        if (projectSequenceId.getChildren() == null) {
            projectSequenceId.setChildren(new ArrayList<>());
        }

        Long nextNotebookId = projectSequenceId.getChildren().stream()
                .mapToLong(SequenceId::getSequence)
                .max()
                .orElse(0L) + 1L;
        projectSequenceId.getChildren().add(new SequenceId(ObjectId.get().toHexString(), nextNotebookId));
        repository.save(projectSequenceId);

        return SequenceIdUtil.buildFullId(projectId, nextNotebookId.toString());
    }

    /**
     * Generates next experiment id and persists it as new embedded sub document within project notebook
     * into sequence collection.
     * Full experiment id looks like "{projectId}-{notebookSeqId}-{nextExperimentSeqId}".
     *
     * @param projectId  project id
     * @param notebookId notebook seq id
     * @return new value of experiment id (full)
     */
    public String getNextExperimentId(String projectId, String notebookId) {

        SequenceId projectSequenceId = getProjectSequenceId(projectId);
        SequenceId notebookSequenceId = getNotebookSequenceId(projectSequenceId, notebookId);

        if (notebookSequenceId.getChildren() == null) {
            notebookSequenceId.setChildren(new ArrayList<>());
        }

        Long nextExperimentId = notebookSequenceId.getChildren().stream()
                .mapToLong(SequenceId::getSequence)
                .max()
                .orElse(0L) + 1L;
        notebookSequenceId.getChildren().add(new SequenceId(ObjectId.get().toHexString(), nextExperimentId));
        repository.save(projectSequenceId);

        return SequenceIdUtil.buildFullId(projectId, notebookId, nextExperimentId.toString());
    }

    /**
     * Get next notebook batch number.
     *
     * @param projectId                  project id
     * @param notebookId                 notebook id
     * @param experimentId               experiment id
     * @param clientLatestBatchNumberStr latest batch number received from client
     * @return next batch number value
     */
    public String getNotebookBatchNumber(String projectId, String notebookId, String experimentId,
                                         String clientLatestBatchNumberStr) {
        batchNumberLock.lock();
        try {
            ExperimentDTO experiment = Optional.ofNullable(
                    experimentService.getExperiment(projectId, notebookId, experimentId,
                            userService.getUserWithAuthorities())).
                    orElseThrow(() -> EntityNotFoundException.createWithExperimentId(experimentId));

            int dbLastBatchNumber = BatchComponentUtil.getLastBatchNumber(experiment).orElse(0);
            int clientLastBatchNumber = BatchComponentUtil.isValidBatchNumber(clientLatestBatchNumberStr)
                    ? Integer.parseInt(clientLatestBatchNumberStr) : 0;

            //compare last values from Client and Database, increment biggest
            int nextBatchNumber = Math.max(dbLastBatchNumber, clientLastBatchNumber) + 1;
            return BatchComponentUtil.formatBatchNumber(nextBatchNumber);

        } finally {
            batchNumberLock.unlock();
        }

    }

    private SequenceId getProjectSequenceId(String projectId) {
        return repository.findBySequence(Long.valueOf(projectId)).
                orElseThrow(() -> EntityNotFoundException.createWithProjectId(projectId));
    }

    private SequenceId getNotebookSequenceId(SequenceId projectSequenceId, String notebookId) {
        List<SequenceId> children = projectSequenceId.getChildren() != null
                ? projectSequenceId.getChildren() : Collections.emptyList();

        return children.stream().filter(s -> notebookId.equals(s.getSequence().toString())).findAny().
                orElseThrow(() -> EntityNotFoundException.createWithNotebookId(notebookId));

    }
}
