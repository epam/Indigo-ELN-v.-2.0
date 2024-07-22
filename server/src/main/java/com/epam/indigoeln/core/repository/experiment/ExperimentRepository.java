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
package com.epam.indigoeln.core.repository.experiment;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.ExperimentStatus;
import com.epam.indigoeln.core.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public interface ExperimentRepository extends MongoRepository<Experiment, String> {

    Collection<Experiment> findByAuthor(User author);

    Collection<Experiment> findByAuthorOrSubmittedBy(User author, User submittedBy);

    @Query("{'fileIds': ?0}")
    Experiment findByFileId(String fileId);

    Stream<Experiment> findByAuthorAndStatusAndCreationDateAfter(User user,
                                                                 ExperimentStatus status, ZonedDateTime creationTime);

    Stream<Experiment> findByDocumentIdIn(Collection<String> documentsIds);

    @Query(value = "{'experimentFullName' : { $regex: '^?0.*', $options: 'i' }}",
            fields = "{id:1, experimentFullName:1}")
    List<Experiment> findExperimentsByFullNameStartingWith(String experimentFullName, Pageable pageable);

    @Query(value = "{'experimentFullName' : { $regex: '^?0.*', $options: 'i' }, " +
            "'accessList' : { $elemMatch : {'user'  : {$ref : '" + User.COLLECTION_NAME +
            "', $id : ?1}, 'permissions' : ?2}}}", fields = "{id:1, experimentFullName:1}")
    List<Experiment> findExperimentsByFullNameStartingWithAndHasAccess(String experimentFullName,
                                                                       String userId, String permission,
                                                                       Pageable pageable);

    List<Experiment> findExperimentsByIdIn(Collection<String> experimentIds);
}