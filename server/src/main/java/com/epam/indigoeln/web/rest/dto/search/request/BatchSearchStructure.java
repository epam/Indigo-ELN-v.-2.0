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
package com.epam.indigoeln.web.rest.dto.search.request;

public class BatchSearchStructure {

    private String name;
    private String searchMode;
    private float similarity;
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
        this.formula = formula;
    }
}
