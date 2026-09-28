package com.epam.indigoeln.eln.entity;

import com.epam.indigoeln.eln.model.AccessLevel;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public interface WithACL<A extends BaseACLEntity, P extends WithACL<?, ?>> {

    UserEntity getCreatedBy();

    Map<UserEntity, A> getAclEntities();

    void insertACL(UserEntity user, AccessLevel access);

    ACLEntry[] getFullACL();
    void setFullACL(ACLEntry[] fullACL);

    void setShortACL(ACLEntry[] shortACL);

    @Nullable
    P getACLParent();
}
