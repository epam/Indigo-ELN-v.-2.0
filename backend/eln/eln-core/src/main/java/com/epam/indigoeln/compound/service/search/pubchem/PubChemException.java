package com.epam.indigoeln.compound.service.search.pubchem;

class PubChemException extends RuntimeException {

    PubChemException(String message) {
        super(message);
    }

    public PubChemException(String message, Throwable cause) {
        super(message, cause);
    }

    static class NotFound extends PubChemException {

        NotFound(String message) {
            super(message);
        }
    }
}
