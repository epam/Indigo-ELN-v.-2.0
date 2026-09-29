package com.epam.indigoeln.eln.model;

public enum SampleSource {

    VIRTUAL, // input sample not linked to any real sample; output sample that is not yet registered
    SRS, // Sample Registration Service
    PUBCHEM, // PubChem
}
