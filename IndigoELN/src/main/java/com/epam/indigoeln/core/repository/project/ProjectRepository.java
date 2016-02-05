package com.epam.indigoeln.core.repository.project;

import com.epam.indigoeln.core.model.Project;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Collection;

public interface ProjectRepository extends MongoRepository<Project, String> {

    @Query("{'accessList.user.$id': ?0}")
    Collection<Project> findByUserId(String userId);

    @Query("{'notebooks.$id': ?0}")
    Project findByNotebookId(String notebookId);

    @Query("{'fileIds': ?0}")
    Project findByFileId(String fileId);
}
