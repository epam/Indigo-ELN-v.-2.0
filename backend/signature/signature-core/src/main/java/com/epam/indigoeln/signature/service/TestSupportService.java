package com.epam.indigoeln.signature.service;

import io.quarkus.runtime.configuration.ConfigUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Transactional
@ApplicationScoped
@SuppressWarnings("SqlWithoutWhere")
public class TestSupportService {

    @PersistenceContext
    EntityManager em;

    @Transactional
    public void cleanupDatabase() {
        if (!ConfigUtils.isProfileActive("devtest")) {
            throw new UnsupportedOperationException("Only available in tests");
        }
        em.createNativeQuery("delete from Document").executeUpdate();
        em.createNativeQuery("delete from Signature_Template").executeUpdate();
    }
}
