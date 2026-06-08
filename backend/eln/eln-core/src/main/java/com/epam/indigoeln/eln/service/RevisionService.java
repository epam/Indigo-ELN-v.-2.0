package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.entity.*;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.SneakyThrows;

import java.time.ZonedDateTime;

@ApplicationScoped
public class RevisionService {

    @Inject
    UserService userService;
    @Inject
    ObjectMapper objectMapper;

    @SneakyThrows
    public ProjectRevisionEntity addRevision(ProjectEntity project, Integer revisionNo, ZonedDateTime datetime, String summary, Mutation mutation, JsonNode diff) {
        ProjectRevisionEntity revision = new ProjectRevisionEntity();
        revision.setProject(project);
        doAddRevision(revision, revisionNo, datetime, summary, mutation, diff);
        project.setRevision(revisionNo);
        return revision;
    }

    @SneakyThrows
    public NotebookRevisionEntity addRevision(NotebookEntity notebook, Integer revisionNo, ZonedDateTime datetime, String summary, Mutation mutation, JsonNode diff) {
        NotebookRevisionEntity revision = new NotebookRevisionEntity();
        revision.setNotebook(notebook);
        doAddRevision(revision, revisionNo, datetime, summary, mutation, diff);
        notebook.setRevision(revisionNo);
        return revision;
    }

    @SneakyThrows
    public ExperimentRevisionEntity addRevision(ExperimentEntity experiment, Integer revisionNo, ZonedDateTime datetime, String summary, Mutation mutation, JsonNode diff) {
        ExperimentRevisionEntity revision = new ExperimentRevisionEntity();
        revision.setExperiment(experiment);
        doAddRevision(revision, revisionNo, datetime, summary, mutation, diff);
        experiment.setRevision(revisionNo);
        return revision;
    }

    private void doAddRevision(BaseRevisionEntity revision, Integer revisionNo, ZonedDateTime datetime, String summary, Mutation mutation, JsonNode diff) {
        revision.setRevision(revisionNo);
        revision.setUser(userService.getCurrentUserEntity());
        revision.setDatetime(datetime);
        revision.setSummary(summary);
        revision.setMutation(mutation);
        revision.setDiff(diff);
    }
}
