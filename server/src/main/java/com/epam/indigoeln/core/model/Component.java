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
package com.epam.indigoeln.core.model;

import com.mongodb.BasicDBObject;
import com.mongodb.DBRef;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * Component model object.
 */
@ToString
@EqualsAndHashCode
@Document(collection = Component.COLLECTION_NAME)
public class Component implements Serializable {

    public static final String COLLECTION_NAME = "component";

    private static final long serialVersionUID = -3522699714105273016L;

    @Id
    private String id;
    private String name;
    private BasicDBObject content;

    private DBRef experiment;

    @Version
    private Long version;

    public String getId() {
        return id;
    }

    public BasicDBObject getContent() {
        return content;
    }


    public void setId(String id) {
        this.id = id;
    }

    public void setContent(BasicDBObject content) {
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public DBRef getExperiment() {
        return experiment;
    }

    public void setExperiment(DBRef experiment) {
        this.experiment = experiment;
    }
}
