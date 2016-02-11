package com.epam.indigoeln.core.service.util;

import com.epam.indigoeln.core.model.Component;
import com.epam.indigoeln.web.rest.dto.ComponentDTO;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.util.ObjectUtils.nullSafeEquals;
import static org.springframework.util.ObjectUtils.nullSafeToString;

/**
 * Utility class for components operations
 */
public final class ComponentsUtil {

    private ComponentsUtil() {
    }

    /**
     * Extract batch number fields from list of components
     * @param components components list
     * @return list of batch numbers fetched from batch components
     */
    public static List<String> extractBatchNumbers(Collection<Component> components) {
        return  components.stream().
                filter(c -> nullSafeEquals(c.getName(), Component.TYPE_PRODUCT_BATCH_DETAILS) && c.getContent() != null).
                map(c -> nullSafeToString(c.getContent().get(Component.FIELD_BATCH_NUMBER))).
                collect(Collectors.toList());
    }

    /**
     * Find batch component by specified batch number
     * @param components components for search
     * @param number batch number to find
     * @return  batch component with given number
     */
    public static Optional<ComponentDTO> getBatchByNumber(Collection<Component> components, String number) {
        return  components == null ? Optional.empty() :
                components.stream().filter(c ->
                        nullSafeEquals(c.getName(), Component.TYPE_PRODUCT_BATCH_DETAILS) &&
                        c.getContent() != null &&
                        nullSafeEquals(number, c.getContent().get(Component.FIELD_BATCH_NUMBER))
                ).findAny().map(ComponentDTO::new);
    }
}
