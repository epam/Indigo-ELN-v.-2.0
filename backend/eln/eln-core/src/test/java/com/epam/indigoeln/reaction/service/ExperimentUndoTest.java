package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.reaction.model.InputAnchor;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputSampleMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import com.epam.indigoeln.reaction.model.units.VolumeUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.epam.indigoeln.reaction.util.CalculationReportBuilder;
import com.epam.indigoeln.test.ClientUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.*;
import org.openapitools.jackson.nullable.JsonNullable;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.epam.indigoeln.test.ClientCallAssert.assertThatClientCall;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
public class ExperimentUndoTest extends MutationsTestBase {

    TherapeuticAreaRef therapeuticArea;
    ProjectCodeRef projectCode;
    ExperimentDetailsDTO experiment1, experiment2;

    @BeforeAll
    void beforeAll() {
        therapeuticArea = dictionaryClient.getFirst(BuiltInDictionary.THERAPEUTIC_AREA);
        projectCode = dictionaryClient.getFirst(BuiltInDictionary.PROJECT_CODE);
        ProjectDetailsDTO project = getOrCreateProject("ExperimentUndoTest");
        notebook = notebookClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
        experiment1 = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
        experiment2 = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
    }

    @BeforeEach
    void setUp(TestInfo testInfo) {
        reportBuilder = new CalculationReportBuilder(new File("build/calculations-" + testInfo.getTestMethod().get().getName() + ".html"));
        initExperiment("ExperimentUndoServiceTest");
    }

    @AfterEach
    void tearDown() {
        reportBuilder.close();
    }

    @Test
    void testSimpleUndoRedo() {
        applyMutation(new ReactionMutation.AddEmptyInput(reaction.getAnchor()), false);
        assertThat(experimentClient.getExperimentRevisions(experiment.getId(), true))
                .extracting(RevisionSummaryDTO::getSummary)
                .containsExactly("Experiment created", "Add empty input");
        InputAnchor anchor = input1.getAnchor();
        assertThat(anchor).isNotNull();
        // undo
        applyMutation(new ExperimentMutation.Undo());
        assertThat(input1).isNull();
        assertThat(experimentClient.getExperimentRevisions(experiment.getId(), true))
                .extracting(RevisionSummaryDTO::getSummary)
                .containsExactly("Experiment created", "Add empty input", "Undo: Add empty input");
        // redo
        applyMutation(new ExperimentMutation.Redo());
        assertThat(input1).isNotNull();
        assertThat(input1.getAnchor()).isEqualTo(anchor);
        assertThat(experimentClient.getExperimentRevisions(experiment.getId(), true))
                .extracting(RevisionSummaryDTO::getSummary)
                .containsExactly("Experiment created", "Add empty input", "Undo: Add empty input", "Redo: Add empty input");
    }

    @Test
    void testSimpleNotUndoable() {
        applyMutation(new ExperimentMutation.CompleteExperiment(), false);
        assertThatClientCall(() -> applyMutation(new ExperimentMutation.Undo()))
                .isBadRequest("Not undoable: Experiment completed");
    }

    @Test
    void testSimpleNotRedoable() {
        assertThatClientCall(() -> applyMutation(new ExperimentMutation.Redo()))
                .isBadRequest("Nothing to redo");
    }

    @Test
    void testAttributesUndoRedo() {
        String oldTitle = experiment.getTitle();

        applyMutation(new ExperimentMutation.EditExperimentAttributes(JsonNullable.of("newTitle"), JsonNullable.of(therapeuticArea), JsonNullable.of(projectCode)
                , JsonNullable.of("newDescription"), JsonNullable.of("newLiterature")
                , JsonNullable.of(Set.of(experiment1.toRef())), JsonNullable.of(Set.of(experiment2.toRef())), JsonNullable.of(Set.of(experiment1.toRef(), experiment2.toRef()))
        ), false);
        assertUpdatedAttributes();

        applyMutation(new ExperimentMutation.Undo());
        assertInitialAttributes(oldTitle);

        applyMutation(new ExperimentMutation.Redo());
        assertUpdatedAttributes();
    }

