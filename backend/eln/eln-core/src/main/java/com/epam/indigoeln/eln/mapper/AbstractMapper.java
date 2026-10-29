package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.common.model.UserRef;
import com.epam.indigoeln.eln.entity.ACLEntry;
import com.epam.indigoeln.eln.entity.DictionaryItemEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.entity.UserInfo;
import com.epam.indigoeln.eln.entity.WithACL;
import com.epam.indigoeln.eln.model.ACLEntryDTO;
import com.epam.indigoeln.eln.model.AccessLevel;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.model.ExperimentStatus;
import com.epam.indigoeln.eln.service.DictionaryService;
import com.epam.indigoeln.eln.service.UserService;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class AbstractMapper {

    private static final int SHORT_ACL_SIZE = 3;

    @Inject
    DictionaryService dictionaryService;
    @Inject
    UserService userService;

    protected abstract ACLEntryDTO convertACL(ACLEntry entry);
    public abstract List<ACLEntryDTO> convertACLList(List<ACLEntry> entries);

    /**
     * Sets the short ACL shown in lists and the total ACL size, computing the effective ACL once.
     */
    protected void mapShortACL(WithACL<?, ?> entity, Consumer<List<ACLEntryDTO>> aclSetter, Consumer<Integer> aclCountSetter) {
        List<ACLEntry> acl = entity.getEffectiveACL();
        aclSetter.accept(convertACLList(acl.stream()
                .filter(e -> e.getLevel() != AccessLevel.IMPLICIT_VIEW)
                .limit(SHORT_ACL_SIZE)
                .toList()));
        aclCountSetter.accept(acl.size());
    }

    protected Integer convertMapToTotalCount(Map<ExperimentStatus, Integer> map) {
        return map.values().stream().mapToInt(Integer::intValue).sum();
    }

    @Nullable
    protected <T extends DictionaryItemRef> T convertDictionaryItemRef(@Nullable DictionaryItemEntity entity) {
        return dictionaryService.get(entity);
    }

    public UserRef convertUserRef(UserEntity entity) {
        //noinspection ConstantValue
        return entity != null ? entity.toRef() : null;
    }

    public UserInfo convertUserInfo(UserEntity entity) {
        //noinspection ConstantValue
        return entity != null ? entity.toInfo() : null;
    }
}
