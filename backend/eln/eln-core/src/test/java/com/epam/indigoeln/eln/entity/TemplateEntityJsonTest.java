package com.epam.indigoeln.eln.entity;

import com.epam.indigoeln.eln.model.TemplateComponent;
import com.epam.indigoeln.eln.model.TemplateTab;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class TemplateEntityJsonTest {

    @Inject
    private ObjectMapper objectMapper;

    @Test
    void testGenerateTemplateEntityJson() throws Exception {
        TemplateEntity template = new TemplateEntity();
        template.setName("Sample Template");
        template.setTemplateTabs(List.of(
                new TemplateTab("Tab 1", List.of(
                        new TemplateComponent.Attachments(),
                        new TemplateComponent.Batches(),
                        new TemplateComponent.ConceptDetails(),
                        new TemplateComponent.ExperimentDetails(),
                        new TemplateComponent.ExperimentDescription()
                )),
                new TemplateTab("Tab 2", List.of(
                        new TemplateComponent.PreferredCompoundsDetails(),
                        new TemplateComponent.PreferredCompoundsSummary(),
                        new TemplateComponent.ReactionsDetails(),
                        new TemplateComponent.StoichiometryTable(true, false),
                        new TemplateComponent.ReactionScheme()
                )),
                new TemplateTab("Tab 3", List.of(
                        new TemplateComponent.Reactants(),
                        new TemplateComponent.IntendedProducts()
                ))
        ));

        UserEntity user = new UserEntity();
        user.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        template.setCreatedBy(user);
        template.setCreatedAt(ZonedDateTime.now());
        template.setModifiedBy(user);
        template.setModifiedAt(ZonedDateTime.now());

        String json = objectMapper.writeValueAsString(template);

        assertThat(json).contains("\"name\":\"Sample Template\"");
        assertThat(json).contains("\"templateTabs\"");
        assertThat(json).contains("\"createdBy\"");
        assertThat(json).contains("\"modifiedBy\"");
        assertThat(json).contains("\"type\":\"attachments\"");
        assertThat(json).contains("\"type\":\"batches\"");
        assertThat(json).contains("\"type\":\"conceptDetails\"");
        assertThat(json).contains("\"type\":\"experimentDetails\"");
        assertThat(json).contains("\"type\":\"experimentDescription\"");
        assertThat(json).contains("\"type\":\"preferredCompoundsDetails\"");
        assertThat(json).contains("\"type\":\"preferredCompoundsSummary\"");
        assertThat(json).contains("\"type\":\"reactionsDetails\"");
        assertThat(json).contains("\"type\":\"stoichiometryTable\"");
        assertThat(json).contains("\"reactantsReagentsSolvents\":true");
        assertThat(json).contains("\"reactionProducts\":false");
        assertThat(json).contains("\"type\":\"reactionScheme\"");
        assertThat(json).contains("\"type\":\"reactants\"");
        assertThat(json).contains("\"type\":\"intendedProducts\"");

        ObjectMapper testObjectMapper = objectMapper.copy();
        testObjectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        java.nio.file.Path outputPath = java.nio.file.Paths.get("build", "test-results", "test", "template_entity.json");
        testObjectMapper.writeValue(outputPath.toFile(), template);

    }
}
