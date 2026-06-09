package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.ACLEntry;
import com.epam.indigoeln.eln.entity.DictionaryItemEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.entity.UserInfo;
import com.epam.indigoeln.eln.model.ACLEntryDTO;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.model.ExperimentStatus;
import com.epam.indigoeln.eln.service.DictionaryService;
import com.epam.indigoeln.eln.service.UserService;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

public abstract class AbstractMapper {

    @Inject
    DictionaryService dictionaryService;
    @Inject
    UserService userService;

    protected abstract ACLEntryDTO convertACL(ACLEntry entry);
    public abstract List<ACLEntryDTO> convertACLList(ACLEntry[] entries);

    protected Integer convertMapToTotalCount(Map<ExperimentStatus, Integer> map) {
        return map.values().stream().mapToInt(Integer::intValue).sum();
    }

    @Nullable
    protected <T extends DictionaryItemRef> T convertDictionaryItemRef(@Nullable DictionaryItemEntity entity) {
        return dictionaryService.get(entity);
    }

    @Nullable
    protected UserInfo convertUserInfo(@Nullable UserEntity entity) {
        return entity != null ? entity.toInfo() : null;
    }
}
