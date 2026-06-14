package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.SortOrder;
import com.epam.indigoeln.common.model.UserRef;
import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.model.*;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.ArrayList;
import java.util.List;

import static com.epam.indigoeln.test.ClientCallAssert.assertThatClientCall;
import static org.assertj.core.api.Assertions.assertThat;


@QuarkusTest
@TestSecurity(user = ELNBaseTest.LISA_USERNAME)
class TemplateServiceTest extends ELNBaseTest {

    List<TemplateComponent> components_1 = List.of(new TemplateComponent.Attachments(), new TemplateComponent.StoichiometryTable(true, true, true));
    List<TemplateComponent> components_2 = List.of(new TemplateComponent.Batches(), new TemplateComponent.ExperimentDescription());

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
        Page<TemplateDTO> templates = templateClient.getTemplates(null, null, null, Paging.DEFAULT);
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
    void testGetTemplatesSortByEarliest() {
        String uniquePrefix = "testGetTemplatesSortByEarliest_" + System.currentTimeMillis();
        createTemplate(new TemplateRequest(uniquePrefix + "_Template1", templateTabs));
        createTemplate(new TemplateRequest(uniquePrefix + "_Template2", templateTabs));
        createTemplate(new TemplateRequest(uniquePrefix + "_Template3", templateTabs));

        Page<TemplateDTO> templates = templateClient.getTemplates(uniquePrefix, SortOrder.EARLIEST, null, Paging.DEFAULT);

        assertThat(templates.getItems())
                .extracting(TemplateDTO::getName)
                .containsExactly(uniquePrefix + "_Template1", uniquePrefix + "_Template2", uniquePrefix + "_Template3");
    }

    @Test
    void testGetTemplatesSortByLatest() {
        String uniquePrefix = "testGetTemplatesSortByLatest_" + System.currentTimeMillis();
        createTemplate(new TemplateRequest(uniquePrefix + "_Template1", templateTabs));
        createTemplate(new TemplateRequest(uniquePrefix + "_Template2", templateTabs));
        createTemplate(new TemplateRequest(uniquePrefix + "_Template3", templateTabs));

        Page<TemplateDTO> templates = templateClient.getTemplates(uniquePrefix, SortOrder.LATEST, null, Paging.DEFAULT);

        assertThat(templates.getItems())
                .extracting(TemplateDTO::getName)
                .containsExactly(uniquePrefix + "_Template3", uniquePrefix + "_Template2", uniquePrefix + "_Template1");
    }

    @Test
    void testGetTemplatesDefaultSort() {
        String uniquePrefix = "testGetTemplatesDefaultSort_" + System.currentTimeMillis();
        createTemplate(new TemplateRequest(uniquePrefix + "_Template1", templateTabs));
        createTemplate(new TemplateRequest(uniquePrefix + "_Template2", templateTabs));
        createTemplate(new TemplateRequest(uniquePrefix + "_Template3", templateTabs));

        Page<TemplateDTO> templates = templateClient.getTemplates(uniquePrefix, null, null, Paging.DEFAULT);

        assertThat(templates.getItems())
                .extracting(TemplateDTO::getName)
                .containsExactly(uniquePrefix + "_Template3", uniquePrefix + "_Template2", uniquePrefix + "_Template1");
    }

    @Test
    void testGetTemplatesCreatedByMe() {
        String uniquePrefix = "testGetTemplatesCreatedByMe_" + System.currentTimeMillis();
        createTemplate(new TemplateRequest(uniquePrefix + "_MyTemplate1", templateTabs));
        createTemplate(new TemplateRequest(uniquePrefix + "_MyTemplate2", templateTabs));

        Page<TemplateDTO> templates = templateClient.getTemplates(uniquePrefix, null, true, Paging.DEFAULT);

        assertThat(templates.getItems())
                .extracting(TemplateDTO::getName)
                .contains(uniquePrefix + "_MyTemplate1", uniquePrefix + "_MyTemplate2");

        assertThat(templates.getItems())
                .extracting(TemplateDTO::getCreatedBy)
                .extracting(UserRef::getDisplayName)
                .containsOnly(LISA_DISPLAY_NAME);
    }

