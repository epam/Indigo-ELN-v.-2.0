package com.epam.indigoeln.web.rest.dto;


import com.epam.indigoeln.core.model.Dictionary;
import com.epam.indigoeln.core.model.Word;

import java.util.Set;

public class DictionaryDTO extends BasicDTO {

    private static final long serialVersionUID = -492928653193551903L;

    private Set<Word> words;
    private String description;

    DictionaryDTO() {
    }

    public DictionaryDTO(Dictionary dictionary) {
        super(dictionary);
        this.setWords(dictionary.getWords());
        this.setDescription(dictionary.getDescription());
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
}
