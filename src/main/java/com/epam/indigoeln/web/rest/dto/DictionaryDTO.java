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
    private UserDTO author;

    private Set<UserPermissionDTO> accessList = new HashSet<>();

    DictionaryDTO() {
        super();
    }

    public DictionaryDTO(Dictionary dictionary) {
        this.id = dictionary.getId();
        this.words = dictionary.getWords();
        this.name = dictionary.getName();
        this.description = dictionary.getDescription();
        this.author = new UserDTO(dictionary.getAuthor());
        if (dictionary.getAccessList() != null) {
            this.accessList.addAll(dictionary.getAccessList()
                    .stream()
                    .map(UserPermissionDTO::new)
                    .collect(Collectors.toList()));
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

    public UserDTO getAuthor() {
        return author;
    }

    public void setAuthor(UserDTO author) {
        this.author = author;
    }
}
