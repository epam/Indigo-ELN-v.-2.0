package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.model.*;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.JwtSecurity;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.epam.indigoeln.test.ClientCallAssert.assertThatClientCall;
import static org.assertj.core.api.Assertions.assertThat;


@QuarkusTest
@JwtSecurity
@TestSecurity(user = ELNBaseTest.LISA_USERNAME)
class TemplateServiceTest extends ELNBaseTest {

    List<TemplateComponent> components_1 = List.of(new TemplateComponent.Attachments(), new TemplateComponent.StoichiometryTable(true, true));
    List<TemplateComponent> components_2 = List.of(new TemplateComponent.Batches(), new TemplateComponent.PreferredCompoundsDetails());

    List<TemplateTab> templateTabs = List.of(new TemplateTab("tabName", components_1), new TemplateTab("tabName2", components_2));
    List<TemplateDetailsDTO> templatesToRemove = new ArrayList<>();

    @AfterAll
    void tearDownClass() {
        for (TemplateDetailsDTO template : templatesToRemove) {
            templateClient.deleteTemplate(template.getId());
        }
    }

    @Test
    void testCreateTemplateWithNullRequestValidation() {
        //noinspection DataFlowIssue
        assertThatClientCall(() -> createTemplate(new TemplateRequest(null, List.of())))
                .isBadRequest("must not be empty");
    }

    @Test
    void testCreateTemplateWithEmptyNameValidation() {
        List<TemplateTab> validTabs = List.of(new TemplateTab("ValidTab", components_1));
        TemplateRequest invalidRequest = new TemplateRequest("", validTabs);

        assertThatClientCall(() -> createTemplate(invalidRequest))
                .isBadRequest("must not be empty");
    }

    @Test
    void testCreateTemplateWithEmptyTabListValidation() {
        TemplateRequest invalidRequest = new TemplateRequest("EmptyTabsTemplate", List.of());

        assertThatClientCall(() -> createTemplate(invalidRequest))
                .isBadRequest("must not be empty");
    }

    @Test
    void testCreateTemplateWithNoComponentsInTabValidation() {
        List<TemplateTab> invalidTabs = List.of(new TemplateTab("EmptyTab", List.of()));
        TemplateRequest invalidRequest = new TemplateRequest("NoComponentsTemplate", invalidTabs);

        assertThatClientCall(() -> createTemplate(invalidRequest))
                .isBadRequest("must not be empty");
    }

    @Test
    void testCreateTemplateWithEmptyTabNameValidation() {
        List<TemplateTab> invalidTabs = List.of(new TemplateTab("", components_1));
        TemplateRequest invalidRequest = new TemplateRequest("InvalidTabNameTemplate", invalidTabs);

        assertThatClientCall(() -> createTemplate(invalidRequest))
                .isBadRequest("must not be empty");
    }

    @Test
    void testCreateTemplateWithDuplicateNameValidation() {
        createTemplate(new TemplateRequest("DuplicateTemplateName", templateTabs));

        assertThatClientCall(() -> createTemplate(new TemplateRequest("DuplicateTemplateName", templateTabs)))
                .isBadRequest("Template with name 'DuplicateTemplateName' already exists.");
    }

    @Test
    void testCreateTemplateWithDuplicateComponentsInTabValidation() {
        TemplateComponent duplicateComponent = new TemplateComponent.Attachments();
        List<TemplateTab> invalidTabs = List.of(new TemplateTab("TabWithDuplicates", List.of(duplicateComponent, duplicateComponent)));
        TemplateRequest invalidRequest = new TemplateRequest("DuplicateComponentsTemplate", invalidTabs);

        assertThatClientCall(() -> createTemplate(invalidRequest))
                .isBadRequest("This component has already been added to the tab.");
    }

    @Test
    void testCreateTemplate() {
        TemplateDetailsDTO template = createTemplate(new TemplateRequest("testCreateTemplate", templateTabs));
        assertThat(template.getId()).isNotNull();
        assertThat(template.getName()).isEqualTo("testCreateTemplate");
        assertThat(template.getCreatedBy().getDisplayName()).isEqualTo(LISA_DISPLAY_NAME);
        assertThat(template.getCreatedAt()).isNotNull();
        assertThat(template.getModifiedBy().getDisplayName()).isEqualTo(LISA_DISPLAY_NAME);
        assertThat(template.getModifiedAt()).isNotNull();
    }

    @Test
    void testGetTemplate() {
        TemplateDetailsDTO createdTemplate = createTemplate(new TemplateRequest("testGetTemplate", templateTabs));
        TemplateDetailsDTO loadedTemplate = templateClient.getTemplate(createdTemplate.getId());
        assertThat(loadedTemplate).usingRecursiveComparison().isEqualTo(createdTemplate);
    }

    @Test
    void testGetTemplates() {
        createTemplate(new TemplateRequest("testGetTemplates", templateTabs));
        Page<TemplateDTO> templates = templateClient.getTemplates(Paging.DEFAULT);
        assertThat(templates.getItems()).first().satisfies(template -> {
            assertThat(template.getId()).isNotNull();
            assertThat(template.getName()).isEqualTo("testGetTemplates");
            assertThat(template.getCreatedBy().getDisplayName()).isEqualTo(LISA_DISPLAY_NAME);
            assertThat(template.getCreatedAt()).isNotNull();
            assertThat(template.getModifiedBy().getDisplayName()).isEqualTo(LISA_DISPLAY_NAME);
            assertThat(template.getModifiedAt()).isNotNull();
        });
    }

    @Test
    void testEditTemplate() {
        TemplateDetailsDTO template = createTemplate(new TemplateRequest("testEditTemplate", templateTabs));
        TemplateDetailsDTO notModified = templateClient.editTemplate(template.getId(), new TemplateEditRequest(null));
        assertThat(notModified).usingRecursiveComparison(COMPARE_WITHOUT_MODIFIED_AT).isEqualTo(template);
        TemplateDetailsDTO modified = templateClient.editTemplate(template.getId(), new TemplateEditRequest(Optional.of("testEditTemplate_new")));
        assertThat(modified.getName()).isEqualTo("testEditTemplate_new");
        TemplateDetailsDTO saved = templateClient.getTemplate(template.getId());
        assertThat(saved).usingRecursiveComparison().isEqualTo(modified);
    }

    private TemplateDetailsDTO createTemplate(TemplateRequest request) {
        TemplateDetailsDTO template = templateClient.createTemplate(request);
        templatesToRemove.add(template);
        return template;
    }
}
