package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.DictionaryEntity;
import com.epam.indigoeln.eln.entity.SaltCodeEntity;
import com.epam.indigoeln.eln.model.Dictionary;
import com.epam.indigoeln.eln.model.DictionaryDTO;
import com.epam.indigoeln.eln.model.DictionaryRef;
import com.epam.indigoeln.eln.model.DictionaryRequest;
import org.mapstruct.*;

import java.util.Collection;
import java.util.List;


@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueCheckStrategy =  NullValueCheckStrategy.ALWAYS)
public abstract class DictionaryMapper extends AbstractMapper {

    public abstract List<DictionaryDTO> dictionaryToDTOList(Collection<? extends DictionaryEntity> entities);

    public abstract List<DictionaryRef> dictionaryToRefList(Collection<? extends DictionaryEntity> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", expression = "java(request.getDeleted() != null ? request.getDeleted() : false)")
    public abstract DictionaryEntity dictionaryToEntity(DictionaryRequest request, Dictionary dictionary, Integer ordinal, @MappingTarget DictionaryEntity entity);

    public abstract List<DictionaryRef> saltCodeToRefList(Collection<SaltCodeEntity> entities);
}
