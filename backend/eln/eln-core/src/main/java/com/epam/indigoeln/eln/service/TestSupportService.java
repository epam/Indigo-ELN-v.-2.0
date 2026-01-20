package com.epam.indigoeln.eln.service;

import io.quarkus.cache.CacheManager;
import io.quarkus.runtime.configuration.ConfigUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

// no @Transactional
@ApplicationScoped
@SuppressWarnings("SqlWithoutWhere")
public class TestSupportService {

    @PersistenceContext
    EntityManager em;

    @Inject
    CacheManager cacheManager;

    @Transactional
    public void cleanupDatabase() {
        if (!ConfigUtils.isProfileActive("devtest")) {
            throw new UnsupportedOperationException("Only available in tests");
        }
        // experiments, notebooks, projects
        em.createNativeQuery("delete from Attachment").executeUpdate();
        em.createNativeQuery("delete from Experiment").executeUpdate();
        em.createNativeQuery("delete from Notebook").executeUpdate();
        em.createNativeQuery("delete from Project").executeUpdate();
        em.createNativeQuery("delete from Template").executeUpdate();
        em.createNativeQuery("delete from Signature_Template").executeUpdate();
        // samples, compounds
        em.createNativeQuery("delete from Sample").executeUpdate();
        em.createNativeQuery("delete from Compound").executeUpdate();
        em.createNativeQuery("alter sequence compound_str_code_compound_seq restart").executeUpdate();
        // dictionaries
        em.createNativeQuery("delete from Dictionary_Item").executeUpdate();
        em.createNativeQuery("delete from Dictionary").executeUpdate();
        em.createNativeQuery("delete from Salt_Code").executeUpdate();
        // users
        em.createNativeQuery("delete from User_Account where username not in ('admin')").executeUpdate();

        for (String cacheName : cacheManager.getCacheNames()) {
            cacheManager.getCache(cacheName).get().invalidateAll().await().indefinitely();
        }
    }
}
