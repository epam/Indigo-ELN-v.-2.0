package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.BaseTest;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.util.TestHelper;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.JwtSecurity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.epam.indigoeln.eln.service.CustomAssertions.assertThatClientCall;
import static org.assertj.core.api.Assertions.assertThat;


@QuarkusTest
@JwtSecurity
@TestSecurity(user = TestHelper.LISA_USERNAME)
class TemplateServiceTest extends BaseTest {

    List<TemplateComponent> components = List.of(new TemplateComponent.Attachments(), new TemplateComponent.StoichiometryTable());

    @Test
    void testCreateTemplateValidation() {
        assertThatClientCall(() -> templatesClient.createTemplate(new TemplateRequest(null, List.of())))
                .isBadRequest();
    }

    @Test
    void testCreateTemplate() {
        TemplateDetailsDTO template = templatesClient.createTemplate(new TemplateRequest("testCreateTemplate", components));
        assertThat(template.getId()).isNotNull();
        assertThat(template.getName()).isEqualTo("testCreateTemplate");
        assertThat(template.getCreatedBy().getDisplayName()).isEqualTo(TestHelper.LISA_DISPLAY_NAME);
        assertThat(template.getCreatedAt()).isNotNull();
        assertThat(template.getModifiedBy().getDisplayName()).isEqualTo(TestHelper.LISA_DISPLAY_NAME);
        assertThat(template.getModifiedAt()).isNotNull();
    }

    @Test
    void testGetTemplate() {
        TemplateDetailsDTO createdTemplate = templatesClient.createTemplate(new TemplateRequest("testGetTemplate", components));
        TemplateDetailsDTO loadedTemplate = templatesClient.getTemplate(createdTemplate.getId());
        assertThat(loadedTemplate).usingRecursiveComparison().isEqualTo(createdTemplate);
    }

    @Test
    void testGetTemplates() {
        templatesClient.createTemplate(new TemplateRequest("testGetTemplates", components));
        Page<TemplateDTO> templates = templatesClient.getTemplates(Paging.DEFAULT);
        assertThat(templates.getItems()).first().satisfies(template -> {
            assertThat(template.getId()).isNotNull();
            assertThat(template.getName()).isEqualTo("testGetTemplates");
            assertThat(template.getCreatedBy().getDisplayName()).isEqualTo(TestHelper.LISA_DISPLAY_NAME);
            assertThat(template.getCreatedAt()).isNotNull();
            assertThat(template.getModifiedBy().getDisplayName()).isEqualTo(TestHelper.LISA_DISPLAY_NAME);
            assertThat(template.getModifiedAt()).isNotNull();
        });
    }
    
    @Test
    void testEditTemplate() {
        TemplateDetailsDTO template = templatesClient.createTemplate(new TemplateRequest("testEditTemplate", components));
        TemplateDetailsDTO notModified = templatesClient.editTemplate(template.getId(), new TemplateEditRequest(null));
        assertThat(notModified).usingRecursiveComparison(TestHelper.COMPARE_WITHOUT_MODIFIED_AT).isEqualTo(template);
        TemplateDetailsDTO modified = templatesClient.editTemplate(template.getId(), new TemplateEditRequest(Optional.of("testEditTemplate_new")));
        assertThat(modified.getName()).isEqualTo("testEditTemplate_new");
        TemplateDetailsDTO saved = templatesClient.getTemplate(template.getId());
        assertThat(saved).usingRecursiveComparison().isEqualTo(modified);
    }
}
