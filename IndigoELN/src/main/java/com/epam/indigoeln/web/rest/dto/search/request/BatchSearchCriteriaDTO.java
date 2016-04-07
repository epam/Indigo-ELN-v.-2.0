package com.epam.indigoeln.web.rest.dto.search.request;

import java.util.Optional;

public class BatchSearchCriteriaDTO {

    private String name;
    private String field;
    private Optional<String> condition;
    private Object value;

    public String getName() {
        return name;
    }

    public String getField() {
        return field;
    }

    public Optional<String> getCondition() {
        return condition;
    }

    public Object getValue() {
        return value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setField(String field) {
        this.field = field;
    }

    public void setCondition(String condition) {
        this.condition = Optional.ofNullable(condition);
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
