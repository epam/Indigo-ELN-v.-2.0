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

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@ToString
@EqualsAndHashCode
@Document(collection = SequenceId.COLLECTION_NAME)
public class SequenceId implements Serializable {

    public static final String COLLECTION_NAME = "sequence_id";
    private static final long serialVersionUID = 2661973943428932165L;

    @Id
    private String id;

    @NotNull
    private Long sequence;

    @Version
    private Long version;

    private List<SequenceId> children;


    public SequenceId() {
        super();
    }

    public SequenceId(Long sequence) {
        this.sequence = sequence;
    }

    public SequenceId(String id, Long sequence) {
        this.id = id;
        this.sequence = sequence;
    }

    public String getId() {
        return id;
    }

    public Long getSequence() {
        return sequence;
    }

    public List<SequenceId> getChildren() {
        return children;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public void setChildren(List<SequenceId> children) {
        this.children = children;
    }
}


