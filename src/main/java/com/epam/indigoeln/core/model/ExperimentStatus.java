package com.epam.indigoeln.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ExperimentStatus {

    OPEN("Open"),
    COMPLETED("Completed"),
    SUBMITTED("Submitted"),
    SUBMIT_FAIL("Submit_Fail"),
    SINGING("Signing"),
    SINGED("Signed"),
    ARCHIVED("Archived");

    private final String value;

    ExperimentStatus(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }

    @JsonCreator
    public static ExperimentStatus fromValue(String value){
        for(ExperimentStatus status : ExperimentStatus.values()){
            if(status.toString().equals(value)){
                return status;
            }
        }
        throw new IllegalArgumentException();
    }
}