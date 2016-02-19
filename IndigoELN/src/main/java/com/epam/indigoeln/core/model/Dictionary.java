package com.epam.indigoeln.core.model;

import com.epam.indigoeln.core.util.JsonUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Objects;
import com.mongodb.BasicDBList;
import org.json.JSONArray;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A dictionary model
 */
@Document(collection = Dictionary.COLLECTION_NAME)
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class Dictionary extends BasicModelObject {

    private static final long serialVersionUID = -3170256796584556733L;

    public static final String COLLECTION_NAME = "dictionary";

    @Field("description")
    private String description;

    @Field("words")
    private Set<Word> words = new HashSet<>();

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public Set<Word> getWords() {
        return words;
    }

    public void setWords(Set<Word> words) {
        this.words = words;
    }

    @Override
    public String toString() {
        return "Dictionary{" +
                "description='" + description + '\'' +
                "words='" + words + '\'' +
                "} " + super.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), description, words);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Dictionary)) return false;
        if (!super.equals(o)) return false;
        Dictionary dict = (Dictionary) o;
        return  Objects.equal(description, dict.description) &&
                Objects.equal(words, dict.words);
    }
}
