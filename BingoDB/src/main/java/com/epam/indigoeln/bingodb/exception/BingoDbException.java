package com.epam.indigoeln.bingodb.exception;

public class BingoDbException extends RuntimeException {

    private static final long serialVersionUID = -4847681103134512032L;

    public BingoDbException(String message) {
        super(message);
    }
}
