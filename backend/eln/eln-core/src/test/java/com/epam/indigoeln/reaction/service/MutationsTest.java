package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.compound.model.FindSamplesRequest;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.model.Page;
import com.epam.indigoeln.eln.model.Paging;
import com.epam.indigoeln.reaction.model.CompoundRef;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.SaltCodeRef;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputSampleMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import com.epam.indigoeln.reaction.util.CalculationReportBuilder;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static com.epam.indigoeln.common.util.ModelUtil.loadResource;
import static com.epam.indigoeln.test.ClientCallAssert.assertThatClientCall;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
public class MutationsTest extends MutationsTestBase {

    @BeforeAll
    void beforeAll(@TempDir Path tempDir) {
        miscClient.loadCompoundsFromFileClient("compounds.sdf", tempDir, loadResource(getClass(), "/Compound_000000001_000500000.1.sdf"));
    }

    @BeforeEach
    void setUp(TestInfo testInfo) {
        reportBuilder = new CalculationReportBuilder(new File("calculations-" + testInfo.getDisplayName() + ".html"));
        initExperiment("MutationsTest");
    }

    @AfterEach
    void tearDown() {
        reportBuilder.close();
    }

    @Test
    void testAddEmptyInputToEmptyReaction() {
        applyMutation(new ReactionMutation.AddEmptyInput(reaction.getAnchor()));
    }

    @Test
    void testAddInputToEmptyReaction() {
        Page<SampleDTO> samples = compoundClient.findSamples(new FindSamplesRequest(), Paging.DEFAULT);
        applyMutation(new ReactionMutation.AddInput(reaction.getAnchor(), samples.getItems().getFirst().getId()));
    }

    @Test
    void testIncorrectRevision() {
        assertThatClientCall(() -> {
            experimentClient.mutateExperimentModel2(experiment.getId(), 100, new ReactionMutation.AddEmptyInput(reaction.getAnchor()));
        }).isConflict("incorrect revision 100 requested; current revision 0");
    }

    @Test
    void testIncorrectAnchor() {
        assertThatClientCall(() -> {
            experimentClient.mutateExperimentModel2Raw(experiment.getId(), model.getRevision(), "{\"type\": \"AddEmptyInput\", \"anchor\": \"invalid\"}");
        }).isBadRequest("Cannot construct instance of `com.epam.indigoeln.reaction.model.Anchor");
    }

    @Test
    void testSetInputRowSaltCodeAndEQ() {
        String molFile = new String(ModelUtil.loadResource(getClass(), "/reaction.rxn"));
        applyMutation(new ReactionMutation.SetScheme(reaction.getAnchor(), molFile));
        DictionaryItemRef saltCode = dictionaryClient.getSaltCodes().getFirst();
        applyMutation(new ReactionInputMutation.SetInputRowSaltCode(input1.getAnchor(), saltCode));
        ReactionInput input = model.locate(input1.getAnchor());
        assertThat(input.getCompound()).isInstanceOf(CompoundRef.Virtual.class);
        assertThat(input.getCompound().getSaltCode()).extracting(SaltCodeRef::getId, SaltCodeRef::getName).contains(saltCode.getId(), saltCode.getName());
        applyMutation(new ReactionInputMutation.SetInputRowSaltEQ(input1.getAnchor(), 2.0));
        input = model.locate(input1.getAnchor());
        assertThat(input.getCompound().getSaltCode()).extracting(SaltCodeRef::getId, SaltCodeRef::getName).contains(saltCode.getId(), saltCode.getName());
        assertThat(input.getCompound().getSaltEQ()).isCloseTo(2.0, Offset.offset(1e-6));
        reportBuilder.close();
    }
    
    @Test
    void testSetInputComment() {
        String molFile = new String(ModelUtil.loadResource(getClass(), "/reaction.rxn"));
        applyMutation(new ReactionMutation.SetScheme(reaction.getAnchor(), molFile));
        applyMutation(prepareResolveInputs());
        applyMutation(new ReactionInputSampleMutation.SetInputComment(input1Sample1.getAnchor(), "1111"));
    }
}
