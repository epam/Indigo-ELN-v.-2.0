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

import static com.epam.indigoeln.eln.util.CustomAssertions.assertThatClientCall;
import static org.assertj.core.api.Assertions.assertThat;


@QuarkusTest
@JwtSecurity
@TestSecurity(user = TestHelper.LISA_USERNAME)
class TemplateServiceTest extends BaseTest {

    List<TemplateComponent> components_1 = List.of(new TemplateComponent.Attachments(), new TemplateComponent.StoichiometryTable(true, true));
    List<TemplateComponent> components_2 = List.of(new TemplateComponent.Batches(), new TemplateComponent.PreferredCompoundsDetails());

    List<TemplateTab> templateTabs = List.of(new TemplateTab("tabName", components_1), new TemplateTab("tabName2", components_2));

    @Test
    void testCreateTemplateValidation() {
        assertThatClientCall(() -> templateClient.createTemplate(new TemplateRequest(null, List.of())))
                .isBadRequest("must not be empty");
    }

    @Test
    void testCreateTemplate() {
        TemplateDetailsDTO template = templateClient.createTemplate(new TemplateRequest("testCreateTemplate", templateTabs));
        assertThat(template.getId()).isNotNull();
        assertThat(template.getName()).isEqualTo("testCreateTemplate");
        assertThat(template.getCreatedBy().getDisplayName()).isEqualTo(TestHelper.LISA_DISPLAY_NAME);
        assertThat(template.getCreatedAt()).isNotNull();
        assertThat(template.getModifiedBy().getDisplayName()).isEqualTo(TestHelper.LISA_DISPLAY_NAME);
        assertThat(template.getModifiedAt()).isNotNull();
    }

    @Test
    void testGetTemplate() {
        TemplateDetailsDTO createdTemplate = templateClient.createTemplate(new TemplateRequest("testGetTemplate", templateTabs));
        TemplateDetailsDTO loadedTemplate = templateClient.getTemplate(createdTemplate.getId());
        assertThat(loadedTemplate).usingRecursiveComparison().isEqualTo(createdTemplate);
    }

    @Test
    void testGetTemplates() {
        templateClient.createTemplate(new TemplateRequest("testGetTemplates", templateTabs));
        Page<TemplateDTO> templates = templateClient.getTemplates(Paging.DEFAULT);
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
        TemplateDetailsDTO template = templateClient.createTemplate(new TemplateRequest("testEditTemplate", templateTabs));
        TemplateDetailsDTO notModified = templateClient.editTemplate(template.getId(), new TemplateEditRequest(null));
        assertThat(notModified).usingRecursiveComparison(TestHelper.COMPARE_WITHOUT_MODIFIED_AT).isEqualTo(template);
        TemplateDetailsDTO modified = templateClient.editTemplate(template.getId(), new TemplateEditRequest(Optional.of("testEditTemplate_new")));
        assertThat(modified.getName()).isEqualTo("testEditTemplate_new");
        TemplateDetailsDTO saved = templateClient.getTemplate(template.getId());
        assertThat(saved).usingRecursiveComparison().isEqualTo(modified);
    }

    @Test
    void testCreateTemplateWithDuplicateName() {
        templateClient.createTemplate(new TemplateRequest("DuplicateTemplateName", templateTabs));

        assertThatClientCall(() -> templateClient.createTemplate(new TemplateRequest("DuplicateTemplateName", templateTabs)))
                .isBadRequest("A template with this name already exists. Please choose a different name.");
    }
}