    @Test
    void testGetTemplatesCreatedByMeFalse() {
        String uniquePrefix = "testGetTemplatesCreatedByMeFalse_" + System.currentTimeMillis();
        createTemplate(new TemplateRequest(uniquePrefix + "_MyTemplate1", templateTabs));
        createTemplate(new TemplateRequest(uniquePrefix + "_MyTemplate2", templateTabs));

        Page<TemplateDTO> templates = templateClient.getTemplates(uniquePrefix, null, false, Paging.DEFAULT);
        assertThat(templates.getItems())
                .extracting(TemplateDTO::getName)
                .contains(uniquePrefix + "_MyTemplate1", uniquePrefix + "_MyTemplate2");
    }

    @Test
    void testGetTemplatesSearchExactMatch() {
        String uniquePrefix = "testGetTemplatesSearchExactMatch_" + System.currentTimeMillis();
        createTemplate(new TemplateRequest(uniquePrefix + "_SearchTestTemplate", templateTabs));
        createTemplate(new TemplateRequest(uniquePrefix + "_OtherTemplate", templateTabs));

        Page<TemplateDTO> templates = templateClient.getTemplates(uniquePrefix + "_SearchTestTemplate", null, null, Paging.DEFAULT);

        assertThat(templates.getItems())
                .extracting(TemplateDTO::getName)
                .containsOnly(uniquePrefix + "_SearchTestTemplate");
    }

    @Test
    void testGetTemplatesSearchPartialMatch() {
        String uniquePrefix = "testGetTemplatesSearchPartialMatch_" + System.currentTimeMillis();
        createTemplate(new TemplateRequest(uniquePrefix + "_SearchTestTemplate", templateTabs));
        createTemplate(new TemplateRequest(uniquePrefix + "_SearchAnotherTemplate", templateTabs));
        createTemplate(new TemplateRequest(uniquePrefix + "_OtherTemplate", templateTabs));

        Page<TemplateDTO> templates = templateClient.getTemplates(uniquePrefix + "_Search", null, null, Paging.DEFAULT);

        assertThat(templates.getItems())
                .extracting(TemplateDTO::getName)
                .containsExactlyInAnyOrder(uniquePrefix + "_SearchTestTemplate", uniquePrefix + "_SearchAnotherTemplate");

        assertThat(templates.getItems())
                .extracting(TemplateDTO::getName)
                .doesNotContain(uniquePrefix + "_OtherTemplate");
    }

    @Test
    void testGetTemplatesSearchCaseInsensitive() {
        String uniquePrefix = "testGetTemplatesSearchCaseInsensitive_" + System.currentTimeMillis();
        createTemplate(new TemplateRequest(uniquePrefix + "_SearchTestTemplate", templateTabs));

        Page<TemplateDTO> templates1 = templateClient.getTemplates(uniquePrefix + "_search", null, null, Paging.DEFAULT);
        Page<TemplateDTO> templates2 = templateClient.getTemplates(uniquePrefix + "_SEARCH", null, null, Paging.DEFAULT);
        Page<TemplateDTO> templates3 = templateClient.getTemplates(uniquePrefix + "_Search", null, null, Paging.DEFAULT);

        assertThat(templates1.getItems())
                .extracting(TemplateDTO::getName)
                .contains(uniquePrefix + "_SearchTestTemplate");
        assertThat(templates2.getItems())
                .extracting(TemplateDTO::getName)
                .contains(uniquePrefix + "_SearchTestTemplate");
        assertThat(templates3.getItems())
                .extracting(TemplateDTO::getName)
                .contains(uniquePrefix + "_SearchTestTemplate");
    }

    @Test
    void testGetTemplatesSearchNoResults() {
        String uniquePrefix = "testGetTemplatesSearchNoResults_" + System.currentTimeMillis();
        createTemplate(new TemplateRequest(uniquePrefix + "_ExistingTemplate", templateTabs));

        Page<TemplateDTO> templates = templateClient.getTemplates(uniquePrefix + "_NonExistentTemplate", null, null, Paging.DEFAULT);

        assertThat(templates.getItems()).isEmpty();
    }

