package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.compound.model.NumericSearch;
import com.epam.indigoeln.compound.model.StructuralSearch;
import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.api.MutateModelForm;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputSampleMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputSampleMutation;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.JwtSecurity;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static com.epam.indigoeln.common.util.ModelUtil.loadResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;


@QuarkusTest
@JwtSecurity
@TestSecurity(user = ELNBaseTest.MAGGIE_USERNAME)
class GlobalSearchServiceTest extends ELNBaseTest {

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
        withUser(MAGGIE_USERNAME, () -> {
            List<DictionaryItemRef> therapeuticAreas = dictionaryClient.getDictionary(BuiltInDictionary.THERAPEUTIC_AREA);
            therapeuticArea1 = therapeuticAreas.get(0);
            therapeuticArea2 = therapeuticAreas.get(1);
            List<DictionaryItemRef> projectCodes = dictionaryClient.getDictionary(BuiltInDictionary.PROJECT_CODE);
            projectCode1 = projectCodes.get(0);
            projectCode2 = projectCodes.get(1);
            project1 = projectClient.createProject(new ProjectRequest("p1", List.of("k1", "k2"), "l1 xx", "pd1"));
            project2 = projectClient.createProject(new ProjectRequest("p2", List.of("k2", "k3"), "l2 xx", "pd2"));
            notebook1 = notebookClient.createNotebook(project1.getId(), new NotebookRequest("00000001", "nd1 xx"));
            notebook2 = notebookClient.createNotebook(project2.getId(), new NotebookRequest("00000002", "nd2 xx"));
            experiment1 = experimentClient.createExperiment(notebook1.getId(), new ExperimentRequest(emptyTemplateID, "ed1 xx", therapeuticArea1, projectCode1));
            experiment2 = experimentClient.createExperiment(notebook2.getId(), new ExperimentRequest(emptyTemplateID, "ed2 xx", therapeuticArea2, projectCode2));
            String rxnFile = new String(loadResource(getClass(), "/reaction.rxn"));
            ExperimentModel experimentModel = experiment2.getModel();
            experimentModel = experimentClient.mutateExperimentModel(experiment2.getId(), new MutateModelForm(experimentModel, experimentClient.analyzeScheme(experiment2.getId(), experimentModel.getReactions().getFirst().getAnchor(), rxnFile)));
            InputSampleAnchor inputSample = experimentModel.getReactions().getFirst().getInputs().getFirst().getSamples().getFirst().getAnchor();
            OutputAnchor output = experimentModel.getReactions().getFirst().getOutputs().getFirst().getAnchor();
            experimentModel = experimentClient.mutateExperimentModel(experiment2.getId(), new MutateModelForm(experimentModel, new ReactionOutputMutation.AddProductSample(output)));
            OutputSampleAnchor outputSample = experimentModel.getReactions().getFirst().getOutputs().getFirst().getSamples().getFirst().getAnchor();
            experimentModel = experimentClient.mutateExperimentModel(experiment2.getId(), new MutateModelForm(experimentModel, new ReactionOutputSampleMutation.SetOutputPurity(outputSample, 0.3, null)));
            experimentModel = experimentClient.mutateExperimentModel(experiment2.getId(), new MutateModelForm(experimentModel, new ReactionInputSampleMutation.SetInputWeight(inputSample, 10.0, WeightUnit.G, null)));
            experimentModel = experimentClient.mutateExperimentModel(experiment2.getId(), new MutateModelForm(experimentModel, new ReactionOutputSampleMutation.SetOutputActualWeight(outputSample, 5.0, WeightUnit.G, null)));
            System.out.println(experimentModel);
        });
        withUser(BART_USERNAME, () -> {
            project3 = projectClient.createProject(new ProjectRequest("p3"));
            projectClient.updateProjectAccess(project3.getId(), AccessForm.of(maggieUserID, AccessLevel.VIEW));
            notebook3 = notebookClient.createNotebook(project3.getId(), new NotebookRequest("00000003", null));
            experiment3 = experimentClient.createExperiment(notebook3.getId(), new ExperimentRequest(emptyTemplateID, null, null, null));
            experimentClient.cancelExperiment(experiment3.getId());
        });
    }

    @Test
    void testFindProjects() {
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(new GlobalSearchRequest().withQuery("p1"), Paging.DEFAULT);
        assertResults(results, tuple(EntityType.PROJECT, "p1", project1.getId()));
    }

    @Test
    void testFindNotebooks() {
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(new GlobalSearchRequest().withQuery("nd2"), Paging.DEFAULT);
        assertResults(results, tuple(EntityType.NOTEBOOK, "00000002", notebook2.getId()));
    }

    @Test
    void testFindExperiments() {
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(new GlobalSearchRequest().withQuery("ed1"), Paging.DEFAULT);
        assertResults(results, tuple(EntityType.EXPERIMENT, experiment1.getName(), experiment1.getId()));
        assertThat(results.getItems().getFirst().getExperimentStatus()).isEqualTo(experiment1.getStatus());
    }

    @Test
    void testFindAll() {
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(new GlobalSearchRequest().withQuery("xx"), Paging.DEFAULT);
        assertResults(results
                , tuple(EntityType.PROJECT, project1.getName(), project1.getId())
                , tuple(EntityType.PROJECT, project2.getName(), project2.getId())
                , tuple(EntityType.NOTEBOOK, notebook1.getName(), notebook1.getId())
                , tuple(EntityType.NOTEBOOK, notebook2.getName(), notebook2.getId())
                , tuple(EntityType.EXPERIMENT, experiment1.getName(), experiment1.getId())
                , tuple(EntityType.EXPERIMENT, experiment2.getName(), experiment2.getId())
        );
        assertThat(results.getItems()).map(GlobalSearchResultDTO::getFragment, GlobalSearchResultDTO::getCreatedBy).containsExactly(
                tuple(project1.getDescription(), getMaggieUserRef()),
                tuple(project2.getDescription(), getMaggieUserRef()),
                tuple("nd1 <mark>xx</mark>", getMaggieUserRef()),
                tuple("nd2 <mark>xx</mark>", getMaggieUserRef()),
                tuple("ed1 <mark>xx</mark>", getMaggieUserRef()),
                tuple("ed2 <mark>xx</mark>", getMaggieUserRef())
        );
    }

    @Test
    void testFindExperimentsByTherapeuticArea() {
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(new GlobalSearchRequest().withTherapeuticArea(therapeuticArea1), Paging.DEFAULT);
        assertResults(results, tuple(EntityType.EXPERIMENT, experiment1.getName(), experiment1.getId()));
    }

    @Test
    void testFindExperimentsByProjectCode() {
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(new GlobalSearchRequest().withProjectCode(projectCode2), Paging.DEFAULT);
        assertResults(results, tuple(EntityType.EXPERIMENT, experiment2.getName(), experiment2.getId()));
    }

    @Test
    void testFindExperimentsByStatus() {
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(new GlobalSearchRequest().withExperimentStatus(Set.of(ExperimentStatus.CANCELLED, ExperimentStatus.SUBMITTED)), Paging.DEFAULT);
        System.out.println(results);
        assertResults(results
                , tuple(EntityType.EXPERIMENT, experiment3.getName(), experiment3.getId())
        );
    }

    @Test
    void testFindAllByAuthor() {
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(new GlobalSearchRequest().withAuthor(Set.of(getBartUserRef())), Paging.DEFAULT);
        assertResults(results
                , tuple(EntityType.PROJECT, project3.getName(), project3.getId())
                , tuple(EntityType.NOTEBOOK, notebook3.getName(), notebook3.getId())
                , tuple(EntityType.EXPERIMENT, experiment3.getName(), experiment3.getId())
        );
    }

    @Test
    void testFindByMoleculeSubstructure() {
        String molFile = new String(loadResource(getClass(), "/ring-substructure.mol"));
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(
                new GlobalSearchRequest().withMoleculeStructure(new StructuralSearch(StructuralSearch.Type.SUBSTRUCTURE, molFile)),
                Paging.DEFAULT
        );
        assertResults(results, tuple(EntityType.EXPERIMENT, experiment2.getName(), experiment2.getId()));
        assertThat(results.getItems().getFirst().getReactionRoles()).isEqualTo(Set.of(ReactionRole.REACTANT, ReactionRole.OUTPUT));
    }

    @Test
    void testFindByMoleculeSubstructureAndRole() {
        String molFile = new String(loadResource(getClass(), "/ring-substructure.mol"));
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(
                new GlobalSearchRequest().withMoleculeStructure(new StructuralSearch(StructuralSearch.Type.SUBSTRUCTURE, molFile)).withReactionRole(ReactionRole.REACTANT),
                Paging.DEFAULT
        );
        assertResults(results, tuple(EntityType.EXPERIMENT, experiment2.getName(), experiment2.getId()));
        assertThat(results.getItems().getFirst().getReactionRoles()).isEqualTo(Set.of(ReactionRole.REACTANT));
    }

    @Test
    void testFindByMoleculeSubstructureAndRoleNotFound() {
        String molFile = new String(loadResource(getClass(), "/ring-substructure.mol"));
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(
                new GlobalSearchRequest().withMoleculeStructure(new StructuralSearch(StructuralSearch.Type.SUBSTRUCTURE, molFile)).withReactionRole(ReactionRole.SOLVENT),
                Paging.DEFAULT
        );
        assertResults(results);
    }

    // TODO test for EXACT and SIMILARITY molfile search types

    @Test
    void testFindByReactionSubstructure() {
        String rxnFile = new String(loadResource(getClass(), "/reaction-substructure.rxn"));
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(
                new GlobalSearchRequest().withReactionStructure(new StructuralSearch(StructuralSearch.Type.SUBSTRUCTURE, rxnFile)),
                Paging.DEFAULT
        );
        assertResults(results, tuple(EntityType.EXPERIMENT, experiment2.getName(), experiment2.getId()));
    }

    // TODO test for EXACT rxnfile

    @Test
    void testBatchPurity() {
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(
                new GlobalSearchRequest().withBatchPurity(new NumericSearch.GreaterThanOrEqual(0.1)),
                Paging.DEFAULT
        );
        assertResults(results, tuple(EntityType.EXPERIMENT, experiment2.getName(), experiment2.getId()));
    }

    @Test
    void testBatchYield() {
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(
                new GlobalSearchRequest().withBatchYield(new NumericSearch.GreaterThanOrEqual(0.1)),
                Paging.DEFAULT
        );
        assertResults(results, tuple(EntityType.EXPERIMENT, experiment2.getName(), experiment2.getId()));
    }

    @Test
    void testFindAllEntitiesQuickSearchAndAuthor() {
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(new GlobalSearchRequest().withQuery("xx").withAuthor(Set.of(getMaggieUserRef())), Paging.DEFAULT);
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
    void testFindByAllAttributes() {
        String molFile = new String(loadResource(getClass(), "/ring-substructure.mol"));
        Page<GlobalSearchResultDTO> results = globalSearchClient.search(new GlobalSearchRequest()
                .withQuery("xx")
                .withTherapeuticArea(therapeuticArea2)
                .withProjectCode(projectCode2)
                .withExperimentStatus(Set.of(ExperimentStatus.OPEN))
                .withAuthor(Set.of(getMaggieUserRef()))
                .withBatchYield(new NumericSearch.GreaterThanOrEqual(0.1))
                .withBatchPurity(new NumericSearch.GreaterThanOrEqual(0.1))
                .withMoleculeStructure(new StructuralSearch(StructuralSearch.Type.SUBSTRUCTURE, molFile))
                , Paging.DEFAULT);
        assertResults(results, tuple(EntityType.EXPERIMENT, experiment2.getName(), experiment2.getId()));
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
