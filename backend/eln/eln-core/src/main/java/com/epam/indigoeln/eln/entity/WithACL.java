package com.epam.indigoeln.eln.entity;

import com.epam.indigoeln.eln.model.AccessLevel;

import java.util.Map;

public interface WithACL<A extends BaseACLEntity> {

    UserEntity getCreatedBy();

    Map<UserEntity, A> getAclEntities();

    void insertACL(UserEntity user, AccessLevel access, Boolean inherited);
}
