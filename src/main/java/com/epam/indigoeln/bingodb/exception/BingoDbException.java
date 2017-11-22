package com.epam.indigoeln.bingodb.exception;

/**
 * Runtime exception for BingoDB.
 */
public class BingoDbException extends RuntimeException {

    /**
     * Create a new BingoDbException instance.
     *
     * @param message an error message for exception
     */
    public BingoDbException(String message) {
        super(message);
    }
}
