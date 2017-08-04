package com.epam.indigoeln.web.rest.dto.print;

import java.util.Collections;
import java.util.List;

public class PrintRequest {
    private List<String> components = Collections.emptyList();
    private boolean includeAttachments;

    public List<String> getComponents() {
        return components;
    }

    public void setComponents(List<String> components) {
        this.components = components;
    }

    public boolean isIncludeAttachments() {
        return includeAttachments;
    }

    public void setIncludeAttachments(boolean includeAttachments) {
        this.includeAttachments = includeAttachments;
    }
}
