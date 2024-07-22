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
package com.epam.indigoeln.core.repository.project;

import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.model.User;
import com.mongodb.DBRef;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Collection;
import java.util.List;

import static com.epam.indigoeln.core.repository.RepositoryConstants.ENTITY_SHORT_FIELDS_SET;

public interface ProjectRepository extends MongoRepository<Project, String> {

    @Query("{'accessList.user': {'$ref': '" + User.COLLECTION_NAME + "', '$id': ?0}}")
    Collection<Project> findByUserId(String userId);

    @Query("{'notebooks': {'$ref': '" + Notebook.COLLECTION_NAME + "', '$id': ?0}}")
    Project findByNotebookId(String notebookId);

    @Query("{'notebooks': { $in : ?0}}")
    Collection<Project> findByNotebookIds(Collection<DBRef> notebookIds);

    @Query("{'fileIds': ?0}")
    Project findByFileId(String fileId);


    @Query(value = "{'accessList' : { $elemMatch : {'user'  : {$ref : '"
            + User.COLLECTION_NAME + "', $id : ?0}, 'permissions' : { $in : ?1}}}}",
            fields = ENTITY_SHORT_FIELDS_SET)
    List<Project> findByUserIdAndPermissions(String userId, List<String> permissions);

    @Query(value = "{}", fields = ENTITY_SHORT_FIELDS_SET)
    List<Project> findAllIgnoreChildren();
}
