package com.epam.indigoeln.core.service.file;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.repository.file.FileRepository;
import com.epam.indigoeln.core.repository.project.ProjectRepository;
import com.epam.indigoeln.core.service.exception.EntityNotFoundException;
import com.epam.indigoeln.core.service.exception.FileNotFoundException;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Optional;

@Service
public class FileService {

    @Autowired
    FileRepository fileRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    ExperimentRepository experimentRepository;

    public Page<GridFSDBFile> getAllFilesByProjectId(String projectId, Pageable pageable) {
        Project project = Optional.ofNullable(projectRepository.findOne(projectId)).
                orElseThrow(() -> EntityNotFoundException.createWithProjectId(projectId));
        return fileRepository.findAll(project.getFileIds(), pageable);
    }

    public Page<GridFSDBFile> getAllFilesByExperimentId(String experimentId, Pageable pageable) {
        Experiment experiment = Optional.ofNullable(experimentRepository.findOne(experimentId)).
                orElseThrow(() -> EntityNotFoundException.createWithExperimentId(experimentId));
        return fileRepository.findAll(experiment.getFileIds(), pageable);
    }

    public GridFSDBFile getFileById(String id) {
        GridFSDBFile gridFSDBFile = fileRepository.findOneById(id);
        if (gridFSDBFile == null) {
            throw new FileNotFoundException(id);
        }
        return gridFSDBFile;
    }

    public GridFSFile saveFileForProject(String projectId, InputStream content,
                                         String filename, String contentType, User user) {
        Project project = null;
        if (!StringUtils.isEmpty(projectId)) {
            project = Optional.ofNullable(projectRepository.findOne(projectId)).
                    orElseThrow(() -> EntityNotFoundException.createWithProjectId(projectId));
        }

        GridFSFile gridFSFile = fileRepository.store(content, filename, contentType, user);
        if (project != null) {
            project.getFileIds().add(gridFSFile.getId().toString());
            projectRepository.save(project);
        }
        return gridFSFile;
    }

    public GridFSFile saveFileForExperiment(String experimentId, InputStream content,
                                            String filename, String contentType, User user) {
        Experiment experiment = Optional.ofNullable(experimentRepository.findOne(experimentId)).
                orElseThrow(() -> EntityNotFoundException.createWithExperimentId(experimentId));

        GridFSFile gridFSFile = fileRepository.store(content, filename, contentType, user);
        experiment.getFileIds().add(gridFSFile.getId().toString());
        experimentRepository.save(experiment);

        return gridFSFile;
    }

    public void deleteProjectFile(String id) {
        Project project = projectRepository.findByFileId(id);
        if (project != null) {
            project.getFileIds().remove(id);
            projectRepository.save(project);
        }
        fileRepository.delete(id);
    }

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