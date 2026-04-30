package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.DictionaryEntity;
import com.epam.indigoeln.eln.entity.DictionaryItemEntity;
import com.epam.indigoeln.eln.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

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
        return new DictionaryItemRef(entity.getId(), entity.getName(), entity.getActive(), entity.getDeleted(), entity.getDictionary().getId());
    }
    public abstract List<DictionaryItemRef> itemToRefList(Collection<DictionaryItemEntity> entities);

    @IgnoreBaseFields
    @Mapping(target = "ordinal", ignore = true)
    @Mapping(target = "dictionary", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "details", ignore = true)
    public abstract DictionaryItemEntity itemToEntity(DictionaryItemRequest request);
}
