package com.epam.indigoeln.eln.util;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.eln.model.TemplateRequest;
import com.epam.indigoeln.eln.model.TemplateTab;
import com.epam.indigoeln.eln.repository.TemplateRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for validating template requests.
 */
public class TemplateValidationUtil {

    /**
     * Validates a template request based on the provided specification.
     *
     * @param templateRequest    the template request to validate.
     * @param templateRepository the template repository.
     */
    public static void validateTemplateRequest(TemplateRequest templateRequest, TemplateRepository templateRepository) {
        if (templateRequest.getName().isBlank()) {
            throw new InvalidRequestException("Template name is required.");
        }

        if (templateRepository.existsByName(templateRequest.getName())) {
            throw new InvalidRequestException("A template with this name already exists. Please choose a different name.");
        }

        List<TemplateTab> tabs = templateRequest.getTemplateTabs();
        if (tabs.isEmpty()) {
            throw new InvalidRequestException("A template must contain at least one tab.");
        }

        for (TemplateTab tab : tabs) {
            if (tab.getName().isBlank()) {
                throw new InvalidRequestException("Tab name cannot be empty.");
            }

            if (tab.getComponents().isEmpty()) {
                throw new InvalidRequestException("Each tab must contain at least one component.");
            }

            if (hasDuplicateComponents(tab.getComponents())) {
                throw new InvalidRequestException("This component has already been added to the tab.");
            }
        }
    }

    /**
     * Checks if a list of components contains duplicates.
     *
     * @param components the list of components to check.
     * @return true if duplicates are found, false otherwise.
     */
    private static boolean hasDuplicateComponents(List<?> components) {
        Set<Object> uniqueComponents = new HashSet<>(components);
        return uniqueComponents.size() != components.size();
    }
}