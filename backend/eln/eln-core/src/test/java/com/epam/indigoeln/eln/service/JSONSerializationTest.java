package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.model.ProjectEditRequest;
import com.epam.indigoeln.eln.model.UserRef;
import com.epam.indigoeln.reaction.model.CompoundRef;
import com.epam.indigoeln.reaction.model.patch.handler2.Patched;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.EnteredValueSource;
import com.epam.indigoeln.reaction.model.units.MolWeightUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.epam.indigoeln.reaction.util.SerializerUtils;
import com.epam.indigoeln.test.FeignUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.Value;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JSONSerializationTest {

    @Inject
    ObjectMapper quarkusObjectMapper;

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
    void testSerializeUserRef(MapperType serializer, MapperType deserializer) throws Exception {
        UserRef userRef = new UserRef(UUID.randomUUID(), "username", "Test User");
        String serialized = getMapper(serializer).writeValueAsString(userRef);
        assertThat(serialized).isEqualToIgnoringWhitespace("{\"id\":\"" + userRef.getId() + "\",\"username\":\"username\",\"displayName\":\"Test User\"}");
        UserRef deserialized = getMapper(deserializer).readValue(serialized, UserRef.class);
        assertThat(deserialized.getId()).isEqualTo(userRef.getId());
        assertThat(deserialized.getDisplayName()).isEqualTo(userRef.getDisplayName());
    }

    @ParameterizedTest
    @MethodSource("mappers")
    void testSerializeProjectEditRequest(MapperType serializer, MapperType deserializer) throws Exception {
        // name - update, keywords - set to null, literature - not changed
        ProjectEditRequest request = new ProjectEditRequest(Optional.of("name_new"), Optional.empty(), null, null);
        String serialized = getMapper(serializer).writeValueAsString(request);
        assertThat(serialized).isEqualToIgnoringWhitespace("{\"name\":\"name_new\",\"keywords\":null}");
        ProjectEditRequest request2 = getMapper(deserializer).readValue(serialized, ProjectEditRequest.class);
        assertThat(request2.getName()).get().isEqualTo("name_new");
        assertThat(request2.getKeywords()).isEmpty();
        assertThat(request2.getLiterature()).isNull();
    }

    @ParameterizedTest
    @MethodSource("mappers")
    void testSerializeEnteredValue(MapperType serializer, MapperType deserializer) throws Exception {
        EnteredValue<WeightUnit> value = new EnteredValue<>(5.0, WeightUnit.G, EnteredValueSource.USER_ENTERED);
        String serialized = getMapper(serializer).writeValueAsString(value);
        assertThat(serialized).isEqualToIgnoringWhitespace("""
                {"value":5.0,"unit":"G","source":"USER_ENTERED"}
                """);
        EnteredValue<WeightUnit> value2 = getMapper(deserializer).readValue(serialized, new TypeReference<>() {});
        assertThat(value2.getValue()).isEqualTo(5.0);
        assertThat(value2.getUnit()).isEqualTo(WeightUnit.G);
        assertThat(value2.getSource()).isEqualTo(EnteredValueSource.USER_ENTERED);
        assertThat(value2.isConflict()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("mappers")
    void testSerializeByteArray(MapperType serializer, MapperType deserializer) throws Exception {
        ByteData value = new ByteData(new byte[] {0, 1, 2});
        String serialized = getMapper(serializer).writeValueAsString(value);
        assertThat(serialized).startsWith("{\"data\":");
        ByteData value2 = getMapper(deserializer).readValue(serialized, ByteData.class);
        assertThat(value2.data).isEqualTo(value.data);
    }

    @ParameterizedTest
    @MethodSource("mappers")
    void testSerializeCompoundRef(MapperType serializer, MapperType deserializer) throws Exception {
        class Holder {

        }
        CompoundRef.Unknown compoundRef = new CompoundRef.Unknown();
        compoundRef.setMolWeight(EnteredValue.userLastEntered(10.0, MolWeightUnit.G_PER_MOL));
        Patched<CompoundRef> value = Patched.verbatim(compoundRef);
        JavaType type = getMapper(serializer).constructType(new TypeReference<Patched<CompoundRef>>() {});
        SerializerUtils.withRootTypeForTesting(type, () -> {
            String serialized = getMapper(serializer).writeValueAsString(value);
            assertThat(serialized).isEqualToIgnoringWhitespace("""
                    {"type": "unknown", "molWeight": {"value":10.0, "unit":"G_PER_MOL", "source":"USER_LAST_ENTERED"}}
                    """);
            Patched<CompoundRef> value2 = getMapper(deserializer).readValue(serialized, new TypeReference<>() {});
            assertThat(value2).isEqualTo(value);
        });
    }

    ObjectMapper getMapper(MapperType mapperType) {
        return switch (mapperType) {
            case QUARKUS -> quarkusObjectMapper;
            case STANDALONE -> FeignUtil.OBJECT_MAPPER;
        };
    }

    enum MapperType {
        QUARKUS, STANDALONE
    }

    @Value
    static class ByteData {
        byte[] data;
    }
}
