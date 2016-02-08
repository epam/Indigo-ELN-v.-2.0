package com.epam.indigoeln.core.service.search;

public final class SearchServiceConstants {
    private SearchServiceConstants() {
    }

    public static final String CHEMISTRY_SEARCH_EXACT         = "exact";
    public static final String CHEMISTRY_SEARCH_SUBSTRUCTURE  = "substructure";
    public static final String CHEMISTRY_SEARCH_SIMILARITY    = "similarity";
    public static final String CHEMISTRY_SEARCH_MOLFORMULA    = "molformula";

    public static final String FULL_BATCH_NUMBER_FORMAT = "\\d{8}-\\d{4}-\\d{3}";
}
