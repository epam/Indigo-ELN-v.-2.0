package com.epam.indigoeln.eln.util;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.eln.model.TemplateComponent;
import com.epam.indigoeln.eln.model.TemplateRequest;
import com.epam.indigoeln.eln.model.TemplateTab;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for validating template requests.
 */
public class TemplateValidationUtil {

    private TemplateValidationUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Validates a template request based on the provided specification.
     *
     * @param templateRequest    the template request to validate.
     */
    public static void validateTemplateRequest(TemplateRequest templateRequest) {
        List<TemplateTab> tabs = templateRequest.getTemplateTabs();

        for (TemplateTab tab : tabs) {
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
    private static boolean hasDuplicateComponents(List<TemplateComponent> components) {
        Set<String> uniqueTypes = new HashSet<>();
        for (TemplateComponent component : components) {
            String type = component.getClass().getSimpleName();
            if (!uniqueTypes.add(type)) {
                return true;
            }
        }
        return false;
    }

}