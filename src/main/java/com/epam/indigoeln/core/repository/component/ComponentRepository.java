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
package com.epam.indigoeln.core.repository.component;

import com.epam.indigoeln.core.model.Component;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ComponentRepository extends MongoRepository<Component, String> {

    @Query("{ 'name' :'productBatchSummary', 'content.batches' : {$elemMatch : {'registrationJobId' : ?0}}}")
    List<Component> findBatchSummariesByRegistrationJobId(String jobId);

    @Query("{ 'name' :'productBatchSummary', 'content.batches' : {$elemMatch : {'fullNbkBatch' : {$in : ?0}}}}")
    List<Component> findBatchSummariesByFullBatchNumbers(List<String> fullNbkBatchNumbers);

    @Query(value = "{'id' : { $in : ?0}}", delete = true)
    void deleteAllById(List<String> componentIds);

}
