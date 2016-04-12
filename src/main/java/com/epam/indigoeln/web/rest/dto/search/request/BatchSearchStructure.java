package com.epam.indigoeln.web.rest.dto.search.request;

public class BatchSearchStructure {
    private String name;
    private String searchMode;
    private float  similarity;
    private String molfile;
    private String formula;

    public String getName() {
        return name;
    }

    public String getSearchMode() {
        return searchMode;
    }

    public float getSimilarity() {
        return similarity;
    }

    public String getMolfile() {
        return molfile;
    }

    public String getFormula() {
        return formula;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSearchMode(String searchMode) {
        this.searchMode = searchMode;
    }

    public void setSimilarity(float similarity) {
        this.similarity = similarity;
    }

    public void setMolfile(String molfile) {
        this.molfile = molfile;
    }

    public void setFormula(String formula) {
        this.formula =formula;
    }
}
