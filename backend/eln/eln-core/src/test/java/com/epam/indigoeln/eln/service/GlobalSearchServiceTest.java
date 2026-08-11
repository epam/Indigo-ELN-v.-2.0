package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.compound.model.search.NumericSearch;
import com.epam.indigoeln.compound.model.search.StructuralSearch;
import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.reaction.model.ReactionRole;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputSampleMutation;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.epam.indigoeln.reaction.util.ExperimentObject;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static com.epam.indigoeln.common.util.ModelUtil.loadResourceAsString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;


@QuarkusTest
@TestSecurity(user = ELNBaseTest.MAGGIE_USERNAME)
class GlobalSearchServiceTest extends ELNBaseTest {

    TherapeuticAreaRef therapeuticArea1;
    TherapeuticAreaRef therapeuticArea2;
    ProjectCodeRef projectCode1;
    ProjectCodeRef projectCode2;

    ProjectDetailsDTO project1;
    ProjectDetailsDTO project2;
    ProjectDetailsDTO project3;
    NotebookDetailsDTO notebook1;
    NotebookDetailsDTO notebook2;
    NotebookDetailsDTO notebook3;
    ExperimentObject experiment1;
    ExperimentObject experiment2;
    ExperimentObject experiment3;

    @BeforeAll
    void setUp() {
        withUser(MAGGIE_USERNAME, () -> {
            List<TherapeuticAreaRef> therapeuticAreas = dictionaryClient.getDictionary(BuiltInDictionary.THERAPEUTIC_AREA);
            therapeuticArea1 = therapeuticAreas.get(0);
            therapeuticArea2 = therapeuticAreas.get(1);
            List<ProjectCodeRef> projectCodes = dictionaryClient.getDictionary(BuiltInDictionary.PROJECT_CODE);
            projectCode1 = projectCodes.get(0);
            projectCode2 = projectCodes.get(1);
            project1 = projectClient.createProject(new ProjectRequest("p1", List.of("k1", "k2"), "l1 xx", "pd1"));
            project2 = projectClient.createProject(new ProjectRequest("p2", List.of("k2", "k3"), "l2 xx", "pd2"));
            notebook1 = notebookClient.createNotebook(project1.getId(), new NotebookRequest("00000001", "nd1 xx"));
            notebook2 = notebookClient.createNotebook(project2.getId(), new NotebookRequest("00000002", "nd2 xx"));
            experiment1 = createExperiment(notebook1, new ExperimentRequest(emptyTemplateID, "ed1 xx", therapeuticArea1, projectCode1));
            experiment2 = createExperiment(notebook2, new ExperimentRequest(emptyTemplateID, "ed2 xx", therapeuticArea2, projectCode2));
            experiment2.mutateSetSchemeFromResource("/reaction.rxn");
            experiment2.mutateAddProductSample(1);
            experiment2.mutate(new ReactionOutputSampleMutation.SetOutputPurity(experiment2.outputSample(1, 1).getAnchor(), "30"));
            experiment2.mutateSetInputWeight(1, 1, "10.0", WeightUnit.G);
            experiment2.mutate(new ReactionOutputSampleMutation.SetOutputActualWeight(experiment2.outputSample(1, 1).getAnchor(), "5.0", WeightUnit.G));
        });
        withUser(BART_USERNAME, () -> {
            project3 = projectClient.createProject(new ProjectRequest("p3"));
            projectClient.updateProjectAccess(project3.getId(), AccessForm.of(MAGGIE_USERNAME, AccessLevel.VIEW));
            notebook3 = notebookClient.createNotebook(project3.getId(), new NotebookRequest("00000003", null));
            experiment3 = createExperiment(notebook3, new ExperimentRequest(emptyTemplateID));
            experimentClient.cancelExperiment(experiment3.id());
        });
    }

    @Test
    void testFindProjects() {
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(new GlobalSearchRequest().withQuery("p1"), Paging.DEFAULT);
        assertResults(results, tuple(ELNEntityType.PROJECT, "p1", project1.getId()));
    }

    @Test
    void testFindNotebooks() {
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(new GlobalSearchRequest().withQuery("nd2"), Paging.DEFAULT);
        assertResults(results, tuple(ELNEntityType.NOTEBOOK, "00000002", notebook2.getId()));
    }

