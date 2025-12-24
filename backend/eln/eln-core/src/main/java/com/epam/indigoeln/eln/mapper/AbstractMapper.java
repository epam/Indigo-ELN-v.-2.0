package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.ACLEntry;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.model.ACLDetailsEntryDTO;
import com.epam.indigoeln.eln.model.ACLEntryDTO;
import com.epam.indigoeln.eln.model.ExperimentStatus;
import com.epam.indigoeln.eln.model.UserRef;

import java.util.List;
import java.util.Map;

@SuppressWarnings("MapperOrMapperConfigMissing")
public abstract class AbstractMapper {

    public UserRef userRef(UserEntity user) {
        return new UserRef(user.getId(), user.getUsername(), user.getDisplayName());
    }

    protected abstract ACLEntryDTO convertACL(ACLEntry entry);
    public abstract List<ACLEntryDTO> convertACLList(ACLEntry[] entries);

    protected abstract ACLDetailsEntryDTO convertDetailsACL(ACLEntry entry);
    public abstract List<ACLDetailsEntryDTO> convertDetailsACLList(ACLEntry[] entry);

    protected Integer convertMapToTotalCount(Map<ExperimentStatus, Integer> map) {
        return map.values().stream().mapToInt(Integer::intValue).sum();
    }
}
