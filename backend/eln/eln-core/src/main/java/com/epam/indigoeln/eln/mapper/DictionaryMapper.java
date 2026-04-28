package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.DictionaryEntity;
import com.epam.indigoeln.eln.entity.DictionaryItemEntity;
import com.epam.indigoeln.eln.entity.SaltCodeEntity;
import com.epam.indigoeln.eln.entity.SaltCodeInfo;
import com.epam.indigoeln.eln.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueCheckStrategy =  NullValueCheckStrategy.ALWAYS)
public abstract class DictionaryMapper extends AbstractMapper {

    @IgnoreBaseFields
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "deleted", constant = "false")
    public abstract DictionaryEntity requestToEntity(DictionaryRequest request);

    public abstract DictionaryDTO dictionaryToDTO(DictionaryEntity entity);

    public abstract List<DictionaryItemDTO> itemToDTOList(Collection<DictionaryItemEntity> entities);

    public DictionaryItemRef itemToRef(DictionaryItemEntity entity) {
        return new UserDictionaryItemRef(entity.getId(), entity.getName());
    }
    public abstract List<DictionaryItemRef> itemToRefList(Collection<DictionaryItemEntity> entities);

    @IgnoreBaseFields
    @Mapping(target = "ordinal", ignore = true)
    @Mapping(target = "dictionary", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "deleted", constant = "false")
    public abstract DictionaryItemEntity itemToEntity(DictionaryItemRequest request);

    public abstract SaltCodeInfo saltCodeToInfo(SaltCodeEntity entity);

    public List<DictionaryItemRef> saltCodeToRefList(Collection<SaltCodeEntity> entities) {
        List<DictionaryItemRef> result = new ArrayList<>(entities.size());
        for (SaltCodeEntity entity : entities) {
            result.add(saltCodeToInfo(entity));
        }
        return result;
    }
}
