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
package com.epam.indigoeln.core.repository.registration;

import com.epam.indigoeln.core.model.RegistrationJob;
import com.epam.indigoeln.core.util.WebSocketUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class RegistrationJobRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public RegistrationJob save(RegistrationJob registrationJob) {
        mongoTemplate.save(registrationJob);
        return registrationJob;
    }

    public RegistrationJob findOneForCheck() {
        return mongoTemplate.findAndModify(
                Query.query(Criteria.where("registrationStatus").is(RegistrationStatus.Status.IN_PROGRESS.toString())
                        .and("handledBy").is(WebSocketUtil.getHostName())),
                Update.update("registrationStatus", RegistrationStatus.Status.IN_CHECK.toString()),
                RegistrationJob.class);
    }
}
