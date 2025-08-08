package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.model.ProjectEditRequest;
import com.epam.indigoeln.eln.model.UserRef;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class JSONSerializationTest {

    @Inject
    ObjectMapper objectMapper;

    @Test
    void testSerializeUserRef() throws Exception {
        UserRef userRef = new UserRef(UUID.randomUUID(), "username", "Test User");
        String serialized = objectMapper.writeValueAsString(userRef);
        assertThat(serialized).isEqualTo("{\"id\":\"" + userRef.getId() + "\",\"username\":\"username\",\"displayName\":\"Test User\"}");
        UserRef deserialized = objectMapper.readValue(serialized, UserRef.class);
        assertThat(deserialized.getId()).isEqualTo(userRef.getId());
        assertThat(deserialized.getDisplayName()).isEqualTo(userRef.getDisplayName());
    }

    @Test
    void testSerializeProjectEditRequest() throws Exception {
        // name - update, keywords - set to null, literature - not changed
        ProjectEditRequest request = new ProjectEditRequest(Optional.of("name_new"), Optional.empty(), null, null);
        String serialized = objectMapper.writeValueAsString(request);
        assertThat(serialized).isEqualTo("{\"name\":\"name_new\",\"keywords\":null}");
        ProjectEditRequest request2 = objectMapper.readValue(serialized, ProjectEditRequest.class);
        assertThat(request2.getName()).get().isEqualTo("name_new");
        assertThat(request2.getKeywords()).isEmpty();
        assertThat(request2.getLiterature()).isNull();
    }
}
