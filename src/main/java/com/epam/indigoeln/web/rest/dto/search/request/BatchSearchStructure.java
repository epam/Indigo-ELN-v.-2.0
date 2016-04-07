package com.epam.indigoeln.web.rest.dto.search.request;

import java.util.Optional;

public class BatchSearchStructure {
    private String name;
    private String searchMode;
    private float  similarity;
    private Optional<String> molfile;
    private Optional<String> formula;

    public String getName() {
        return name;
    }

    public String getSearchMode() {
        return searchMode;
    }

    public float getSimilarity() {
        return similarity;
    }

    public Optional<String> getMolfile() {
        return molfile;
    }

    public Optional<String> getFormula() {
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
        this.molfile = Optional.ofNullable(molfile);
    }

    public void setFormula(String formula) {
        this.formula = Optional.ofNullable(formula);
    }
}