    private void assertInitialAttributes(@Nullable String oldTitle) {
        assertThat(experiment.getTitle()).isEqualTo(oldTitle);
        assertThat(experiment.getTherapeuticArea()).isNull();
        assertThat(experiment.getProjectCode()).isNull();
        assertThat(experiment.getDescription()).isNull();
        assertThat(experiment.getLiterature()).isNull();
        assertThat(experiment.getLinkedExperiments()).isEmpty();
        assertThat(experiment.getContinuedFrom()).isEmpty();
        assertThat(experiment.getContinuedTo()).isEmpty();
    }

    private void assertUpdatedAttributes() {
        assertThat(experiment.getTitle()).isEqualTo("newTitle");
        assertThat(experiment.getTherapeuticArea()).isEqualTo(therapeuticArea);
        assertThat(experiment.getProjectCode()).isEqualTo(projectCode);
        assertThat(experiment.getDescription()).isEqualTo("newDescription");
        assertThat(experiment.getLiterature()).isEqualTo("newLiterature");
        assertThat(experiment.getLinkedExperiments()).containsExactlyInAnyOrder(experiment1.toRef());
        assertThat(experiment.getContinuedFrom()).containsExactlyInAnyOrder(experiment2.toRef());
        assertThat(experiment.getContinuedTo()).containsExactlyInAnyOrder(experiment1.toRef(), experiment2.toRef());
    }

    @Test
    void testAttachments() {
        List<AttachmentDTO> attachments = experimentClient.createExperimentAttachment(experiment.getId(), ClientUtil.createFileUpload("attachment.txt", "content".getBytes()));
        experiment = experimentClient.getExperiment(experiment.getId());

        applyMutation(new ExperimentMutation.Undo());
        assertThat(experiment.getAttachments()).isEmpty();

        applyMutation(new ExperimentMutation.Redo());
        assertThat(experiment.getAttachments()).singleElement().usingRecursiveComparison().isEqualTo(attachments.getFirst());
    }

    @Test
    void testNotUndoable() {
        applyMutation(new ExperimentMutation.CompleteExperiment(), false);
        assertThatClientCall(() -> {
            applyMutation(new ExperimentMutation.Undo(), false);
        }).isBadRequest("Not undoable: Experiment completed");
    }

    @Test
    void testNothingToRedo() {
        assertThatClientCall(() -> {
            applyMutation(new ExperimentMutation.Redo(), false);
        }).isBadRequest("Nothing to redo");
    }

