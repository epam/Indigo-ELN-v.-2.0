package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.ACLEntry;
import com.epam.indigoeln.eln.entity.BaseACLEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.model.ACLEntryDTO;
import com.epam.indigoeln.eln.model.UserRef;
import one.util.streamex.StreamEx;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;

public abstract class AbstractMapper {

    public UserRef userRef(UserEntity user) {
        return new UserRef(user.getId(), user.getDisplayName());
    }

    protected abstract ACLEntryDTO convertACL(ACLEntry entry);
    public abstract List<ACLEntryDTO> convertACLList(ACLEntry[] entries);

    @Mapping(target = "userId", expression = "java(entity.getUser().getId())")
    @Mapping(target = "displayName", expression = "java(entity.getUser().getDisplayName())")
    protected abstract ACLEntryDTO convertACL(BaseACLEntity entity);

    public List<ACLEntryDTO> convertACLMap(Map<UserEntity, ? extends BaseACLEntity> entities) {
        return StreamEx.ofValues(entities)
                .sortedBy(BaseACLEntity::getLevel)
                .map(this::convertACL)
                .toList();
    }
}
