/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
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
