package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.ACLEntry;
import com.epam.indigoeln.eln.model.ACLDetailsEntryDTO;
import com.epam.indigoeln.eln.model.ACLEntryDTO;
import com.epam.indigoeln.eln.model.ExperimentStatus;

import java.util.List;
import java.util.Map;

@SuppressWarnings("MapperOrMapperConfigMissing")
public abstract class AbstractMapper {

    protected abstract ACLEntryDTO convertACL(ACLEntry entry);
    public abstract List<ACLEntryDTO> convertACLList(ACLEntry[] entries);

    protected abstract ACLDetailsEntryDTO convertDetailsACL(ACLEntry entry);
    public abstract List<ACLDetailsEntryDTO> convertDetailsACLList(ACLEntry[] entry);

    protected Integer convertMapToTotalCount(Map<ExperimentStatus, Integer> map) {
        return map.values().stream().mapToInt(Integer::intValue).sum();
    }
}
