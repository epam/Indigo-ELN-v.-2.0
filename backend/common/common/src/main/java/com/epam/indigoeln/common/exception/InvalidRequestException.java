package com.epam.indigoeln.common.exception;

public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }

    public static InvalidRequestException fail(String message) {
        throw new InvalidRequestException(message);
    }

    public static void validate(boolean condition, String message) {
        if (!condition) {
            throw new InvalidRequestException(message);
        }
    }
}