    @Test
    void testFindExperiments() {
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(new GlobalSearchRequest().withQuery("ed1"), Paging.DEFAULT);
        assertResults(results, tuple(ELNEntityType.EXPERIMENT, experiment1.name(), experiment1.id()));
        assertThat(results.getItems().getFirst().getExperimentStatus()).isEqualTo(experiment1.status());
    }

    @Test
    void testFindAll() {
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(new GlobalSearchRequest().withQuery("xx"), Paging.DEFAULT);
        assertResults(results
                , tuple(ELNEntityType.PROJECT, project1.getName(), project1.getId())
                , tuple(ELNEntityType.PROJECT, project2.getName(), project2.getId())
                , tuple(ELNEntityType.NOTEBOOK, notebook1.getName(), notebook1.getId())
                , tuple(ELNEntityType.NOTEBOOK, notebook2.getName(), notebook2.getId())
                , tuple(ELNEntityType.EXPERIMENT, experiment1.name(), experiment1.id())
                , tuple(ELNEntityType.EXPERIMENT, experiment2.name(), experiment2.id())
        );
        assertThat(results.getItems()).map(GlobalSearchResultDTO::getFragment, GlobalSearchResultDTO::getCreatedBy).containsExactly(
                tuple(project1.getDescription(), MAGGIE_USER_REF),
                tuple(project2.getDescription(), MAGGIE_USER_REF),
                tuple("nd1 <mark>xx</mark>", MAGGIE_USER_REF),
                tuple("nd2 <mark>xx</mark>", MAGGIE_USER_REF),
                tuple("ed1 <mark>xx</mark>", MAGGIE_USER_REF),
                tuple("ed2 <mark>xx</mark>", MAGGIE_USER_REF)
        );
    }

    @Test
    void testFindExperimentsByTherapeuticArea() {
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(new GlobalSearchRequest().withTherapeuticArea(therapeuticArea1), Paging.DEFAULT);
        assertResults(results, tuple(ELNEntityType.EXPERIMENT, experiment1.name(), experiment1.id()));
    }

    @Test
    void testFindExperimentsByProjectCode() {
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(new GlobalSearchRequest().withProjectCode(projectCode2), Paging.DEFAULT);
        assertResults(results, tuple(ELNEntityType.EXPERIMENT, experiment2.name(), experiment2.id()));
    }

    @Test
    void testFindExperimentsByStatus() {
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(new GlobalSearchRequest().withExperimentStatus(Set.of(ExperimentStatus.CANCELLED, ExperimentStatus.SUBMITTED)), Paging.DEFAULT);
        assertResults(results
                , tuple(ELNEntityType.EXPERIMENT, experiment3.name(), experiment3.id())
        );
    }

    @Test
    void testFindAllByAuthor() {
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(new GlobalSearchRequest().withAuthor(Set.of(BART_USER_REF)), Paging.DEFAULT);
        assertResults(results
                , tuple(ELNEntityType.PROJECT, project3.getName(), project3.getId())
                , tuple(ELNEntityType.NOTEBOOK, notebook3.getName(), notebook3.getId())
                , tuple(ELNEntityType.EXPERIMENT, experiment3.name(), experiment3.id())
        );
    }

    @Test
    void testFindByMoleculeSubstructure() {
        String molFile = loadResourceAsString("/ring-substructure.mol");
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(
                new GlobalSearchRequest().withMoleculeStructure(new StructuralSearch(StructuralSearch.Type.SUBSTRUCTURE, molFile)),
                Paging.DEFAULT
        );
        assertResults(results, tuple(ELNEntityType.EXPERIMENT, experiment2.name(), experiment2.id()));
        assertThat(results.getItems().getFirst().getReactionRoles()).isEqualTo(Set.of(ReactionRole.REACTANT, ReactionRole.OUTPUT));
    }

    @Test
    void testFindByMoleculeSubstructureAndRole() {
        String molFile = loadResourceAsString("/ring-substructure.mol");
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(
                new GlobalSearchRequest().withMoleculeStructure(new StructuralSearch(StructuralSearch.Type.SUBSTRUCTURE, molFile)).withReactionRole(ReactionRole.REACTANT),
                Paging.DEFAULT
        );
        assertResults(results, tuple(ELNEntityType.EXPERIMENT, experiment2.name(), experiment2.id()));
        assertThat(results.getItems().getFirst().getReactionRoles()).isEqualTo(Set.of(ReactionRole.REACTANT));
    }

