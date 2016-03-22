package com.epam.indigoeln;

public class IndigoRuntimeException extends RuntimeException {

    public IndigoRuntimeException() {
        super();
    }

    public IndigoRuntimeException(String message) {
        super(message);
    }

    public IndigoRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public IndigoRuntimeException(Throwable cause) {
        super(cause);
    }

    public IndigoRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
