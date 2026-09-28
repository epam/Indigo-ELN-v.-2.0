package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.model.ExperimentRequest;
import com.epam.indigoeln.eln.model.NotebookDetailsDTO;
import com.epam.indigoeln.eln.model.ProjectDetailsDTO;
import com.epam.indigoeln.eln.model.ProjectRequest;
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
            notebook = createNotebook(project.getId());
        });
    }

    protected void initExperiment(String projectName) {
        ProjectDetailsDTO project = getOrCreateProject(projectName);
        notebook = createNotebook(project.getId());
        experiment = createExperiment(notebook, new ExperimentRequest(emptyTemplateID));
    }
}
