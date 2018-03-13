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
package com.epam.indigoeln.core.service.dictionary;

import com.epam.indigoeln.core.model.Dictionary;
import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.model.UserPermission;
import com.epam.indigoeln.core.repository.dictionary.DictionaryRepository;
import com.epam.indigoeln.core.repository.notebook.NotebookRepository;
import com.epam.indigoeln.core.service.exception.EntityNotFoundException;
import com.epam.indigoeln.core.util.SequenceIdUtil;
import com.epam.indigoeln.core.util.SortedPageUtil;
import com.epam.indigoeln.web.rest.dto.DictionaryDTO;
import com.epam.indigoeln.web.rest.dto.ExperimentDictionaryDTO;
import com.epam.indigoeln.web.rest.util.CustomDtoMapper;
import com.epam.indigoeln.web.rest.util.PermissionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The DictionaryService provides a number od methods for
 * dictionary's data manipulation.
 *
 * @author Anton Pikhtin
 */
@Service
public class DictionaryService {

    @Autowired
    private DictionaryRepository dictionaryRepository;

    @Autowired
    private NotebookRepository notebookRepository;

    @Autowired
    private CustomDtoMapper dtoMapper;

    private static SortedPageUtil<Dictionary> dictionarySortedPageUtil;

    static {
        Map<String, Function<Dictionary, String>> functionMap = new HashMap<>();
        functionMap.put("name", Dictionary::getName);
        functionMap.put("description", Dictionary::getDescription);

        dictionarySortedPageUtil = new SortedPageUtil<>(functionMap);
    }

    /**
     * Returns dictionary by it's id.
     *
     * @param id Identifier of the dictionary
     * @return The dictionary by identifier
     */
    public Optional<DictionaryDTO> getDictionaryById(String id) {
        return Optional.ofNullable(dictionaryRepository.findOne(id)).map(DictionaryDTO::new);
    }

    /**
     * Returns dictionary by it's name.
     *
     * @param name Name of the dictionary
     * @return The dictionary by name
     */
    public Optional<DictionaryDTO> getDictionaryByName(String name) {
        return Optional.ofNullable(dictionaryRepository.findByName(name)).map(DictionaryDTO::new);
    }

    /**
     * Creates new dictionary.
     *
     * @param dictionaryDTO Dictionary to create
     * @return Created dictionary
     */
    public DictionaryDTO createDictionary(DictionaryDTO dictionaryDTO, User user) {
        Dictionary dictionary = dtoMapper.convertFromDTO(dictionaryDTO);
        dictionary.setAuthor(user);
        Dictionary savedDictionary = dictionaryRepository.save(dictionary);
        return new DictionaryDTO(savedDictionary);
    }

    /**
     * Updates dictionary.
     *
     * @param dictionaryDTO New dictionary for update
     * @return Updated dictionary
     */
    public DictionaryDTO updateDictionary(DictionaryDTO dictionaryDTO) {

        Dictionary dictionary = Optional.ofNullable(dictionaryRepository.findOne(dictionaryDTO.getId())).
                orElseThrow(() -> EntityNotFoundException.createWithDictionaryId(dictionaryDTO.getId()));

        dictionary.setName(dictionaryDTO.getName());
        dictionary.setDescription(dictionaryDTO.getDescription());
        dictionary.setWords(dictionaryDTO.getWords());

        Dictionary savedDictionary = dictionaryRepository.save(dictionary);
        return new DictionaryDTO(savedDictionary);
    }

    /**
     * Deletes dictionary.
     *
     * @param dictionaryId Identifier of the dictionary to delete
     */
    public void deleteDictionary(String dictionaryId) {
        dictionaryRepository.delete(dictionaryId);
    }

    /**
     * Returns all dictionaries.
     *
     * @return The list o dictionaries
     */
    public List<DictionaryDTO> getAllDictionaries() {
        return dictionaryRepository.findAll().stream().map(DictionaryDTO::new).collect(Collectors.toList());
    }

    /**
     * Returns all found dictionaries (with paging).
     *
     * @param search   Search string
     * @param pageable Pageable object which contains page and size
     * @return Page with all found dictionaries
     */
    public Page<DictionaryDTO> getAllDictionaries(String search, Pageable pageable) {
        return dictionarySortedPageUtil.getPage(
                dictionaryRepository.findByNameIgnoreCaseLikeOrDescriptionIgnoreCaseLike(search, search),
                pageable)
                .map(DictionaryDTO::new);
    }

    /**
     * Returns experiments dictionary.
     *
     * @param user User with authorities
     * @return Experiments dictionary
     */
    public ExperimentDictionaryDTO getExperiments(User user) {

        final boolean contentEditor = PermissionUtil.isContentEditor(user);
        List<Notebook> notebooks = contentEditor
                ? notebookRepository.findAll()
                : notebookRepository.findByUserIdAndPermissions(user.getId(),
                Collections.singletonList(UserPermission.READ_ENTITY));
        AtomicInteger counter = new AtomicInteger(0);
        final Set<ExperimentDictionaryDTO.ExperimentDictionaryItemDTO> experiments = notebooks.stream().flatMap(
                n -> n.getExperiments().stream().filter(
                        e -> contentEditor || PermissionUtil.hasPermissions(user.getId(), e.getAccessList(),
                                UserPermission.READ_ENTITY)
                ).map(e -> {
                    ExperimentDictionaryDTO.ExperimentDictionaryItemDTO experiment = new ExperimentDictionaryDTO
                            .ExperimentDictionaryItemDTO();
                    String name = n.getName() + "-" + e.getName();
                    if (e.getExperimentVersion() > 1 || !e.isLastVersion()) {
                        name += " v" + e.getExperimentVersion();
                    }
                    experiment.setName(name);
                    experiment.setRank(counter.incrementAndGet());

                    experiment.setId(SequenceIdUtil.extractShortId(e));
                    experiment.setNotebookId(SequenceIdUtil.extractShortId(n));
                    experiment.setProjectId(SequenceIdUtil.extractParentId(n));

                    return experiment;
                })
        ).collect(Collectors.toSet());
        return new ExperimentDictionaryDTO(experiments);
    }
}
