package com.epam.indigoeln.assay.service.calc;

/** Identifiers for the built-in library calculations (stored in {@code Calculation.library_id}). */
public final class LibraryCalculations {

    public static final String MEAN = "MEAN";
    public static final String SD = "SD";
    public static final String CV = "CV";
    public static final String PERCENT_INHIBITION = "PERCENT_INHIBITION";
    public static final String NORMALIZE_TO_CONTROLS = "NORMALIZE_TO_CONTROLS";
    public static final String Z_PRIME = "Z_PRIME";
    public static final String IC50_4PL = "IC50_4PL";

    private LibraryCalculations() {
    }
}
