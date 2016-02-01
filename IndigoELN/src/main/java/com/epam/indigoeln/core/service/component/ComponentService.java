package com.epam.indigoeln.core.service.component;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import com.epam.indigoeln.core.model.Component;
import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.UserPermission;
import com.epam.indigoeln.core.repository.experiment.ExperimentRepository;
import com.epam.indigoeln.core.security.AuthoritiesConstants;
import com.epam.indigoeln.core.service.bingodb.BingoDbIntegrationService;
import com.epam.indigoeln.core.service.user.UserService;
import com.epam.indigoeln.core.util.SequenceNumberGenerationUtil;
import com.epam.indigoeln.web.rest.dto.ComponentDTO;
import com.epam.indigoeln.web.rest.util.CustomDtoMapper;
import com.epam.indigoeln.web.rest.util.PermissionUtil;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ComponentService {

    @Autowired
    private ExperimentRepository experimentRepository;

    @Autowired
    private BingoDbIntegrationService bingoDbService;

    @Autowired
    private UserService userService;

    @Autowired
    CustomDtoMapper dtoMapper;

    /**
     * Create new component of experiment with specified experimentId
     * Experiment components list will be sorted by component number (in natural order) before saving
     *
     * If component id is not specified, new value will be generated
     * If component number is not specified, new value will be generated
     *
     * Component number is unique for single experiment
     * Component id is unique for all components in the system
     *
     * @param experimentId id of experiment
     * @param componentDTO component for save
     * @return saved Component
     */
    @Secured(AuthoritiesConstants.EXPERIMENT_CREATOR)
    public Optional<ComponentDTO> createComponent(String experimentId, ComponentDTO componentDTO) {
        Experiment experiment = experimentRepository.findOne(experimentId);
        if(experiment == null) return Optional.empty();

        checkPermissions(experiment, UserPermission.UPDATE_ENTITY);
        experiment.setComponents(Optional.ofNullable(experiment.getComponents()).orElse(new ArrayList<>()));

        if(getComponentById(componentDTO.getId(), experiment).isPresent()){
            throw new ValidationException("Component with the same id already exists");
        }

        Component componentForSave = convertFromDTO(componentDTO, experiment, null);
        if(componentDTO.getMolfile() != null){ //save new item to BingoDb if molfile is not empty
            componentForSave.setBingoDbId(bingoDbService.addMolecule(componentDTO.getMolfile()));
        }

        experiment.getComponents().add(validateComponentNumber(componentForSave, experiment.getComponents()));
        return saveComponent(componentForSave, experiment);
    }

    /**
     * Update existing component of experiment with specified experimentId
     * Experiment components list will be sorted by component number (in natural order) before saving
     *
     * Component id and component Number should be specified
     *
     * Component number is unique for single experiment
     * Component id is unique for all components in the system
     *
     * @param experimentId id of experiment
     * @param componentDTO component for save
     * @return saved Component
     */
    @Secured(AuthoritiesConstants.EXPERIMENT_CREATOR)
    public Optional<ComponentDTO> updateComponent(String experimentId, ComponentDTO componentDTO) {
        Experiment experiment = experimentRepository.findOne(experimentId);
        if(experiment == null) return Optional.empty();

        checkPermissions(experiment, UserPermission.UPDATE_ENTITY);

        Optional<Component> optionalComponent = getComponentById(componentDTO.getId(), experiment);
        return optionalComponent.map(component -> updateComponentInternal(componentDTO, experiment, component.getBingoDbId())).
                    orElseThrow(() -> new ValidationException("Component with the same id does not exist"));
    }

    private Optional<ComponentDTO> updateComponentInternal(ComponentDTO componentDTO, Experiment experiment, Integer bingoDbId) {
        Component componentForSave = convertFromDTO(componentDTO, experiment, bingoDbId);
        if(componentDTO.getMolfile() != null) { // if molfile is not empty update or create new item in BingoDB
            componentForSave.setBingoDbId((componentForSave.getBingoDbId() == null) ?
                    bingoDbService.addMolecule(componentDTO.getMolfile()) : // add new molecule
                    bingoDbService.updateMolecule(componentForSave.getBingoDbId(), componentDTO.getMolfile())); //update existing molecule
        } else { //otherwise, delete from BingoDB if any BingoDB item was assigned
            Optional.ofNullable(componentForSave.getBingoDbId()).ifPresent(bingoDbService::deleteMolecule);
            componentForSave.setBingoDbId(null);
        }
        return saveComponent(componentForSave, experiment);
    }

    /**
     * Delete component by id and experiment id
     * If any BingoDB item is assigned it will be also deleted
     *
     * @param experimentId experiment id
     * @param componentId component id
     */
    @Secured(AuthoritiesConstants.EXPERIMENT_CREATOR)
    public void deleteComponent(String experimentId, String componentId) {
        Optional.ofNullable(experimentRepository.findOne(experimentId)).ifPresent(experiment ->
            {
                checkPermissions(experiment, UserPermission.UPDATE_ENTITY);
                Optional <Component> optionalComponent = getComponentById(componentId, experiment);
                optionalComponent.ifPresent(component -> Optional.ofNullable(component.getBingoDbId()).ifPresent(bingoDbService::deleteMolecule));
                experiment.getComponents().removeIf(component -> componentId.equals(component.getId()));
                experimentRepository.save(experiment);
            }
        );
    }

    /**
     * Get component details by experiment id and component id
     * @param experimentId experiment id
     * @param componentId component id
     * @return result of operation
     */
    @Secured(AuthoritiesConstants.EXPERIMENT_READER)
    public Optional<ComponentDTO> getComponent(String experimentId, String componentId) {
        Experiment experiment = experimentRepository.findOne(experimentId);
        if(experiment == null) return Optional.empty();

        checkPermissions(experiment, UserPermission.READ_ENTITY);

        Optional<Component> optionalComponent = getComponentById(componentId, experiment);
        return  optionalComponent.map(this::createComponentDTOFromComponent);
    }

    /**
     * Get all components by experiment id
     * @param experimentId experiment id
     * @return list of experiment components enriched with molfile parameters
     */
    @Secured(AuthoritiesConstants.EXPERIMENT_READER)
    public Optional<List<ComponentDTO>> getAllExperimentComponents(String experimentId) {
        Experiment experiment = experimentRepository.findOne(experimentId);
        if(experiment == null) return Optional.empty();

        checkPermissions(experiment, UserPermission.READ_ENTITY);

        if(experiment.getComponents() == null) return Optional.of(new ArrayList<>());
        return Optional.of(experiment.getComponents().stream().map(this::createComponentDTOFromComponent).collect(Collectors.toList()));
    }

    /**
     * Save component to the database
     * @param componentForSave component for save
     * @param experiment parent experiment
     * @return saved component DTO
     */
    private Optional<ComponentDTO> saveComponent(Component componentForSave, Experiment experiment) {
        experiment.getComponents().removeIf(component -> component.getId().equals(componentForSave.getId()));
        experiment.getComponents().add(componentForSave);
        experiment.getComponents().sort(Comparator.comparing(Component::getComponentNumber));
        experimentRepository.save(experiment);
        return getComponent(experiment.getId(), componentForSave.getId());
    }

    /**
     *  Check,that no other components with the same number exists in current experiment
     * @param component component to validate
     * @param allComponents all existing components of experiment
     */
    private Component validateComponentNumber(Component component, List<Component> allComponents) {
        if(component.getComponentNumber() == null) {
            throw new ValidationException("The notebook component number is not specified");
        }

        if(allComponents.stream().anyMatch(b -> b.getComponentNumber().equals(component.getComponentNumber()) &&
                !b.getId().equals(component.getId()))) {
            throw new ValidationException(
                    String.format("The notebook component number '%s' already exists in the system", component.getComponentNumber())
            );
        }

        return component;
    }

    /**
     * Get DTO converted from component and enriched by molfile parameters
     * @param component component to be converted
     * @return component DTO
     */
    private ComponentDTO createComponentDTOFromComponent(Component component) {
        ComponentDTO componentDTO = new ComponentDTO(component);
        if(component.getBingoDbId() != null) {
            String molfile = bingoDbService.getMolecule(component.getBingoDbId());
            IndigoObject indigoObjectMolecule = new Indigo().loadMolecule(molfile);
            componentDTO.setMolfile(molfile);
            //componentDTO.setMolecularWeight(indigoObjectMolecule.molecularWeight());
            //componentDTO.setFormula(indigoObjectMolecule.grossFormula());
        }
        return componentDTO;
    }

    /**
     * Check user possibility to read or modify given experiment
     * @param experiment experiment
     * @param permission permission to check
     */
    private void checkPermissions(Experiment experiment, String permission) {
        if (!PermissionUtil.hasPermissions(userService.getUserWithAuthorities(),
                experiment.getAccessList(), permission)) {
            throw new AccessDeniedException("Current user doesn't have permissions " +
                    "to perform operation for experiment with id = " + experiment.getId());
        }
    }

    private Optional<Component> getComponentById(String id, Experiment experiment) {
        return experiment.getComponents().stream().filter(b -> b.getId().equals(id)).findFirst();
    }


    /**
     * Create new component item from DTO
     * If id or component Number is absent in given DTO it will be generated automatically
     *
     * @param componentDTO DTO
     * @param experiment experiment
     * @param bingoDbId bingo Db id
     *
     * @return component item filled from component DTO
     */
    private Component convertFromDTO(ComponentDTO componentDTO, Experiment experiment, Integer bingoDbId) {
        Component result = dtoMapper.convertFromDTO(componentDTO);
        result.setId(Optional.ofNullable(componentDTO.getId()).orElse(ObjectId.get().toHexString()));
        result.setBingoDbId(bingoDbId);

        List<String> allComponentNumbers = experiment.getComponents().stream().map(Component::getComponentNumber).collect(Collectors.toList());
        result.setComponentNumber(Optional.ofNullable(componentDTO.getComponentNumber())
                .orElse(SequenceNumberGenerationUtil.generateNextComponentNumber(allComponentNumbers)));
        return result;
    }
}
