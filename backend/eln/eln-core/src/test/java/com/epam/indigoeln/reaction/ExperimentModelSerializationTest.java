package com.epam.indigoeln.reaction;

import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputMutation;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.MolWeightUnit;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import com.epam.indigoeln.test.FeignUtil;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ExperimentModelSerializationTest {

    @Test
    void testSerialize() throws Exception {
        ExperimentModel model = new ExperimentModel();
        Reaction reaction = Reaction.create(model);
        model.setReactions(List.of(reaction));
        reaction.setRxnfile("molFile");

        ReactionInput input1 = ReactionInput.create(reaction, ReactionRole.REACTANT);
        input1.setCompound(new CompoundRef.Stored(UUID.randomUUID(), EnteredValue.fixed(1.0, MolWeightUnit.G_PER_MOL), 1.1, "C", "compoundKey", null));
        input1.setEq(EnteredValue.userLastEntered(10.0, NoUnit.NO_UNIT));
        ReactionInput input2 = ReactionInput.create(reaction, ReactionRole.REACTANT);
        input2.setCompound(new CompoundRef.Virtual(UUID.randomUUID(), "C", null, null, null, EnteredValue.fixed(1.0, MolWeightUnit.G_PER_MOL), 1.1, null));
        ReactionInput input3 = ReactionInput.create(reaction, ReactionRole.REACTANT);
        input3.setCompound(new CompoundRef.Unknown());
        ReactionInputSample inputSample1 = ReactionInputSample.create(input1);
        input1.setSamples(List.of(inputSample1));
        reaction.setInputs(List.of(input1, input2, input3));

        ReactionOutput output = ReactionOutput.create(reaction, ReactionOutputType.FINAL);
        output.setCompound(new CompoundRef.Virtual(UUID.randomUUID(), "C", null, null, null, EnteredValue.fixed(2.0, MolWeightUnit.G_PER_MOL), 2.2, null));
        ReactionOutputSample outputSample = ReactionOutputSample.create("00000000-0000", output);
        output.setSamples(List.of(outputSample));
        reaction.setOutputs(List.of(output));

        String json = FeignUtil.OBJECT_MAPPER.writeValueAsString(model);
        ExperimentModel model2 = FeignUtil.OBJECT_MAPPER.readValue(json, ExperimentModel.class);

        String json2 = FeignUtil.OBJECT_MAPPER.writeValueAsString(model2);
        assertThat(json2).isEqualTo(json);
    }

    @Test
    void testSerializeMutation() throws Exception {
        Mutation mutation = new ReactionInputMutation.SetInputRowMol(new Anchor.Input(3), 2.5, MolUnit.MMOL);
        String json = FeignUtil.OBJECT_MAPPER.writeValueAsString(mutation);
        System.out.println(json);
        Mutation mutation2 = FeignUtil.OBJECT_MAPPER.readValue(json, Mutation.class);
        assertThat(mutation2).isEqualTo(mutation);
    }
}
