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
package com.epam.indigoeln.core.service.print.itext2.model.project;

import com.epam.indigoeln.core.service.print.itext2.model.common.SectionModel;

/**
 * Implementation of SectionModel for project summary.
 */
public class ProjectSummaryModel implements SectionModel {
    private String keywords;
    private String literature;

    public ProjectSummaryModel(String keywords, String literature) {
        this.keywords = keywords;
        this.literature = literature;
    }

    public String getKeywords() {
        return keywords;
    }

    public String getLiterature() {
        return literature;
    }
}
