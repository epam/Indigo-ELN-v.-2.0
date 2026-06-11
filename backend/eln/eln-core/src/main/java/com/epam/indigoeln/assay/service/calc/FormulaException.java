package com.epam.indigoeln.assay.service.calc;

/** Raised when a custom formula cannot be parsed or evaluated. */
public class FormulaException extends RuntimeException {

    public FormulaException(String message) {
        super(message);
    }
}
