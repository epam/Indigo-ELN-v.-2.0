package com.epam.indigoeln.core.model;

import com.google.common.base.Objects;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * A dictionary model
 */
@Document(collection = Dictionary.COLLECTION_NAME)
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class Dictionary implements Serializable, Persistable<String> {

    public static final String COLLECTION_NAME = "dictionary";
    private static final long serialVersionUID = -3170256796584556733L;
    @Id
    private String id;

    @NotEmpty
    @Field("name")
    private String name;

    @Field("description")
    private String description;

    @Field("words")
    private Set<Word> words = new HashSet<>();

    @Field("accessList")
    private Set<UserPermission> accessList = new HashSet<>();

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public Set<UserPermission> getAccessList() {
        return accessList;
    }

    public void setAccessList(Set<UserPermission> accessList) {
        this.accessList = accessList;
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
        if (this == o) {
            return true;
        }
        if (!(o instanceof Dictionary)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        Dictionary dict = (Dictionary) o;
        return Objects.equal(description, dict.description) &&
                Objects.equal(words, dict.words);
    }

    @Override
    public boolean isNew() {
        return id == null;
    }
}
