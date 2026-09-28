package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.model.MolFormula;
import com.epam.indigoeln.common.model.UserRef;
import com.epam.indigoeln.common.model.units.WeightUnit;
import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.model.ExperimentRef;
import com.epam.indigoeln.eln.model.ProjectEditRequest;
import com.epam.indigoeln.eln.model.TemplateComponent;
import com.epam.indigoeln.eln.model.TemplateTab;
import com.epam.indigoeln.reaction.model.CompoundRef;
import com.epam.indigoeln.reaction.model.EnteredValue;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.InputAnchor;
import com.epam.indigoeln.reaction.model.InputSampleAnchor;
import com.epam.indigoeln.reaction.model.OutputAnchor;
import com.epam.indigoeln.reaction.model.OutputSampleAnchor;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionAnchor;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.ReactionInputSample;
import com.epam.indigoeln.reaction.model.ReactionOutput;
import com.epam.indigoeln.reaction.model.ReactionOutputSample;
import com.epam.indigoeln.reaction.model.ReactionOutputType;
import com.epam.indigoeln.reaction.model.ReactionRole;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.test.FeignUtil;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.mutiny.core.Vertx;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import lombok.Value;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openapitools.jackson.nullable.JsonNullable;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.epam.indigoeln.common.model.units.MolWeightUnit.G_PER_MOL;
import static com.epam.indigoeln.common.model.units.NoUnit.NO_UNIT;
import static com.epam.indigoeln.common.model.units.WeightUnit.G;
import static com.epam.indigoeln.eln.test.EnteredValueAssert.assertThat;
import static com.epam.indigoeln.reaction.model.EnteredValue.fixed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JSONSerializationTest {

    ReactionAnchor REACTION = new ReactionAnchor(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    InputAnchor INPUT = new InputAnchor(UUID.fromString("00000000-0000-0000-0000-000000000010"));
    InputSampleAnchor INPUT_SAMPLE = new InputSampleAnchor(UUID.fromString("00000000-0000-0000-0000-000000000011"));
    OutputAnchor OUTPUT = new OutputAnchor(UUID.fromString("00000000-0000-0000-0000-000000000012"));
    OutputSampleAnchor OUTPUT_SAMPLE = new OutputSampleAnchor(UUID.fromString("00000000-0000-0000-0000-000000000013"));

    @Inject
    ObjectMapper quarkusObjectMapper;

    @Inject
    Vertx vertx;

    private List<Arguments> mappers() {
        return List.of(
                Arguments.of(MapperType.QUARKUS, MapperType.QUARKUS),
                Arguments.of(MapperType.STANDALONE, MapperType.STANDALONE),
                Arguments.of(MapperType.QUARKUS, MapperType.STANDALONE),
                Arguments.of(MapperType.STANDALONE, MapperType.QUARKUS)
        );
    }

    @ParameterizedTest
    @MethodSource("mappers")
    void testSerializeUserRef(MapperType serializer, MapperType deserializer) {
        UserRef userRef = ELNBaseTest.JOHN_USER_REF;
        String serialized = serialize(serializer, userRef);
        assertThat(serialized).isEqualToIgnoringWhitespace("{\"username\":\"john\",\"displayName\":\"John Doe\"}");
        UserRef deserialized = deserialize(deserializer, serialized, UserRef.class);
        assertThat(deserialized.getUsername()).isEqualTo(userRef.getUsername());
        assertThat(deserialized.getDisplayName()).isEqualTo(userRef.getDisplayName());
    }

    @ParameterizedTest
    @MethodSource("mappers")
    void testSerializeProjectEditRequest(MapperType serializer, MapperType deserializer) {
        // name - update, keywords - set to null, literature - not changed
        ProjectEditRequest request = new ProjectEditRequest(JsonNullable.of("name_new"), JsonNullable.of(null), JsonNullable.undefined(), JsonNullable.undefined());
        String serialized = serialize(serializer, request);
        assertThat(serialized).isEqualToIgnoringWhitespace("{\"name\":\"name_new\",\"keywords\":null}");
        ProjectEditRequest request2 = deserialize(deserializer, serialized, ProjectEditRequest.class);
        // updated to a value
        assertThat(request2.getName().isPresent()).isTrue();
        assertThat(request2.getName().get()).isEqualTo("name_new");
        // explicitly set to null
        assertThat(request2.getKeywords().isPresent()).isTrue();
        assertThat(request2.getKeywords().get()).isNull();
        // absent from JSON - not changed
        assertThat(request2.getLiterature().isPresent()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("mappers")
    void testSerializeEnteredValue(MapperType serializer, MapperType deserializer) {
        EnteredValue<WeightUnit> value = EnteredValue.userEntered("5.00", G, 1);
        String serialized = serialize(serializer, value);
        assertThat(serialized).isEqualToIgnoringWhitespace("""
                {"value": "5.00", "unit": "G", "source": 1}
                """);
        EnteredValue<WeightUnit> value2 = deserialize(deserializer, serialized, new TypeReference<>() {});
        assertThat(value2).hasValue(5, G).isUserEntered(1);
    }

    @ParameterizedTest
    @MethodSource("mappers")
    void testSerializeExactEnteredValue(MapperType serializer, MapperType deserializer) {
        EnteredValue<WeightUnit> value = EnteredValue.fixedExact(0.1234567, 2, G);
        String serialized = serialize(serializer, value);
        assertThat(serialized).isEqualToIgnoringWhitespace("""
                {"value": "0.12", "exactValue": 0.1234567, "unit": "G", "source": "fixed"}
                """);
        EnteredValue<WeightUnit> value2 = deserialize(deserializer, serialized, new TypeReference<>() {});
        assertThat(value2).isEqualTo(value);
    }

    @ParameterizedTest
    @MethodSource("mappers")
    void testSerializeByteArray(MapperType serializer, MapperType deserializer) {
        ByteData value = new ByteData(new byte[] {0, 1, 2});
        String serialized = serialize(serializer, value);
        assertThat(serialized).startsWith("{\"data\":");
        ByteData value2 = deserialize(deserializer, serialized, ByteData.class);
        assertThat(value2.data).isEqualTo(value.data);
    }

    @ParameterizedTest
    @MethodSource("mappers")
    void testSerializeInstant(MapperType serializer, MapperType deserializer) {
        Instant value = ZonedDateTime.of(2026, 3, 25, 13, 0, 0, 0, ZoneId.of("UTC")).toInstant();
        String serialized = serialize(serializer, value);
        assertThat(serialized).isEqualTo("\"2026-03-25T13:00:00Z\"");
        Instant value2 = deserialize(deserializer, serialized, Instant.class);
        assertThat((Object) value2).isEqualTo(value);
    }

    @ParameterizedTest
    @MethodSource("mappers")
    void testSerializeTemplateTabs(MapperType serializer, MapperType deserializer) {
        List<TemplateTab> list = List.of(
                new TemplateTab("Tab 1", List.of(
                        new TemplateComponent.Batches(),
                        new TemplateComponent.ExperimentDetails(),
                        new TemplateComponent.ExperimentDescription()
                )),
                new TemplateTab("Tab 2", List.of(
                        new TemplateComponent.StoichiometryTable(false, true, false)
                )),
                new TemplateTab("Tab 3", List.of(
                        new TemplateComponent.Attachments()
                ))
        );

        String serialized = serialize(serializer, list);
        List<TemplateTab> list2 = deserialize(deserializer, serialized, new TypeReference<>() {});
        assertThat(list2).isEqualTo(list);
    }

    @ParameterizedTest
    @MethodSource("mappers")
    void testSerializeMutation(MapperType serializer, MapperType deserializer) {
        Mutation mutation = new ExperimentMutation.EditExperimentAttributes(
                JsonNullable.of("new title"),
                JsonNullable.of(null),
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.of(Set.of(new ExperimentRef(UUID.fromString("63c03dfa-803c-4d89-bf8d-16c536c28a40"), "00000001-0001"))),
                JsonNullable.undefined(),
                JsonNullable.undefined()
        );
        String json = serialize(serializer, mutation);
        assertThat(json).isEqualToIgnoringWhitespace("{\"type\": \"EditExperimentAttributes\", \"title\": \"new title\", \"therapeuticArea\": null, \"linkedExperiments\": [{\"id\": \"63c03dfa-803c-4d89-bf8d-16c536c28a40\", \"name\": \"00000001-0001\"}]}");
        Mutation mutation2 = deserialize(deserializer, json, Mutation.class);
        assertThat(mutation2).isEqualTo(mutation);
    }

    @ParameterizedTest
    @MethodSource("mappers")
    void testSerialize(MapperType serializer, MapperType deserializer) {
        ExperimentModel model = new ExperimentModel();
        Reaction reaction = Reaction.create(model, REACTION);
        reaction.setRxnfile("molFile");

        ReactionInput input1 = ReactionInput.create(reaction, ReactionRole.REACTANT, INPUT, new CompoundRef.Stored(UUID.randomUUID(), null, null, null, fixed(1.0, 1, G_PER_MOL), fixed(1.1, 2, NO_UNIT), new MolFormula("C"), "compoundKey", null, "batchMF"));
        input1.setEq(EnteredValue.userEntered("10.0", NO_UNIT, 1));
        ReactionInput input2 = ReactionInput.create(reaction, ReactionRole.REACTANT, INPUT, new CompoundRef.Virtual(UUID.randomUUID(), new MolFormula("C"), null, null, null, null, fixed(1.0, 1, G_PER_MOL), fixed(1.1, 2, NO_UNIT), null, "batchMF"));
        ReactionInput input3 = ReactionInput.create(reaction, ReactionRole.REACTANT, INPUT, new CompoundRef.Unknown());
        ReactionInputSample inputSample1 = ReactionInputSample.create(input1, INPUT_SAMPLE);
        input1.setSamples(List.of(inputSample1));
        reaction.setInputs(List.of(input1, input2, input3));

        CompoundRef.Virtual compoundRef = new CompoundRef.Virtual(UUID.randomUUID(), new MolFormula("C"), null, null, null, null, fixed(2.0, 1, G_PER_MOL), fixed(2.2, 2, NO_UNIT), null, "batchMF");
        ReactionOutput output = ReactionOutput.create(reaction, ReactionOutputType.FINAL, true, "P1", OUTPUT, compoundRef, EnteredValue.DEFAULT_ONE);
        ReactionOutputSample outputSample = ReactionOutputSample.create(output, "00000000-0000", OUTPUT_SAMPLE, EnteredValue.DEFAULT_ONE_HUNDRED);
        output.setSamples(List.of(outputSample));
        reaction.setOutputs(List.of(output));

        String json = serialize(serializer, model);
        ExperimentModel model2 = deserialize(deserializer, json, ExperimentModel.class);

        String json2 = serialize(serializer, model2);
        assertThat(json2).isEqualTo(json);
    }

    @ParameterizedTest
    @MethodSource("mappers")
    void testDeserializeUnknownField(MapperType serializer, MapperType deserializer) {
        assertThatThrownBy(() -> {
            deserialize(serializer, "{\"type\": \"AddEmptyInput\", \"anchor\": \"R1\", \"unknownField\": 123}", Mutation.class);
        }).isInstanceOf(JacksonException.class);
    }

    ObjectMapper getMapper(MapperType mapperType) {
        return switch (mapperType) {
            case QUARKUS -> quarkusObjectMapper;
            case STANDALONE -> FeignUtil.OBJECT_MAPPER;
        };
    }

    @SneakyThrows
    private <T> String serialize(MapperType serializer, T value) {
        return getMapper(serializer).writeValueAsString(value);
    }

    @SneakyThrows
    private <T> T deserialize(MapperType deserializer, String serialized, Class<T> klass) {
        return getMapper(deserializer).readValue(serialized, klass);
    }

    @SneakyThrows
    private <T> T deserialize(MapperType deserializer, String serialized, TypeReference<T> type) {
        return getMapper(deserializer).readValue(serialized, type);
    }

    enum MapperType {
        QUARKUS, STANDALONE
    }

    @Value
    static class ByteData {
        byte[] data;
    }
}
