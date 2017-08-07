package com.epam.indigoeln.web.rest.dto.search.request;

import java.util.Collections;
import java.util.List;

public class SearchCriterion {

    private String name;
    private String field;
    private String condition;
    private Object value;
    private List<String> components = Collections.emptyList();

    public SearchCriterion() {
    }

    public SearchCriterion(String name, String field, String condition, Object value) {
        this.name = name;
        this.field = field;
        this.condition = condition;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public List<String> getComponents() {
        return components;
    }

    public void setComponents(List<String> components) {
        this.components = components;
    }
}
