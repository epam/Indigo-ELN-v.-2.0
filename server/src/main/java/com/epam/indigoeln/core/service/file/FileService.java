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
package com.epam.indigoeln.core.service.file;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.repository.file.FileRepository;
import com.epam.indigoeln.core.repository.project.ProjectRepository;
import com.epam.indigoeln.core.service.exception.EntityNotFoundException;
import com.epam.indigoeln.core.service.exception.FileNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collections;
import java.util.Optional;

/**
 * Provides a number of methods for access to files in database.
 */
@Service
public class FileService {

    /**
     * Repository for access to files in database.
     */
    @Autowired
    private FileRepository fileRepository;

    /**
     * Repository for access to projects in database.
     */
    @Autowired
    private ProjectRepository projectRepository;

    /**
     * Repository for access to experiments in database.
     */
    @Autowired
    private ExperimentRepository experimentRepository;

    /**
     * Returns all project files (with paging).
     *
     * @param projectId Project's identifier
     * @param pageable  Pagination information
     * @return Page with GridFS files
     */
    public Page<GridFsResource> getAllFilesByProjectId(String projectId, Pageable pageable) {
        Project project = projectRepository.findById(projectId).
                orElseThrow(() -> EntityNotFoundException.createWithProjectId(projectId));
        return fileRepository.findAll(project.getFileIds(), pageable);
    }

    /**
     * Returns all experiment files.
     *
     * @param experimentId Experiment's identifier
     * @return Page with GridFS files
     */
    public Page<GridFsResource> getAllFilesByExperimentId(String experimentId) {
        Experiment experiment = experimentRepository.findById(experimentId).
                orElseThrow(() -> EntityNotFoundException.createWithExperimentId(experimentId));
        if (experiment.getFileIds().isEmpty()) {
            return new PageImpl<>(Collections.emptyList());
        } else {
            return fileRepository.findAll(experiment.getFileIds(), PageRequest.of(0, experiment.getFileIds().size()));
        }
    }

    /**
     * Returns GridFS file by id.
     *
     * @param id File's identifier
     * @return GridFS file
     */
    public GridFsResource getFileById(String id) {
        GridFsResource gridFSDBFile = fileRepository.findOneById(id);
        if (gridFSDBFile == null) {
            throw new FileNotFoundException(id);
        }
        return gridFSDBFile;
    }

    /**
     * Creates a new file for the project.
     *
     * @param projectId   Project's identifier
     * @param content     Input stream of bytes for file
     * @param filename    File's name
     * @param contentType Content type of file
     * @param user        User
     * @return Created GridFS file
     */
    public GridFsResource saveFileForProject(String projectId, InputStream content,
                                             String filename, String contentType, User user) {
        Project project = null;
        if (!StringUtils.isEmpty(projectId)) {
            project = projectRepository.findById(projectId).
                    orElseThrow(() -> EntityNotFoundException.createWithProjectId(projectId));
        }

        GridFsResource gridFSFile = fileRepository.store(content, filename, contentType, user, project == null);
        if (project != null) {
            project.getFileIds().add(gridFSFile.getId().toString());
            projectRepository.save(project);
        }
        return gridFSFile;
    }

    /**
     * Creates new file for the experiment.
     *
     * @param experimentId Experiment's identifier
     * @param content      Input stream of bytes for file
     * @param filename     File's name
     * @param contentType  Content type of file
     * @param user         User
     * @return Created GridFS file
     */
    public GridFsResource saveFileForExperiment(String experimentId, InputStream content,
                                            String filename, String contentType, User user) {
        Experiment experiment = experimentRepository.findById(experimentId).
                orElseThrow(() -> EntityNotFoundException.createWithExperimentId(experimentId));

        GridFsResource gridFSFile = fileRepository.store(content, filename, contentType, user, false);
        experiment.getFileIds().add(gridFSFile.getId().toString());
        experimentRepository.save(experiment);

        return gridFSFile;
    }

    /**
     * Removes project file.
     *
     * @param id Project's identifier
     */
    public void deleteProjectFile(String id) {
        Project project = projectRepository.findByFileId(id);
        if (project != null) {
            project.getFileIds().remove(id);
            projectRepository.save(project);
        }
        fileRepository.delete(id);
    }

    /**
     * Removes experiment file.
     *
     * @param id Experiment file's identifier
     */
    public void deleteExperimentFile(String id) {
        Experiment experiment = experimentRepository.findByFileId(id);
        if (experiment == null) {
            throw EntityNotFoundException.createWithExperimentFileId(id);
        }

        experiment.getFileIds().remove(id);
        experimentRepository.save(experiment);
        fileRepository.delete(id);
    }
}
