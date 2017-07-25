package com.epam.indigoeln.core.repository.notebook;

import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.User;

import com.mongodb.DBRef;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static com.epam.indigoeln.core.repository.RepositoryConstants.*;

public interface NotebookRepository extends MongoRepository<Notebook, String> {

    @Query("{'experiments': {'$ref': '" + Experiment.COLLECTION_NAME + "', '$id': ?0}}")
    Notebook findByExperimentId(String experimentId);

    @Query("{'experiments': { $in : ?0}}")
    Stream<Notebook> findByExperimentsIds(Collection<DBRef> experimentsIds);

    @Query(value = "{'accessList' : { $elemMatch : {'user'  : {$ref : '" + User.COLLECTION_NAME + "', $id : ?0}, 'permissions' : { $in : ?1}}}}")
    List<Notebook> findByUserIdAndPermissions(String userId, List<String> permissions);

    @Query(value = "{'accessList' : { $elemMatch : {'user'  : {$ref : '" + User.COLLECTION_NAME + "', $id : ?0}, 'permissions' : { $in : ?1}}}}",
            fields = ENTITY_SHORT_FIELDS_SET)
    List<Notebook> findByUserIdAndPermissionsShort(String userId, List<String> permissions);

    @Query(value = "{}",  fields = ENTITY_SHORT_FIELDS_SET)
    List<Notebook> findAllIgnoreChildren();
}