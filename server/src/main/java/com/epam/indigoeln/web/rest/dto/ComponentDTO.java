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
package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.Component;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.bson.Document;

@JsonTypeName("Component")
public class ComponentDTO {

    private String id;
    private String name;
    private Document content;

    public ComponentDTO() {
        super();
    }

    public ComponentDTO(Component component) {
        if (component != null) {
            this.id = component.getId();
            this.content = component.getContent();
            this.name = component.getName();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Document getContent() {
        return content;
    }

    public void setContent(Document content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "ComponentDTO{"
                + "id='" + id + '\''
                + ", content=" + content
                + '}';
    }
}