    @Test
    void testGetTemplatesSearchAndSort() {
        String uniquePrefix = "testGetTemplatesSearchAndSort_" + System.currentTimeMillis();
        createTemplate(new TemplateRequest(uniquePrefix + "_SearchTemplate1", templateTabs));
        createTemplate(new TemplateRequest(uniquePrefix + "_SearchTemplate2", templateTabs));
        createTemplate(new TemplateRequest(uniquePrefix + "_SearchTemplate3", templateTabs));
        createTemplate(new TemplateRequest(uniquePrefix + "_OtherTemplate", templateTabs));

        Page<TemplateDTO> templates = templateClient.getTemplates(uniquePrefix + "_Search", SortOrder.EARLIEST, null, Paging.DEFAULT);

        assertThat(templates.getItems())
                .extracting(TemplateDTO::getName)
                .containsExactly(uniquePrefix + "_SearchTemplate1", uniquePrefix + "_SearchTemplate2", uniquePrefix + "_SearchTemplate3");
    }

    @Test
    void testGetTemplatesSearchSortAndCreatedByMe() {
        String uniquePrefix = "testGetTemplatesSearchSortAndCreatedByMe_" + System.currentTimeMillis();
        createTemplate(new TemplateRequest(uniquePrefix + "_MySearchTemplate1", templateTabs));
        createTemplate(new TemplateRequest(uniquePrefix + "_MySearchTemplate2", templateTabs));
        createTemplate(new TemplateRequest(uniquePrefix + "_OtherSearchTemplate", templateTabs));

        Page<TemplateDTO> templates = templateClient.getTemplates(uniquePrefix, SortOrder.LATEST, true, Paging.DEFAULT);
        List<TemplateDTO> searchTemplates = templates.getItems().stream()
                .filter(t -> t.getName().contains("Search"))
                .toList();

        assertThat(searchTemplates)
                .extracting(TemplateDTO::getName)
                .containsExactly(uniquePrefix + "_OtherSearchTemplate", uniquePrefix + "_MySearchTemplate2", uniquePrefix + "_MySearchTemplate1");

        assertThat(searchTemplates)
                .extracting(TemplateDTO::getCreatedBy)
                .extracting(UserRef::getDisplayName)
                .containsOnly(LISA_DISPLAY_NAME);
    }

    @Test
    void testGetTemplatesPagination() {
        String uniquePrefix = "testGetTemplatesPagination_" + System.currentTimeMillis();
        createTemplate(new TemplateRequest(uniquePrefix + "_Template1", templateTabs));
        createTemplate(new TemplateRequest(uniquePrefix + "_Template2", templateTabs));
        createTemplate(new TemplateRequest(uniquePrefix + "_Template3", templateTabs));

        Paging paging1 = new Paging(0, 2);
        Page<TemplateDTO> page0 = templateClient.getTemplates(uniquePrefix, SortOrder.LATEST, null, paging1);
        assertThat(page0.getTotalItems()).isEqualTo(3);
        assertThat(page0.getTotalPages()).isEqualTo(2);
        assertThat(page0.getItems()).hasSize(2);
        assertThat(page0.getItems()).extracting(TemplateDTO::getName).containsExactly(uniquePrefix + "_Template3", uniquePrefix + "_Template2");

        Paging paging2 = new Paging(1, 2);
        Page<TemplateDTO> page1 = templateClient.getTemplates(uniquePrefix, SortOrder.LATEST, null, paging2);
        assertThat(page1.getTotalItems()).isEqualTo(3);
        assertThat(page1.getTotalPages()).isEqualTo(2);
        assertThat(page1.getItems().size()).isEqualTo(1);
        assertThat(page1.getItems()).extracting(TemplateDTO::getName).contains(uniquePrefix + "_Template1");
    }

    @Test
    void testEditTemplate() {
        TemplateDetailsDTO template = createTemplate(new TemplateRequest("testEditTemplate", templateTabs));
        TemplateDetailsDTO notModified = templateClient.editTemplate(template.getId(), new TemplateEditRequest(JsonNullable.undefined()));
        assertThat(notModified).usingRecursiveComparison(COMPARE_WITHOUT_MODIFIED_AT).isEqualTo(template);
        TemplateDetailsDTO modified = templateClient.editTemplate(template.getId(), new TemplateEditRequest(JsonNullable.of("testEditTemplate_new")));
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
