package com.epam.indigoeln.core.service.search.impl.pubchem;

public class PubChemConst {
    public static final String RESOURCE_URI = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/";
    public static final String GET_BY_KEY_URI = RESOURCE_URI + "listkey/{key}/cids/JSON";
    public static final String GET_BY_CIDS_URI = RESOURCE_URI + "cid/SDF";

    public static final String CID_PARAM = "cid";
    public static final String MAX_RECORDS_PARAM = "MaxRecords";
    public static final String THRESHOLD_PARAM = "Threshold";
    public static final String SDF_PARAM = "sdf";

    public static final String SEARCH_BY_FORMULA_URI = RESOURCE_URI + "formula/{formula}/JSON";
    public static final String SEARCH_BY_SIMILARITY_URI = RESOURCE_URI + "similarity/sdf/JSON";
    public static final String SEARCH_BY_IDENTITY_URI = RESOURCE_URI + "identity/sdf/JSON";
    public static final String SEARCH_BY_SUBSTRUCTURE_URI = RESOURCE_URI + "substructure/sdf/JSON";

    public static final long DELAY = 5000;
    public static final long TIMEOUT = 60000;
    public static final int CHUNK_COUNT = 50;
    public static final int LIMIT = 500;

    public static final String COMPOUND_CID = "PUBCHEM_COMPOUND_CID";
    public static final String MOLECULAR_FORMULA = "PUBCHEM_MOLECULAR_FORMULA";
    public static final String MOLECULAR_WEIGHT = "PUBCHEM_MOLECULAR_WEIGHT";
    public static final String IUPAC_CAS_NAME = "PUBCHEM_IUPAC_CAS_NAME";

    private PubChemConst(){}
}
