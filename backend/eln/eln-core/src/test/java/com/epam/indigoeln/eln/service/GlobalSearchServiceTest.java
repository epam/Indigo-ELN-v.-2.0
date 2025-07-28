package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.BaseTest;
import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.util.TestHelper;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.JwtSecurity;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.epam.indigoeln.eln.util.CustomAssertions.assertThatClientCall;
import static org.assertj.core.api.Assertions.*;


@QuarkusTest
@JwtSecurity
@TestSecurity(user = TestHelper.MAGGIE_USERNAME)
class GlobalSearchServiceTest extends BaseTest {

    DictionaryItemRef therapeuticArea1;
    DictionaryItemRef therapeuticArea2;
    DictionaryItemRef projectCode1;
    DictionaryItemRef projectCode2;

    ProjectDetailsDTO project1;
    ProjectDetailsDTO project2;
    ProjectDetailsDTO project3;
    NotebookDetailsDTO notebook1;
    NotebookDetailsDTO notebook2;
    NotebookDetailsDTO notebook3;
    ExperimentDetailsDTO experiment1;
    ExperimentDetailsDTO experiment2;
    ExperimentDetailsDTO experiment3;

    @BeforeAll
    void setUp() {
        withUser(TestHelper.MAGGIE_USERNAME, () -> {
            List<DictionaryItemRef> therapeuticAreas = dictionaryClient.getDictionary(Dictionary.THERAPEUTIC_AREA);
            therapeuticArea1 = therapeuticAreas.get(0);
            therapeuticArea2 = therapeuticAreas.get(1);
            List<DictionaryItemRef> projectCodes = dictionaryClient.getDictionary(Dictionary.PROJECT_CODE);
            projectCode1 = projectCodes.get(0);
            projectCode2 = projectCodes.get(1);
            project1 = projectClient.createProject(new ProjectRequest("p1", List.of("k1", "k2"), "l1 xx", "pd1"));
            project2 = projectClient.createProject(new ProjectRequest("p2", List.of("k2", "k3"), "l2 xx", "pd2"));
            notebook1 = notebookClient.createNotebook(project1.getId(), new NotebookRequest("00000001", "nd1 xx"));
            notebook2 = notebookClient.createNotebook(project2.getId(), new NotebookRequest("00000002", "nd2 xx"));
            experiment1 = experimentClient.createExperiment(notebook1.getId(), new ExperimentRequest(getEmptyTemplateID(), "ed1 xx", therapeuticArea1, projectCode1));
            experiment2 = experimentClient.createExperiment(notebook2.getId(), new ExperimentRequest(getEmptyTemplateID(), "ed2 xx", therapeuticArea2, projectCode2));
        });
        withUser(TestHelper.BART_USERNAME, () -> {
            project3 = projectClient.createProject(new ProjectRequest("p3"));
            projectClient.updateProjectAccess(project3.getId(), AccessForm.of(testHelper.getMaggieUserID(), AccessLevel.VIEW));
            notebook3 = notebookClient.createNotebook(project3.getId(), new NotebookRequest("00000003", null));
            experiment3 = experimentClient.createExperiment(notebook3.getId(), new ExperimentRequest(getEmptyTemplateID(), null, null, null));
        });
    }

    @Test
    void testFindProjects() {
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(GlobalSearchRequest.builder().query("p1").build(), Paging.DEFAULT);
        assertResults(results, tuple(EntityType.PROJECT, "p1", project1.getId()));
    }

    @Test
    void testFindNotebooks() {
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(GlobalSearchRequest.builder().query("nd2").build(), Paging.DEFAULT);
        assertResults(results, tuple(EntityType.NOTEBOOK, "00000002", notebook2.getId()));
    }

    @Test
    void testFindExperiments() {
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(GlobalSearchRequest.builder().query("ed1").build(), Paging.DEFAULT);
        assertResults(results, tuple(EntityType.EXPERIMENT, experiment1.getName(), experiment1.getId()));
    }

    @Test
    void testFindAll() {
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(GlobalSearchRequest.builder().query("xx").build(), Paging.DEFAULT);
        assertResults(results
                , tuple(EntityType.PROJECT, project1.getName(), project1.getId())
                , tuple(EntityType.PROJECT, project2.getName(), project2.getId())
                , tuple(EntityType.NOTEBOOK, notebook1.getName(), notebook1.getId())
                , tuple(EntityType.NOTEBOOK, notebook2.getName(), notebook2.getId())
                , tuple(EntityType.EXPERIMENT, experiment1.getName(), experiment1.getId())
                , tuple(EntityType.EXPERIMENT, experiment2.getName(), experiment2.getId())
        );
    }

    @Test
    void testFindExperimentsByTherapeuticArea() {
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(GlobalSearchRequest.builder().therapeuticArea(therapeuticArea1).build(), Paging.DEFAULT);
        assertResults(results, tuple(EntityType.EXPERIMENT, experiment1.getName(), experiment1.getId()));
    }

    @Test
    void testFindExperimentsByProjectCode() {
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(GlobalSearchRequest.builder().projectCode(projectCode2).build(), Paging.DEFAULT);
        assertResults(results, tuple(EntityType.EXPERIMENT, experiment2.getName(), experiment2.getId()));
    }

    @Test
    void testFindExperimentsByStatus() {
        // TODO move one experiment to another status
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(GlobalSearchRequest.builder().experimentStatus(ExperimentStatus.OPEN).build(), Paging.DEFAULT);
        System.out.println(results);
        assertResults(results
                , tuple(EntityType.EXPERIMENT, experiment1.getName(), experiment1.getId())
                , tuple(EntityType.EXPERIMENT, experiment2.getName(), experiment2.getId())
                , tuple(EntityType.EXPERIMENT, experiment3.getName(), experiment3.getId())
        );
    }

    @Test
    void testFindAllByAuthor() {
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(GlobalSearchRequest.builder().author(new UserRef(testHelper.getBartUserID(), TestHelper.BART_DISPLAY_NAME)).build(), Paging.DEFAULT);
        assertResults(results
                , tuple(EntityType.PROJECT, project3.getName(), project3.getId())
                , tuple(EntityType.NOTEBOOK, notebook3.getName(), notebook3.getId())
                , tuple(EntityType.EXPERIMENT, experiment3.getName(), experiment3.getId())
        );
    }

    private void assertResults(Page<GlobalSearchResultDTO> results, Tuple... expected) {
        assertThat(results.getItems()).map(GlobalSearchResultDTO::getType, GlobalSearchResultDTO::getName, GlobalSearchResultDTO::getId).containsOnly(expected);
    }
}
