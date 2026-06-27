package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.reaction.util.ExperimentObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInfo;

public abstract class MutationsTestBase extends ELNBaseTest {

    protected NotebookDetailsDTO notebook;
    protected ExperimentObject experiment;

    @BeforeAll
    void beforeAll(TestInfo testInfo) {
        withUser(JOHN_USERNAME, () -> {
            ProjectDetailsDTO project = projectClient.createProject(new ProjectRequest(testInfo.getDisplayName()));
            notebook = notebookClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
        });
    }

    protected void initExperiment(String projectName) {
        ProjectDetailsDTO project = getOrCreateProject(projectName);
        notebook = notebookClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
        experiment = createExperiment(notebook, new ExperimentRequest(emptyTemplateID));
    }
}
