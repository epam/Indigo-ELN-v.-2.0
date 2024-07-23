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
package com.epam.indigoeln.web.rest.dto.search;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.util.SequenceIdUtil;
import lombok.Data;

@Data
public class EntitiesIdsDTO {
    private final String id;
    private final String notebookId;
    private final String projectId;
    private final String name;

    public EntitiesIdsDTO(Experiment experiment){
        this.id = SequenceIdUtil.extractShortId(experiment);
        this.notebookId = SequenceIdUtil.extractParentId(experiment);
        this.projectId = SequenceIdUtil.extractFirstId(experiment);
        this.name = experiment.getExperimentFullName();
    }
}
