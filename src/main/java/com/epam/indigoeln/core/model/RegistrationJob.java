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

import com.epam.indigoeln.core.repository.registration.RegistrationStatus;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@ToString
@EqualsAndHashCode
@Document(collection = RegistrationJob.COLLECTION_NAME)
public class RegistrationJob implements Serializable {

    private static final long serialVersionUID = 5186246198191004066L;

    public static final String COLLECTION_NAME = "registration_job";

    @Id
    private String id;

    private RegistrationStatus.Status registrationStatus;
    private String registrationJobId;
    private String registrationRepositoryId;
    private String handledBy;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RegistrationStatus.Status getRegistrationStatus() {
        return registrationStatus;
    }

    public void setRegistrationStatus(RegistrationStatus.Status registrationStatus) {
        this.registrationStatus = registrationStatus;
    }

    public String getRegistrationJobId() {
        return registrationJobId;
    }

    public void setRegistrationJobId(String registrationJobId) {
        this.registrationJobId = registrationJobId;
    }

    public String getRegistrationRepositoryId() {
        return registrationRepositoryId;
    }

    public void setRegistrationRepositoryId(String registrationRepositoryId) {
        this.registrationRepositoryId = registrationRepositoryId;
    }

    public String getHandledBy() {
        return handledBy;
    }

    public void setHandledBy(String handledBy) {
        this.handledBy = handledBy;
    }
}
