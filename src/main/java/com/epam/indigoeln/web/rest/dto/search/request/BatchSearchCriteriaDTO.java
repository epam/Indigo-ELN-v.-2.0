package com.epam.indigoeln.web.rest.dto.search.request;

public class BatchSearchCriteriaDTO {

    private String name;
    private String field;
    private String condition;
    private Object value;

    public String getName() {
        return name;
    }

    public String getField() {
        return field;
    }

    public String getCondition() {
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
        this.condition = condition;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
