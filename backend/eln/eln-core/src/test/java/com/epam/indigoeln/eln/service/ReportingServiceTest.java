package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.eln.BaseTest;
import com.epam.indigoeln.eln.api.MutateModelForm;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.NotebookEntity;
import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.util.ResponseWithHeaders;
import com.epam.indigoeln.eln.util.TestHelper;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.JwtSecurity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import static com.epam.indigoeln.common.util.ModelUtil.loadResource;


@QuarkusTest
@JwtSecurity
@TestSecurity(user = TestHelper.JOHN_USERNAME)
public class ReportingServiceTest extends BaseTest {

    List<DictionaryItemRef> therapeuticAreas;
    List<DictionaryItemRef> projectCodes;

    @BeforeEach
    void setUp() {
        therapeuticAreas = dictionaryClient.getDictionary(Dictionary.THERAPEUTIC_AREA);
        projectCodes = dictionaryClient.getDictionary(Dictionary.PROJECT_CODE);
    }

    @SuppressWarnings("unused")
    public static List<ExperimentEntity> fillExperimentDataForJasperReportsStudio() {
        ExperimentEntity experiment = new ExperimentEntity();
        NotebookEntity notebook = new NotebookEntity();
        ProjectEntity project = new ProjectEntity();
        experiment.setNotebook(notebook);
        notebook.setProject(project);
        experiment.setProject(project);

        project.setName("Demo project");
        experiment.setName("00000111-0018");
        experiment.setCreatedBy(new UserEntity("test", "Test", "User", "Test User", Set.of()));
        experiment.setStatus(ExperimentStatus.OPEN);
        // Reaction Details, Experiment Subject / Title = O-acetylation of salicylic acid
        experiment.setCreatedAt(ZonedDateTime.parse("2025-07-29T13:22:05Z"));
        // Reaction Details, Therapeutic Area = Diabetes
        // other Reaction Details fields?
        experiment.setPicture(ModelUtil.loadResource(ReportingServiceTest.class, "/experiment-image.svg"));
        return List.of(experiment);
    }

    @Test
    void testReport() throws Exception {
        ProjectDetailsDTO project = projectClient.createProject(new ProjectRequest("ReportingServiceTest"));
        NotebookDetailsDTO notebook = notebookClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
        ExperimentDetailsDTO experiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(getEmptyTemplateID()));
        String rxnFile = new String(loadResource(getClass(), "/reaction.rxn"));
        ExperimentModel experimentModel = experimentClient.getExperimentModel(experiment.getId());
        experimentClient.mutateExperimentModel(experiment.getId(), new MutateModelForm(experimentModel, new ReactionMutation.SetScheme(experimentModel.getReactions().getFirst().getAnchor(), rxnFile)));

        ResponseWithHeaders response = experimentClient.printReportClient(experiment.getId());
        Files.write(Paths.get("report.pdf"), response.getContent().readAllBytes());
    }
}
