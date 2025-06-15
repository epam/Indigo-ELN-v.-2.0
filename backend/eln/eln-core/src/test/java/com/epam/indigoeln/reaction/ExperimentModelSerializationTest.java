package com.epam.indigoeln.reaction;

import com.epam.indigoeln.eln.util.FeignUtil;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolWeightUnit;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ExperimentModelSerializationTest {

    @Test
    @SneakyThrows
    void testSerialize() {
        ReactionInput input1 = new ReactionInput();
        input1.setCompound(new CompoundRef.Stored(UUID.randomUUID(), "realCompound", EnteredValue.fixed(1.0, MolWeightUnit.G_PER_MOL), "molFile", "C"));
        input1.setEq(EnteredValue.userLastEntered(10.0, NoUnit.NO_UNIT));
        ReactionInput input2 = new ReactionInput();
        input2.setCompound(new CompoundRef.Virtual("molFile", "C"));
        ReactionInput input3 = new ReactionInput();
        input3.setCompound(new CompoundRef.Unknown());
        ReactionInputSample inputSample1 = new ReactionInputSample();
        input1.setSamples(List.of(inputSample1));

        ReactionOutput output = new ReactionOutput();
        output.setCompound(new CompoundRef.Virtual("molFile", "C"));
        ReactionOutputSample outputSample = new ReactionOutputSample();
        output.setSamples(List.of(outputSample));
        Reaction reaction = new Reaction();
        reaction.setInputs(List.of(input1, input2, input3));
        reaction.setOutputs(List.of(output));
        reaction.setMolFile("molFile");
        ExperimentModel model = new ExperimentModel(List.of(reaction));

        String json = FeignUtil.OBJECT_MAPPER.writeValueAsString(model);
        ExperimentModel model2 = FeignUtil.OBJECT_MAPPER.readValue(json, ExperimentModel.class);

        String json2 = FeignUtil.OBJECT_MAPPER.writeValueAsString(model2);
        assertThat(json2).isEqualTo(json);
    }
}
