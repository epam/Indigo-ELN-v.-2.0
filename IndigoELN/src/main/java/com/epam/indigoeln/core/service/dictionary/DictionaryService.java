package com.epam.indigoeln.core.service.dictionary;

import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.core.repository.dictionary.DictionaryRepository;
import com.epam.indigoeln.core.repository.notebook.NotebookRepository;
import com.epam.indigoeln.core.service.exception.EntityNotFoundException;
import com.epam.indigoeln.core.util.SequenceIdUtil;
import com.epam.indigoeln.web.rest.dto.DictionaryDTO;
import com.epam.indigoeln.web.rest.dto.ExperimentDictionaryDTO;
import com.epam.indigoeln.web.rest.util.CustomDtoMapper;
import com.epam.indigoeln.web.rest.util.PermissionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class DictionaryService {

    @Autowired
    private DictionaryRepository dictionaryRepository;

    @Autowired
    private NotebookRepository notebookRepository;

    @Autowired
    private CustomDtoMapper dtoMapper;

    public Optional<DictionaryDTO> getDictionaryById(String id) {
        return Optional.ofNullable(dictionaryRepository.findOne(id)).map(DictionaryDTO::new);
    }

    public Optional<DictionaryDTO> getDictionaryByName(String name) {
        return Optional.ofNullable(dictionaryRepository.findByName(name)).map(DictionaryDTO::new);
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

    public ExperimentDictionaryDTO getExperiments(User user) {

        final boolean contentEditor = PermissionUtil.isContentEditor(user);
        List<Notebook> notebooks = contentEditor ?
                notebookRepository.findAll() :
                notebookRepository.findByUserIdAndPermissions(user.getId(), Collections.singletonList(UserPermission.READ_ENTITY));
        AtomicInteger counter = new AtomicInteger(0);
        final Set<ExperimentDictionaryDTO.ExperimentDictionaryItemDTO> experiments = notebooks.stream().flatMap(
                n -> n.getExperiments().stream().filter(
                        e -> contentEditor || PermissionUtil.hasPermissions(user.getId(), e.getAccessList(), UserPermission.READ_ENTITY)
                ).map(e -> {
                    ExperimentDictionaryDTO.ExperimentDictionaryItemDTO experiment = new ExperimentDictionaryDTO.ExperimentDictionaryItemDTO();
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
