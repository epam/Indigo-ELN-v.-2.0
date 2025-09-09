package com.epam.indigoeln.eln.util;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.eln.BaseTest;
import com.epam.indigoeln.eln.model.TemplateComponent;
import com.epam.indigoeln.eln.model.TemplateRequest;
import com.epam.indigoeln.eln.model.TemplateTab;
import com.epam.indigoeln.eln.repository.TemplateRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.jwt.JwtSecurity;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@QuarkusTest
@JwtSecurity
class TemplateValidationUtilTest extends BaseTest {

    List<TemplateComponent> components = List.of(new TemplateComponent.Attachments(), new TemplateComponent.StoichiometryTable(true, true));

    @Inject
    TemplateRepository templateRepository;

    @Test
    void testValidateTemplateRequestWithValidData() {
        List<TemplateTab> validTabs = List.of(new TemplateTab("ValidTab", components));
        TemplateRequest validRequest = new TemplateRequest("ValidTemplate", validTabs);

        TemplateValidationUtil.validateTemplateRequest(validRequest, templateRepository);
    }

    @Test
    void testValidateTemplateRequestWithEmptyName() {
        TemplateRequest invalidRequest = new TemplateRequest("", List.of());

        assertThatThrownBy(() -> TemplateValidationUtil.validateTemplateRequest(invalidRequest, templateRepository))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Template name is required.");
    }

    @Test
    void testValidateTemplateRequestWithEmptyTabList() {
        TemplateRequest invalidRequest = new TemplateRequest("EmptyTabsTemplate", List.of());

        assertThatThrownBy(() -> TemplateValidationUtil.validateTemplateRequest(invalidRequest, templateRepository))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("A template must contain at least one tab.");
    }

    @Test
    void testValidateTemplateRequestWithEmptyTabName() {
        List<TemplateTab> invalidTabs = List.of(new TemplateTab("", components));
        TemplateRequest invalidRequest = new TemplateRequest("InvalidTabNameTemplate", invalidTabs);

        assertThatThrownBy(() -> TemplateValidationUtil.validateTemplateRequest(invalidRequest, templateRepository))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Tab name cannot be empty.");
    }

    @Test
    void testValidateTemplateRequestWithNoComponentsInTab() {
        List<TemplateTab> invalidTabs = List.of(new TemplateTab("EmptyTab", List.of()));
        TemplateRequest invalidRequest = new TemplateRequest("NoComponentsTemplate", invalidTabs);

        assertThatThrownBy(() -> TemplateValidationUtil.validateTemplateRequest(invalidRequest, templateRepository))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Each tab must contain at least one component.");
    }

    @Test
    void testValidateTemplateRequestWithDuplicateComponentsInTab() {
        TemplateComponent duplicateComponent = new TemplateComponent.Attachments();
        List<TemplateTab> invalidTabs = List.of(new TemplateTab("TabWithDuplicates", List.of(duplicateComponent, duplicateComponent)));
        TemplateRequest invalidRequest = new TemplateRequest("DuplicateComponentsTemplate", invalidTabs);

        assertThatThrownBy(() -> TemplateValidationUtil.validateTemplateRequest(invalidRequest, templateRepository))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("This component has already been added to the tab.");
    }
}