package com.epam.indigoeln.core.service.dictionary;

import com.epam.indigoeln.core.model.Dictionary;
import com.epam.indigoeln.core.repository.dictionary.DictionaryRepository;
import com.epam.indigoeln.core.service.exception.EntityNotFoundException;
import com.epam.indigoeln.web.rest.dto.DictionaryDTO;
import com.epam.indigoeln.web.rest.util.CustomDtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DictionaryService {

    @Autowired
    DictionaryRepository dictionaryRepository;

    @Autowired
    private CustomDtoMapper dtoMapper;

    public Optional<DictionaryDTO> getDictionaryById(String id) {
        return Optional.ofNullable(dictionaryRepository.findOne(id)).map(DictionaryDTO::new);
    }

    public DictionaryDTO createDictionary(DictionaryDTO dictionaryDTO) {
        Dictionary dictionary = dtoMapper.convertFromDTO(dictionaryDTO);
        Dictionary savedDictionary = dictionaryRepository.save(dictionary);
        return new DictionaryDTO(savedDictionary);
    }

    public DictionaryDTO updateDictionary(DictionaryDTO dictionaryDTO) {

        Dictionary dictionary = Optional.ofNullable(dictionaryRepository.findOne(dictionaryDTO.getId())).
                orElseThrow(() -> new EntityNotFoundException("Dictionary with id does not exists", dictionaryDTO.getId()));

        dictionary.setName(dictionaryDTO.getName());
        dictionary.setDescription(dictionaryDTO.getDescription());
        dictionary.setWords(dictionaryDTO.getWords());

        Dictionary savedDictionary = dictionaryRepository.save(dictionary);
        return new DictionaryDTO(savedDictionary);
    }

    public void deleteDictionary(String dictionaryId) {
        dictionaryRepository.delete(dictionaryId);
    }

    public Page<DictionaryDTO> getAllDictionaries(Pageable pageable, String search) {
        return dictionaryRepository.findByNameContainingIgnoreCase(search, pageable).map(DictionaryDTO::new);
    }


}
