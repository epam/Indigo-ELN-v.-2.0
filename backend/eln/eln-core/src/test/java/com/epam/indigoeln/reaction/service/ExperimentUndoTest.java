package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.reaction.model.InputAnchor;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputSampleMutation;
import com.epam.indigoeln.reaction.model.units.VolumeUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.epam.indigoeln.test.ClientUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.List;
import java.util.Set;

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
        notebook = createNotebook(project.getId());
        experiment1 = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
        experiment2 = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
    }

    @BeforeEach
    void setUp(TestInfo testInfo) {
        initExperiment("ExperimentUndoServiceTest");
    }

    @Test
    void testSimpleUndoRedo() {
        experiment.mutateAddEmptyInput();
        assertThat(experimentClient.getExperimentRevisions(experiment.id(), true))
                .extracting(RevisionSummaryDTO::getSummary)
                .containsExactly("Experiment created", "Add empty input");
        InputAnchor anchor = experiment.input(1).getAnchor();
        assertThat(anchor).isNotNull();
        // undo
        experiment.mutate(new ExperimentMutation.Undo());
        assertThat(experiment.reaction().getInputs()).isEmpty();
        assertThat(experimentClient.getExperimentRevisions(experiment.id(), true))
                .extracting(RevisionSummaryDTO::getSummary)
                .containsExactly("Experiment created", "Add empty input", "Undo: Add empty input");
        // redo
        experiment.mutate(new ExperimentMutation.Redo());
        assertThat(experiment.input(1)).isNotNull();
        assertThat(experiment.input(1).getAnchor()).isEqualTo(anchor);
        assertThat(experimentClient.getExperimentRevisions(experiment.id(), true))
                .extracting(RevisionSummaryDTO::getSummary)
                .containsExactly("Experiment created", "Add empty input", "Undo: Add empty input", "Redo: Add empty input");
    }

    @Test
    void testSimpleNotRedoable() {
        assertThatClientCall(() -> {
            experiment.mutate(new ExperimentMutation.Redo());
        }).isBadRequest("Nothing to redo");
    }

    @Test
    void testAttributesUndoRedo() {
        String oldTitle = experiment.experiment().getTitle();

        experimentClient.editExperiment(experiment.id(), new ExperimentEditRequest(
                JsonNullable.of("newTitle"), JsonNullable.of(therapeuticArea), JsonNullable.of(projectCode)
                , JsonNullable.of("newDescription"), JsonNullable.of("newLiterature")
                , JsonNullable.of(Set.of(experiment1.toRef())), JsonNullable.of(Set.of(experiment2.toRef())), JsonNullable.of(Set.of(experiment1.toRef(), experiment2.toRef()))
        ));
        experiment.invalidate();
        assertUpdatedAttributes();

        experiment.mutate(new ExperimentMutation.Undo());
        assertInitialAttributes(oldTitle);

        experiment.mutate(new ExperimentMutation.Redo());
        assertUpdatedAttributes();
    }

    private void assertInitialAttributes(@Nullable String oldTitle) {
        assertThat(experiment.experiment().getTitle()).isEqualTo(oldTitle);
        assertThat(experiment.experiment().getTherapeuticArea()).isNull();
        assertThat(experiment.experiment().getProjectCode()).isNull();
        assertThat(experiment.experiment().getDescription()).isNull();
        assertThat(experiment.experiment().getLiterature()).isNull();
        assertThat(experiment.experiment().getLinkedExperiments()).isEmpty();
        assertThat(experiment.experiment().getContinuedFrom()).isEmpty();
        assertThat(experiment.experiment().getContinuedTo()).isEmpty();
    }

    private void assertUpdatedAttributes() {
        assertThat(experiment.experiment().getTitle()).isEqualTo("newTitle");
        assertThat(experiment.experiment().getTherapeuticArea()).isEqualTo(therapeuticArea);
        assertThat(experiment.experiment().getProjectCode()).isEqualTo(projectCode);
        assertThat(experiment.experiment().getDescription()).isEqualTo("newDescription");
        assertThat(experiment.experiment().getLiterature()).isEqualTo("newLiterature");
        assertThat(experiment.experiment().getLinkedExperiments()).containsExactlyInAnyOrder(experiment1.toRef());
        assertThat(experiment.experiment().getContinuedFrom()).containsExactlyInAnyOrder(experiment2.toRef());
        assertThat(experiment.experiment().getContinuedTo()).containsExactlyInAnyOrder(experiment1.toRef(), experiment2.toRef());
    }

    @Test
    void testAttachments() {
        List<AttachmentDTO> attachments = experimentClient.createExperimentAttachment(experiment.id(), ClientUtil.createFileUpload("attachment.txt", "content".getBytes()));

        experiment.mutate(new ExperimentMutation.Undo());
        assertThat(experiment.experiment().getAttachments()).isEmpty();

        experiment.mutate(new ExperimentMutation.Redo());
        assertThat(experiment.experiment().getAttachments()).singleElement().usingRecursiveComparison().isEqualTo(attachments.getFirst());
    }

    @Test
    void testNotUndoable() {
        experimentClient.completeExperiment(experiment.id());
        assertThatClientCall(() -> {
            experiment.mutate(new ExperimentMutation.Undo(), false);
        }).isBadRequest("Not undoable: Version 1");
    }

    @Test
    void testNothingToRedo() {
        assertThatClientCall(() -> {
            experiment.mutate(new ExperimentMutation.Redo(), false);
        }).isBadRequest("Nothing to redo");
    }

    @Test
    void testParallelEditsUndoRedo() {
        experimentClient.updateExperimentAccess(experiment.id(), List.of(
                new AccessForm(LISA_USERNAME, AccessLevel.ADMIN, false),
                new AccessForm(BART_USERNAME, AccessLevel.ADMIN, false)
        ));
        experiment.mutateAddEmptyInput();
        experiment.mutateAddEmptyInput();

        // lisa: L1 (experiment.input(1), weight 10G)
        //     -> L1
        withUser(LISA_USERNAME, () -> experiment.mutate(new ReactionInputSampleMutation.SetInputWeight(experiment.inputSample(1, 1).getAnchor(), "10", WeightUnit.G), false));
        assertThat(getRevisions(4)).containsExactly(
                "Set input sample weight to 10 G"
        );
        // lisa: L2 (experiment.input(1), volume 20ML)
        //     -> L1, L2
        withUser(LISA_USERNAME, () -> experiment.mutate(new ReactionInputSampleMutation.SetInputVolume(experiment.inputSample(1, 1).getAnchor(), "20", VolumeUnit.ML), false));
        assertThat(getRevisions(4)).containsExactly(
                "Set input sample weight to 10 G",
                "Set input sample volume to 20 ML"
        );
        // bart: B3 (experiment.input(2), weight 5G)
        //     -> L1, L2, B3
        withUser(BART_USERNAME, () -> experiment.mutate(new ReactionInputSampleMutation.SetInputWeight(experiment.inputSample(2, 1).getAnchor(), "5", WeightUnit.G), false));
        assertThat(getRevisions(4)).containsExactly(
                "Set input sample weight to 10 G",
                "Set input sample volume to 20 ML",
                "Set input sample weight to 5 G"
        );
        // bart: B4 (experiment.input(2), volume 7.5ML)
        //     -> L1, L2, B3, B4
        withUser(BART_USERNAME, () -> experiment.mutate(new ReactionInputSampleMutation.SetInputVolume(experiment.inputSample(2, 1).getAnchor(), "7.5", VolumeUnit.ML), false));
        assertThat(getRevisions(4)).containsExactly(
                "Set input sample weight to 10 G",
                "Set input sample volume to 20 ML",
                "Set input sample weight to 5 G",
                "Set input sample volume to 7.5 ML"
        );
        // lisa: undo (L2)
        //     -> L1, L2 (undone), B3, B4, UndoL2
        withUser(LISA_USERNAME, () -> experiment.mutate(new ExperimentMutation.Undo(), false));
        assertThat(getRevisions(4)).containsExactly(
                "Set input sample weight to 10 G",
                "Set input sample volume to 20 ML",
                "Set input sample weight to 5 G",
                "Set input sample volume to 7.5 ML",
                "Undo: Set input sample volume to 20 ML"
        );
        // lisa: undo (L1)
        //     -> L1 (undone), L2 (undone), B3, B4, UndoL2, UndoL1
        withUser(LISA_USERNAME, () -> experiment.mutate(new ExperimentMutation.Undo(), false));
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
        withUser(BART_USERNAME, () -> experiment.mutate(new ExperimentMutation.Undo(), false));
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
        withUser(LISA_USERNAME, () -> experiment.mutate(new ExperimentMutation.Redo(), false));
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
        withUser(LISA_USERNAME, () -> experiment.mutate(new ReactionInputMutation.SetInputRowChemicalName(experiment.input(1).getAnchor(), "chemicalName"), false));
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
            withUser(LISA_USERNAME, () -> experiment.mutate(new ExperimentMutation.Redo(), false));
        }).isBadRequest("Nothing to red");
    }

    private List<String> getRevisions(int skip) {
        return experimentClient.getExperimentRevisions(experiment.id(), true).stream()
                .skip(skip)
                .map(RevisionSummaryDTO::getSummary)
                .toList();
    }
}
