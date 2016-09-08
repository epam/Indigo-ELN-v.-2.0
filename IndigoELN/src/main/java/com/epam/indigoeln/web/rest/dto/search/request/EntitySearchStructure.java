package com.epam.indigoeln.web.rest.dto.search.request;

import com.epam.indigoeln.core.repository.search.entity.StructureSearchType;

public class EntitySearchStructure {

    private String name;
    private String searchMode;
    private float similarity;
    private String molfile;
    private String formula;
    private Type type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSearchMode() {
        return searchMode;
    }

    public void setSearchMode(String searchMode) {
        this.searchMode = searchMode;
    }

    public float getSimilarity() {
        return similarity;
    }

    public void setSimilarity(float similarity) {
        this.similarity = similarity;
    }

    public String getMolfile() {
        return molfile;
    }

    public void setMolfile(String molfile) {
        this.molfile = molfile;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public static class Type {

        private StructureSearchType name;

        public StructureSearchType getName() {
            return name;
        }

        public void setName(StructureSearchType name) {
            this.name = name;
        }
    }
}
