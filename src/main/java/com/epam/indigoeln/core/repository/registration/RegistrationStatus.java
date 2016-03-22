package com.epam.indigoeln.core.repository.registration;

public class RegistrationStatus {

    private Status status;

    private String message;

    private RegistrationStatus(Status status, String message) {
        this.status = status;
        this.message = message;
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

    public enum Status {
        PASSED, FAILED, IN_PROGRESS
    }
}
