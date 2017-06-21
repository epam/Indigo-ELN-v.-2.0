package com.epam.indigoeln.core.service.search.impl.pubchem.dto;

public class Structure {

    boolean entered = false;
    byte[]  image;
    String molfile;
    String structureId;
    String structureType = "molecule";

    public boolean isEntered() {
        return entered;
    }

    public void setEntered(boolean entered) {
        this.entered = entered;
    }

    public  byte[]  getImage() {
        return image;
    }

    public void setImage(byte[]  image) {
        this.image = image;
    }

    public String getStructureId() {
        return structureId;
    }

    public void setStructureId(String structureId) {
        this.structureId = structureId;
    }

    public String getStructureType() {
        return structureType;
    }

    public void setStructureType(String structureType) {
        this.structureType = structureType;
    }

    public String getMolfile() {
        return molfile;
    }

    public void setMolfile(String molfile) {
        this.molfile = molfile;
    }
}
