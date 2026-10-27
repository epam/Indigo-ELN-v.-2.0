package com.epam.indigoeln.eln.entity;

import com.epam.indigoeln.eln.model.AccessLevel;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.epam.indigoeln.eln.model.AccessLevel.AUTHOR;
import static com.epam.indigoeln.eln.model.AccessLevel.IMPLICIT_VIEW;

public interface WithACL<A extends BaseACLEntity, P extends WithACL<?, ?>> {

    int SHORT_ACL_SIZE = 3;

    UserEntity getCreatedBy();

    Map<UserEntity, A> getAclEntities();

    void insertACL(UserEntity user, AccessLevel access);

    @Nullable
    P getACLParent();

    /**
     * Effective ACL: author, then own entries, then entries inherited from parents, then own IMPLICIT_VIEW
     * entries (granted because of access to children). The first source that mentions a user wins.
     */
    default List<ACLEntry> getEffectiveACL() {
        Map<UserEntity, ACLEntry> users = new LinkedHashMap<>();
        UserEntity author = getCreatedBy();
        users.put(author, new ACLEntry(author.getId(), author.getDisplayName(), author.getUsername(), AUTHOR, false));
        getAclEntities().forEach((user, entry) -> {
            if (entry.getLevel() != IMPLICIT_VIEW) {
                users.putIfAbsent(user, new ACLEntry(user.getId(), user.getDisplayName(), user.getUsername(), entry.getLevel(), false));
            }
        });
        for (WithACL<?, ?> parent = getACLParent(); parent != null; parent = parent.getACLParent()) {
            parent.getAclEntities().forEach((user, entry) -> {
                if (entry.getLevel() != IMPLICIT_VIEW) {
                    users.putIfAbsent(user, new ACLEntry(user.getId(), user.getDisplayName(), user.getUsername(), entry.getLevel(), true));
                }
            });
        }
        getAclEntities().forEach((user, entry) ->
                users.putIfAbsent(user, new ACLEntry(user.getId(), user.getDisplayName(), user.getUsername(), IMPLICIT_VIEW, false))
        );
        return users.values().stream()
                .sorted(Comparator.comparing(ACLEntry::getLevel, Comparator.reverseOrder())
                        .thenComparing(ACLEntry::isInherited)
                        .thenComparing(ACLEntry::getDisplayName))
                .toList();
    }

    default List<ACLEntry> getShortACL() {
        return getEffectiveACL().stream()
                .filter(e -> e.getLevel() != IMPLICIT_VIEW)
                .limit(SHORT_ACL_SIZE)
                .toList();
    }

    default Integer getAclCount() {
        return getEffectiveACL().size();
    }
}
