package com.epam.indigoeln.core.repository.dictionary;

import com.epam.indigoeln.core.model.Dictionary;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DictionaryRepository extends MongoRepository<Dictionary, String> {

    Dictionary findByName(String name);

    List<Dictionary> findByNameIgnoreCaseLikeOrDescriptionIgnoreCaseLike(String nameLike, String descriptionLike);
}