    @Test
    void testParallelEditsUndoRedo() {
        applyMutation(new ExperimentMutation.EditExperimentAccess(Stream.of(
                AccessForm.of(LISA_USERNAME, AccessLevel.ADMIN),
                AccessForm.of(BART_USERNAME, AccessLevel.ADMIN)
        ).flatMap(Collection::stream).toList()), false);
        applyMutation(new ReactionMutation.AddEmptyInput(reaction.getAnchor()), false);
        applyMutation(new ReactionMutation.AddEmptyInput(reaction.getAnchor()), false);

        // lisa: L1 (input1, weight 10G)
        //     -> L1
        withUser(LISA_USERNAME, () -> applyMutation(new ReactionInputSampleMutation.SetInputWeight(input1Sample1.getAnchor(), "10", WeightUnit.G), false));
        assertThat(getRevisions(4)).containsExactly(
                "Set input sample weight to 10 G"
        );
        // lisa: L2 (input1, volume 20ML)
        //     -> L1, L2
        withUser(LISA_USERNAME, () -> applyMutation(new ReactionInputSampleMutation.SetInputVolume(input1Sample1.getAnchor(), "20", VolumeUnit.ML), false));
        assertThat(getRevisions(4)).containsExactly(
                "Set input sample weight to 10 G",
                "Set input sample volume to 20 ML"
        );
        // bart: B3 (input2, weight 5G)
        //     -> L1, L2, B3
        withUser(BART_USERNAME, () -> applyMutation(new ReactionInputSampleMutation.SetInputWeight(input2Sample1.getAnchor(), "5", WeightUnit.G), false));
        assertThat(getRevisions(4)).containsExactly(
                "Set input sample weight to 10 G",
                "Set input sample volume to 20 ML",
                "Set input sample weight to 5 G"
        );
        // bart: B4 (input2, volume 7.5ML)
        //     -> L1, L2, B3, B4
        withUser(BART_USERNAME, () -> applyMutation(new ReactionInputSampleMutation.SetInputVolume(input2Sample1.getAnchor(), "7.5", VolumeUnit.ML), false));
        assertThat(getRevisions(4)).containsExactly(
                "Set input sample weight to 10 G",
                "Set input sample volume to 20 ML",
                "Set input sample weight to 5 G",
                "Set input sample volume to 7.5 ML"
        );
        // lisa: undo (L2)
        //     -> L1, L2 (undone), B3, B4, UndoL2
        withUser(LISA_USERNAME, () -> applyMutation(new ExperimentMutation.Undo(), false));
        assertThat(getRevisions(4)).containsExactly(
                "Set input sample weight to 10 G",
                "Set input sample volume to 20 ML",
                "Set input sample weight to 5 G",
                "Set input sample volume to 7.5 ML",
                "Undo: Set input sample volume to 20 ML"
        );
        // lisa: undo (L1)
        //     -> L1 (undone), L2 (undone), B3, B4, UndoL2, UndoL1
        withUser(LISA_USERNAME, () -> applyMutation(new ExperimentMutation.Undo(), false));
        assertThat(getRevisions(4)).containsExactly(
                "Set input sample weight to 10 G",
                "Set input sample volume to 20 ML",
                "Set input sample weight to 5 G",
                "Set input sample volume to 7.5 ML",
                "Undo: Set input sample volume to 20 ML",
                "Undo: Set input sample weight to 10 G"
        );
        // bart: undo (B4)
        //     -> L1 (undone), L2 (undone), B3, B4 (undone), UndoL2, UndoL1, UndoB4
        withUser(BART_USERNAME, () -> applyMutation(new ExperimentMutation.Undo(), false));
        assertThat(getRevisions(4)).containsExactly(
                "Set input sample weight to 10 G",
                "Set input sample volume to 20 ML",
                "Set input sample weight to 5 G",
                "Set input sample volume to 7.5 ML",
                "Undo: Set input sample volume to 20 ML",
                "Undo: Set input sample weight to 10 G",
                "Undo: Set input sample volume to 7.5 ML"
        );
        // lisa: redo (L1)
        //     -> L1, L2 (undone), B3, B4 (undone), UndoL2, UndoL1, UndoB4, RedoL1
        withUser(LISA_USERNAME, () -> applyMutation(new ExperimentMutation.Redo(), false));
        assertThat(getRevisions(4)).containsExactly(
                "Set input sample weight to 10 G",
                "Set input sample volume to 20 ML",
                "Set input sample weight to 5 G",
                "Set input sample volume to 7.5 ML",
                "Undo: Set input sample volume to 20 ML",
                "Undo: Set input sample weight to 10 G",
                "Undo: Set input sample volume to 7.5 ML",
                "Redo: Set input sample weight to 10 G"
        );
        // lisa: L5
        //     L2 is forever left undone
        //     -> L1, L2 (undone), B3, B4 (undone), UndoL2, UndoL1, UndoB4, RedoL1, L5
        withUser(LISA_USERNAME, () -> applyMutation(new ReactionInputMutation.SetInputRowChemicalName(input1.getAnchor(), "chemicalName"), false));
        assertThat(getRevisions(4)).containsExactly(
                "Set input sample weight to 10 G",
                "Set input sample volume to 20 ML",
                "Set input sample weight to 5 G",
                "Set input sample volume to 7.5 ML",
                "Undo: Set input sample volume to 20 ML",
                "Undo: Set input sample weight to 10 G",
                "Undo: Set input sample volume to 7.5 ML",
                "Redo: Set input sample weight to 10 G",
                "Set input chemical name to chemicalName"
        );
        // lisa: redo
        //     Error: nothing to redo! (L2 is not suitable)
        assertThatClientCall(() -> {
            withUser(LISA_USERNAME, () -> applyMutation(new ExperimentMutation.Redo(), false));
        }).isBadRequest("Nothing to red");
    }

    private List<String> getRevisions(int skip) {
        return experimentClient.getExperimentRevisions(experiment.getId(), true).stream()
                .skip(skip)
                .map(RevisionSummaryDTO::getSummary)
                .toList();
    }
}
