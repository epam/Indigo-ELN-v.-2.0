package com.epam.indigoeln.core.repository.registration;

import java.util.HashMap;
import java.util.Map;

public class RegistrationStatus {

    private Status status;

    private String message;

    private Map<String, String> compoundNumberMap;

    private Map<String, String> conversationalBatchNumberMap;

    private RegistrationStatus(Status status, String message) {
        this.status = status;
        this.message = message;
        compoundNumberMap = new HashMap<>();
        conversationalBatchNumberMap = new HashMap<>();
    }

    private RegistrationStatus(Status status) {
        this(status, null);
    }

    public static RegistrationStatus passed() {
        return new RegistrationStatus(Status.PASSED);
    }

    public static RegistrationStatus inProgress() {
        return new RegistrationStatus(Status.IN_PROGRESS);
    }

    public static RegistrationStatus failed() {
        return new RegistrationStatus(Status.FAILED);
    }

    public static RegistrationStatus failed(String message) {
        return new RegistrationStatus(Status.FAILED, message);
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public void addCompoundNumber(String batchNumber, String compoundNumber) {
        compoundNumberMap.put(batchNumber, compoundNumber);
    }

    public void addConversationalBatchNumber(String batchNumber, String conversationalBatchNumber) {
        conversationalBatchNumberMap.put(batchNumber, conversationalBatchNumber);
    }

    public String getCompoundNumber(String batchNumber) {
        return compoundNumberMap.get(batchNumber);
    }

    public String getConversationalBatchNumber(String batchNumber) {
        return conversationalBatchNumberMap.get(batchNumber);
    }

    public enum Status {
        PASSED, FAILED, IN_PROGRESS
    }
}
