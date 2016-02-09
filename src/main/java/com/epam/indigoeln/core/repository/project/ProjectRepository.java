package com.epam.indigoeln.core.repository.project;

import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Collection;

public interface ProjectRepository extends MongoRepository<Project, String> {

    @Query("{'accessList.user': {'$ref': '" + User.COLLECTION_NAME + "', '$id': ?0}}")
    Collection<Project> findByUserId(String userId);

    @Query("{'notebooks': {'$ref': '" + Notebook.COLLECTION_NAME + "', '$id': ?0}}")
    Project findByNotebookId(String notebookId);

    @Query("{'fileIds': ?0}")
    Project findByFileId(String fileId);
}
