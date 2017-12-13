package com.epam.indigoeln.core.service.search.impl.pubchem;

final class PubChemConst {
    private static final String RESOURCE_URI = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/";
    static final String GET_BY_KEY_URI = RESOURCE_URI + "listkey/{key}/cids/JSON";
    static final String GET_BY_CIDS_URI = RESOURCE_URI + "cid/SDF";

    static final String CID_PARAM = "cid";
    static final String MAX_RECORDS_PARAM = "MaxRecords";
    static final String THRESHOLD_PARAM = "Threshold";
    static final String SDF_PARAM = "sdf";

    static final String SEARCH_BY_FORMULA_URI = RESOURCE_URI + "formula/{formula}/JSON";
    static final String SEARCH_BY_SIMILARITY_URI = RESOURCE_URI + "similarity/sdf/JSON";
    static final String SEARCH_BY_IDENTITY_URI = RESOURCE_URI + "identity/sdf/JSON";
    static final String SEARCH_BY_SUBSTRUCTURE_URI = RESOURCE_URI + "substructure/sdf/JSON";

    static final long DELAY = 5000;
    static final int TIMEOUT = 60000;
    static final int CHUNK_COUNT = 20;

    static final String COMPOUND_CID = "PUBCHEM_COMPOUND_CID";
    static final String MOLECULAR_FORMULA = "PUBCHEM_MOLECULAR_FORMULA";
    static final String MOLECULAR_WEIGHT = "PUBCHEM_MOLECULAR_WEIGHT";
    static final String IUPAC_CAS_NAME = "PUBCHEM_IUPAC_CAS_NAME";

    private PubChemConst() {
    }
}