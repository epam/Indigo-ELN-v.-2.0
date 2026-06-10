package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.model.UserRef;
import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.model.ExperimentRef;
import com.epam.indigoeln.eln.model.ProjectEditRequest;
import com.epam.indigoeln.eln.model.TemplateComponent;
import com.epam.indigoeln.eln.model.TemplateTab;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.EnteredValueSource;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.epam.indigoeln.test.FeignUtil;
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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JSONSerializationTest {

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
        EnteredValue<WeightUnit> value = EnteredValue.userEntered("5.00", WeightUnit.G, 1);
        String serialized = serialize(serializer, value);
        assertThat(serialized).isEqualToIgnoringWhitespace("""
                {"value": "5.00", "unit": "G", "source": 1}
                """);
        EnteredValue<WeightUnit> value2 = deserialize(deserializer, serialized, new TypeReference<>() {});
        assertThat(value2.getValue()).isEqualTo(5.0);
        assertThat(value2.getUnit()).isEqualTo(WeightUnit.G);
        assertThat(value2.getSource()).isEqualTo(EnteredValueSource.userEntered(1));
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
    void testSerializeDate(MapperType serializer, MapperType deserializer) {
        ZonedDateTime value = ZonedDateTime.of(2026, 3, 25, 13, 0, 0, 0, ZoneId.of("UTC"));
        String serialized = serialize(serializer, value);
        assertThat(serialized).isEqualTo("\"2026-03-25T13:00:00Z\"");
        ZonedDateTime value2 = deserialize(deserializer, serialized, ZonedDateTime.class);
        assertThat(value2).isEqualTo(value);
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
        System.out.println(json);
        assertThat(json).isEqualToIgnoringWhitespace("{\"type\": \"EditExperimentAttributes\", \"title\": \"new title\", \"therapeuticArea\": null, \"linkedExperiments\": [{\"id\": \"63c03dfa-803c-4d89-bf8d-16c536c28a40\", \"name\": \"00000001-0001\"}]}");
        Mutation mutation2 = deserialize(deserializer, json, Mutation.class);
        assertThat(mutation2).isEqualTo(mutation);
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
