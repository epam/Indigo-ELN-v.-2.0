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
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@ToString
@EqualsAndHashCode
@Document(collection = AutosaveItem.COLLECTION_NAME)
public class AutosaveItem implements Serializable, Persistable<String> {

    public static final String COLLECTION_NAME = "autosave";
    private static final long serialVersionUID = 5299632261167812262L;

    @Id
    private String id;

    private User user;

    private BasicDBObject content;

    public AutosaveItem() {
    }

    public AutosaveItem(String id, User user, BasicDBObject content) {
        this.id = id;
        this.user = user;
        this.content = content;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BasicDBObject getContent() {
        return content;
    }

    public void setContent(BasicDBObject content) {
        this.content = content;
    }

    @Override
    public boolean isNew() {
        return true;
    }
}
