package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.DictionaryItemEntity;
import com.epam.indigoeln.eln.entity.SaltCodeEntity;
import com.epam.indigoeln.eln.model.Dictionary;
import com.epam.indigoeln.eln.model.DictionaryItemDTO;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.model.DictionaryItemRequest;
import com.epam.indigoeln.reaction.model.SaltCodeRef;
import org.mapstruct.*;

import java.util.Collection;
import java.util.List;


@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueCheckStrategy =  NullValueCheckStrategy.ALWAYS)
public abstract class DictionaryMapper extends AbstractMapper {

    public abstract List<DictionaryItemDTO> dictionaryToDTOList(Collection<? extends DictionaryItemEntity> entities);

    public abstract List<DictionaryItemRef> dictionaryToRefList(Collection<? extends DictionaryItemEntity> entities);

    @IgnoreBaseFields
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "ordinal", ignore = true)
    public abstract DictionaryItemEntity dictionaryToEntity(DictionaryItemRequest request, Dictionary dictionary);

    public abstract SaltCodeRef saltCodeToRef(SaltCodeEntity entity);
    public abstract List<DictionaryItemRef> saltCodeToRefList(Collection<SaltCodeEntity> entities);
}