    @Test
    void testFindByMoleculeSubstructureAndRoleNotFound() {
        String molFile = loadResourceAsString("/ring-substructure.mol");
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(
                new GlobalSearchRequest().withMoleculeStructure(new StructuralSearch(StructuralSearch.Type.SUBSTRUCTURE, molFile)).withReactionRole(ReactionRole.SOLVENT),
                Paging.DEFAULT
        );
        assertResults(results);
    }

    // TODO test for EXACT and SIMILARITY molfile search types

    @Test
    void testFindByReactionSubstructure() {
        String rxnFile = loadResourceAsString("/reaction-substructure.rxn");
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(
                new GlobalSearchRequest().withReactionStructure(new StructuralSearch(StructuralSearch.Type.SUBSTRUCTURE, rxnFile)),
                Paging.DEFAULT
        );
        assertResults(results, tuple(ELNEntityType.EXPERIMENT, experiment2.name(), experiment2.id()));
    }

    // TODO test for EXACT rxnfile

    @Test
    void testBatchPurity() {
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(
                new GlobalSearchRequest().withBatchPurity(new NumericSearch.GreaterThanOrEqual(10.0)),
                Paging.DEFAULT
        );
        assertResults(results, tuple(ELNEntityType.EXPERIMENT, experiment2.name(), experiment2.id()));
    }

    @Test
    void testBatchYield() {
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(
                new GlobalSearchRequest().withBatchYield(new NumericSearch.GreaterThanOrEqual(0.1)),
                Paging.DEFAULT
        );
        assertResults(results, tuple(ELNEntityType.EXPERIMENT, experiment2.name(), experiment2.id()));
    }

    @Test
    void testFindAllEntitiesQuickSearchAndAuthor() {
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(new GlobalSearchRequest().withQuery("xx").withAuthor(Set.of(MAGGIE_USER_REF)), Paging.DEFAULT);
        assertResults(results
                , tuple(ELNEntityType.PROJECT, project1.getName(), project1.getId())
                , tuple(ELNEntityType.PROJECT, project2.getName(), project2.getId())
                , tuple(ELNEntityType.NOTEBOOK, notebook1.getName(), notebook1.getId())
                , tuple(ELNEntityType.NOTEBOOK, notebook2.getName(), notebook2.getId())
                , tuple(ELNEntityType.EXPERIMENT, experiment1.name(), experiment1.id())
                , tuple(ELNEntityType.EXPERIMENT, experiment2.name(), experiment2.id())
        );
    }

    @Test
    void testFindByAllAttributes() {
        String molFile = loadResourceAsString("/ring-substructure.mol");
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(new GlobalSearchRequest()
                .withQuery("xx")
                .withTherapeuticArea(therapeuticArea2)
                .withProjectCode(projectCode2)
                .withExperimentStatus(Set.of(ExperimentStatus.OPEN))
                .withAuthor(Set.of(MAGGIE_USER_REF))
                .withBatchYield(new NumericSearch.GreaterThanOrEqual(0.1))
                .withBatchPurity(new NumericSearch.GreaterThanOrEqual(10.0))
                .withMoleculeStructure(new StructuralSearch(StructuralSearch.Type.SUBSTRUCTURE, molFile))
                , Paging.DEFAULT);
        assertResults(results, tuple(ELNEntityType.EXPERIMENT, experiment2.name(), experiment2.id()));
    }

    private void assertResults(Page<GlobalSearchResultDTO> results, Tuple... expected) {
        if (expected.length == 0) {
            assertThat(results.getItems()).isEmpty();
            return;
        }
        List<Function<GlobalSearchResultDTO, ?>> extractors = new ArrayList<>(List.of(GlobalSearchResultDTO::getType, GlobalSearchResultDTO::getName, GlobalSearchResultDTO::getId));
        //noinspection unchecked,RedundantCast
        assertThat(results.getItems()).map(extractors.toArray(Function[]::new)).containsOnly((Object[]) expected);
    }
}
