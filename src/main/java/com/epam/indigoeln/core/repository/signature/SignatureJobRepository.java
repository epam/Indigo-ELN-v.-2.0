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
package com.epam.indigoeln.core.repository.signature;

import com.epam.indigoeln.core.model.ExperimentStatus;
import com.epam.indigoeln.core.model.SignatureJob;
import com.epam.indigoeln.core.util.WebSocketUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class SignatureJobRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public SignatureJob save(SignatureJob signatureJob) {
        mongoTemplate.save(signatureJob);
        return signatureJob;
    }

    public SignatureJob findOneForCheck() {
        return mongoTemplate.findAndModify(
                Query.query(Criteria.where("experimentStatus").is(ExperimentStatus.SUBMITTED)
                .and("handledBy").is(WebSocketUtil.getHostName())),
                Update.update("experimentStatus", ExperimentStatus.IN_CHECK),
                SignatureJob.class);
    }
}
