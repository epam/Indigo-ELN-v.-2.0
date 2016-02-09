package com.epam.indigoeln.core.service.file;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.repository.file.FileRepository;
import com.epam.indigoeln.core.repository.project.ProjectRepository;
import com.epam.indigoeln.core.service.EntityNotFoundException;
import com.epam.indigoeln.core.service.FileNotFoundException;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class FileService {

    @Autowired
    FileRepository fileRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    ExperimentRepository experimentRepository;

    public Page<GridFSDBFile> getAllFilesByProjectId(Long projectSequenceId, Pageable pageable) {
        Project project = projectRepository.findOneBySequenceId(projectSequenceId).
                orElseThrow(() -> EntityNotFoundException.createWithProjectId(projectSequenceId.toString()));
        return fileRepository.findAll(project.getFileIds(), pageable);
    }

    public Page<GridFSDBFile> getAllFilesByExperimentId(Long experimentSequenceId, Pageable pageable) {
        Experiment experiment = experimentRepository.findOneBySequenceId(experimentSequenceId).
                orElseThrow(() -> EntityNotFoundException.createWithExperimentId(experimentSequenceId.toString()));
        return fileRepository.findAll(experiment.getFileIds(), pageable);
    }

    public GridFSDBFile getFileById(String id) {
        GridFSDBFile gridFSDBFile = fileRepository.findOneById(id);
        if (gridFSDBFile == null) {
            throw new FileNotFoundException(id);
        }
        return gridFSDBFile;
    }

    public GridFSFile saveFileForProject(Long projectSequenceId, InputStream content,
                                         String filename, String contentType, User user) {
        Project project = projectRepository.findOneBySequenceId(projectSequenceId).
                orElseThrow(() -> EntityNotFoundException.createWithProjectId(projectSequenceId.toString()));

        GridFSFile gridFSFile = fileRepository.store(content, filename, contentType, user);
        project.getFileIds().add(gridFSFile.getId().toString());
        projectRepository.save(project);

        return gridFSFile;
    }

    public GridFSFile saveFileForExperiment(Long experimentSequenceId, InputStream content,
                                            String filename, String contentType, User user) {
        Experiment experiment = experimentRepository.findOneBySequenceId(experimentSequenceId).
                orElseThrow(() -> EntityNotFoundException.createWithExperimentId(experimentSequenceId.toString()));

        GridFSFile gridFSFile = fileRepository.store(content, filename, contentType, user);
        experiment.getFileIds().add(gridFSFile.getId().toString());
        experimentRepository.save(experiment);

        return gridFSFile;
    }

    public void deleteProjectFile(String id) {
        Project project = projectRepository.findByFileId(id);
        if (project == null) {
            throw EntityNotFoundException.createWithProjectFileId(id);
        }

        project.getFileIds().remove(id);
        projectRepository.save(project);
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