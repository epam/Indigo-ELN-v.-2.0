package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.entity.*;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.eln.repository.NotebookRepository;
import com.epam.indigoeln.eln.repository.ProjectRepository;
import com.epam.indigoeln.eln.util.PatchUtil;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import org.jspecify.annotations.Nullable;

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
    @Inject
    ObjectMapper objectMapper;

    @SneakyThrows
    public ProjectRevisionEntity addRevision(ProjectEntity project, Integer revisionNo, ZonedDateTime datetime, String summary, Mutation mutation, @Nullable Mutation reverseMutation, JsonNode diff) {
        ProjectRevisionEntity revision = new ProjectRevisionEntity();
        revision.setProject(project);
        doAddRevision(revision, revisionNo, datetime, summary, mutation, reverseMutation, objectMapper.writeValueAsString(diff));
        project.setRevision(revisionNo);
        project.getRevisions().add(revision);
        return revision;
    }

    @SneakyThrows
    public NotebookRevisionEntity addRevision(NotebookEntity notebook, Integer revisionNo, ZonedDateTime datetime, String summary, Mutation mutation, @Nullable Mutation reverseMutation, JsonNode diff) {
        NotebookRevisionEntity revision = new NotebookRevisionEntity();
        revision.setNotebook(notebook);
        doAddRevision(revision, revisionNo, datetime, summary, mutation, reverseMutation, objectMapper.writeValueAsString(diff));
        notebook.setRevision(revisionNo);
        notebook.getRevisions().add(revision);
        return revision;
    }

    @SneakyThrows
    public ExperimentRevisionEntity addRevision(ExperimentEntity experiment, Integer revisionNo, ZonedDateTime datetime, String summary, Mutation mutation, @Nullable Mutation reverseMutation, JsonNode diff) {
        ExperimentRevisionEntity revision = new ExperimentRevisionEntity();
        revision.setExperiment(experiment);
        doAddRevision(revision, revisionNo, datetime, summary, mutation, reverseMutation, objectMapper.writeValueAsString(diff));
        experiment.setRevision(revisionNo);
        experiment.getRevisions().add(revision);
        return revision;
    }

    private void doAddRevision(BaseRevisionEntity revision, Integer revisionNo, ZonedDateTime datetime, String summary, Mutation mutation, @Nullable Mutation reverseMutation, String diff) {
        revision.setRevision(revisionNo);
        revision.setUser(userService.getCurrentUserEntity());
        revision.setDatetime(datetime);
        revision.setSummary(summary);
        revision.setMutation(mutation);
        revision.setReverseMutation(reverseMutation);
        revision.setDiff(diff);
    }

    @SneakyThrows
    public JsonNode getPatch(BaseRevisionEntity revision) {
        return objectMapper.readTree(revision.getDiff());
    }

    private String doWritePatch(ObjectWriter patchWriter, Object patch) {
        try {
            return patchWriter.writeValueAsString(patch);
        } catch (Exception e) {
            throw new RuntimeException("Cannot write patch: " + e.getMessage(), e);
        }
    }

    public String formatPatch(String diff) {
        try {
            return PatchUtil.formatJSONDiff(objectMapper.readTree(diff));
        } catch (Exception e) {
            throw new RuntimeException("Cannot read patch: " + e.getMessage(), e);
        }
    }
}
