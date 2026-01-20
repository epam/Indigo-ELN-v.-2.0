package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.entity.*;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.eln.repository.NotebookRepository;
import com.epam.indigoeln.eln.repository.ProjectRepository;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import com.epam.indigoeln.reaction.model.patch.ExperimentPatch;
import com.epam.indigoeln.reaction.model.patch.NotebookPatch;
import com.epam.indigoeln.reaction.model.patch.ProjectPatch;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.ZonedDateTime;

@ApplicationScoped
public class RevisionService {

    @Inject
    UserService userService;
    @Inject
    ProjectRepository projectRepository;
    @Inject
    NotebookRepository notebookRepository;
    @Inject
    ExperimentRepository experimentRepository;
    
    ObjectReader projectPatchReader;
    ObjectWriter projectPatchWriter;
    ObjectReader notebookPatchReader;
    ObjectWriter notebookPatchWriter;
    ObjectReader experimentPatchReader;
    ObjectWriter experimentPatchWriter;

    RevisionService(ObjectMapper objectMapper) {
        projectPatchReader = objectMapper.readerFor(ProjectPatch.class);
        projectPatchWriter = objectMapper.writerFor(ProjectPatch.class);
        notebookPatchReader = objectMapper.readerFor(NotebookPatch.class);
        notebookPatchWriter = objectMapper.writerFor(NotebookPatch.class);
        experimentPatchReader = objectMapper.readerFor(ExperimentPatch.class);
        experimentPatchWriter = objectMapper.writerFor(ExperimentPatch.class);
    }

    public void addRevision(ProjectEntity project, Integer revisionNo, ZonedDateTime datetime, String summary, Mutation mutation, @org.jspecify.annotations.Nullable MutationRedoInfo redoInfo, @org.jspecify.annotations.Nullable Mutation reverseMutation, ProjectPatch diff) {
        ProjectRevisionEntity revision = new ProjectRevisionEntity();
        revision.setProject(project);
        doAddRevision(revision, revisionNo, datetime, summary, mutation, redoInfo, reverseMutation, doWritePatch(projectPatchWriter, diff));
        project.setRevision(revisionNo);
        project.getRevisions().add(revision);
        projectRepository.persistRevision(revision);
    }

    public void addRevision(NotebookEntity notebook, Integer revisionNo, ZonedDateTime datetime, String summary, Mutation mutation, @org.jspecify.annotations.Nullable MutationRedoInfo redoInfo, @org.jspecify.annotations.Nullable Mutation reverseMutation, NotebookPatch diff) {
        NotebookRevisionEntity revision = new NotebookRevisionEntity();
        revision.setNotebook(notebook);
        doAddRevision(revision, revisionNo, datetime, summary, mutation, redoInfo, reverseMutation, doWritePatch(notebookPatchWriter, diff));
        notebook.setRevision(revisionNo);
        notebook.getRevisions().add(revision);
        notebookRepository.persistRevision(revision);
    }

    public void addRevision(ExperimentEntity experiment, Integer revisionNo, ZonedDateTime datetime, String summary, Mutation mutation, @org.jspecify.annotations.Nullable MutationRedoInfo redoInfo, @org.jspecify.annotations.Nullable Mutation reverseMutation, ExperimentPatch diff) {
        ExperimentRevisionEntity revision = new ExperimentRevisionEntity();
        revision.setExperiment(experiment);
        doAddRevision(revision, revisionNo, datetime, summary, mutation, redoInfo, reverseMutation, doWritePatch(experimentPatchWriter, diff));
        experiment.setRevision(revisionNo);
        experiment.getRevisions().add(revision);
        experimentRepository.persistRevision(revision);
    }

    private void doAddRevision(BaseRevisionEntity revision, Integer revisionNo, ZonedDateTime datetime, String summary, Mutation mutation, @org.jspecify.annotations.Nullable MutationRedoInfo redoInfo, @org.jspecify.annotations.Nullable Mutation reverseMutation, String diff) {
        revision.setRevision(revisionNo);
        revision.setUser(userService.getCurrentUserEntity());
        revision.setDatetime(datetime);
        revision.setSummary(summary);
        revision.setMutation(mutation);
        revision.setRedoInfo(redoInfo);
        revision.setReverseMutation(reverseMutation);
        revision.setDiff(diff);
    }

    public ProjectPatch getPatch(ProjectRevisionEntity revision) {
        return doGetPatch(projectPatchReader, revision.getDiff());
    }

    public NotebookPatch getPatch(NotebookRevisionEntity revision) {
        return doGetPatch(notebookPatchReader, revision.getDiff());
    }

    public ExperimentPatch getPatch(ExperimentRevisionEntity revision) {
        return doGetPatch(experimentPatchReader, revision.getDiff());
    }

    private <T> T doGetPatch(ObjectReader patchReader, String revision) {
        try {
            return patchReader.readValue(revision);
        } catch (Exception e) {
            throw new RuntimeException("Cannot read patch: " + e.getMessage(), e);
        }
    }

    private String doWritePatch(ObjectWriter patchWriter, Object patch) {
        try {
            return patchWriter.writeValueAsString(patch);
        } catch (Exception e) {
            throw new RuntimeException("Cannot write patch: " + e.getMessage(), e);
        }
    }
}
