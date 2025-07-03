package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.DictionaryEntity;
import com.epam.indigoeln.eln.entity.SaltCodeEntity;
import com.epam.indigoeln.eln.model.Dictionary;
import com.epam.indigoeln.eln.model.DictionaryItemDTO;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.model.DictionaryItemRequest;
import org.mapstruct.*;

import java.util.Collection;
import java.util.List;


@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueCheckStrategy =  NullValueCheckStrategy.ALWAYS)
public abstract class DictionaryMapper extends AbstractMapper {

    public abstract List<DictionaryItemDTO> dictionaryToDTOList(Collection<? extends DictionaryEntity> entities);

    public abstract List<DictionaryItemRef> dictionaryToRefList(Collection<? extends DictionaryEntity> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", expression = "java(request.getDeleted() != null ? request.getDeleted() : false)")
    public abstract DictionaryEntity dictionaryToEntity(DictionaryItemRequest request, Dictionary dictionary, Integer ordinal, @MappingTarget DictionaryEntity entity);

    public abstract List<DictionaryItemRef> saltCodeToRefList(Collection<SaltCodeEntity> entities);
}
