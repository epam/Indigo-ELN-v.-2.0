package com.epam.indigoeln.web.rest.dto;


import com.epam.indigoeln.core.model.Dictionary;
import com.epam.indigoeln.core.model.Word;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class DictionaryDTO {

    private String id;
    private Set<Word> words;
    private String name;
    private String description;

    private Set<UserPermissionDTO> accessList = new HashSet<>();

    DictionaryDTO() {
        super();
    }

    public DictionaryDTO(Dictionary dictionary) {
        this.id = dictionary.getId();
        this.words = dictionary.getWords();
        this.name = dictionary.getName();
        this.description = dictionary.getDescription();
        if (dictionary.getAccessList() != null) {
            this.accessList.addAll(dictionary.getAccessList().stream().map(UserPermissionDTO::new).collect(Collectors.toList()));
        }
    }

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

    public Set<UserPermissionDTO> getAccessList() {
        return accessList;
    }

    public void setAccessList(Set<UserPermissionDTO> accessList) {
        this.accessList = accessList;
    }
}
