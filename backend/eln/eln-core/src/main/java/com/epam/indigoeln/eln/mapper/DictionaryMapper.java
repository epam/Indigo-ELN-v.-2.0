package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.DictionaryEntity;
import com.epam.indigoeln.eln.entity.DictionaryItemEntity;
import com.epam.indigoeln.eln.entity.SaltCodeEntity;
import com.epam.indigoeln.eln.model.DictionaryDTO;
import com.epam.indigoeln.eln.model.DictionaryItemDTO;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.model.DictionaryItemRequest;
import com.epam.indigoeln.reaction.model.SaltCodeRef;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.Collection;
import java.util.List;


@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueCheckStrategy =  NullValueCheckStrategy.ALWAYS)
public abstract class DictionaryMapper extends AbstractMapper {

    public abstract DictionaryDTO dictionaryToDTO(DictionaryEntity entity);

    public abstract List<DictionaryItemDTO> itemToDTOList(Collection<DictionaryItemEntity> entities);

    public abstract DictionaryItemRef itemToRef(DictionaryItemEntity entities);
    public abstract List<DictionaryItemRef> itemToRefList(Collection<DictionaryItemEntity> entities);

    @IgnoreBaseFields
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "ordinal", ignore = true)
    @Mapping(target = "dictionary", ignore = true)
    public abstract DictionaryItemEntity itemToEntity(DictionaryItemRequest request);

    public abstract SaltCodeRef saltCodeToRef(SaltCodeEntity entity);
    public abstract List<DictionaryItemRef> saltCodeToRefList(Collection<SaltCodeEntity> entities);
}
