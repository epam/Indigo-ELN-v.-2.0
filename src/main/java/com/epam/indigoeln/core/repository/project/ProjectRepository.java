package com.epam.indigoeln.core.repository.project;

import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.model.User;
import com.mongodb.DBRef;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Collection;
import java.util.List;
import static com.epam.indigoeln.core.repository.RepositoryConstants.*;

public interface ProjectRepository extends MongoRepository<Project, String> {

    @Query("{'accessList.user': {'$ref': '" + User.COLLECTION_NAME + "', '$id': ?0}}")
    Collection<Project> findByUserId(String userId);

    @Query("{'notebooks': {'$ref': '" + Notebook.COLLECTION_NAME + "', '$id': ?0}}")
    Project findByNotebookId(String notebookId);

    @Query("{'notebooks': { $in : ?0}}")
    Collection<Project> findByNotebookIds(Collection<DBRef> notebookIds);

    @Query("{'fileIds': ?0}")
    Project findByFileId(String fileId);


    @Query(value = "{'accessList' : { $elemMatch : {'user'  : {$ref : '" + User.COLLECTION_NAME + "', $id : ?0}, 'permissions' : { $in : ?1}}}}",
           fields = ENTITY_SHORT_FIELDS_SET)
    List<Project> findByUserIdAndPermissions(String userId, List<String> permissions);

    @Query(value = "{}",  fields = ENTITY_SHORT_FIELDS_SET)
    List<Project> findAllIgnoreChildren();
}
