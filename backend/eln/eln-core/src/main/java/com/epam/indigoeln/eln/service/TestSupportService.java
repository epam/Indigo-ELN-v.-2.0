package com.epam.indigoeln.eln.service;

import io.quarkus.runtime.configuration.ConfigUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

// no @Transactional
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
        // experiments, notebooks, projects
        em.createNativeQuery("delete from Attachment").executeUpdate();
        em.createNativeQuery("delete from Experiment").executeUpdate();
        em.createNativeQuery("delete from Notebook").executeUpdate();
        em.createNativeQuery("delete from Project").executeUpdate();
        em.createNativeQuery("delete from Template").executeUpdate();
        // samples, compounds
        em.createNativeQuery("delete from Sample").executeUpdate();
        em.createNativeQuery("delete from Compound").executeUpdate();
        // users
        em.createNativeQuery("delete from User_Account where username not in ('admin', 'john@eln.com', 'willow@eln.com', 'bart@eln.com', 'lisa@eln.com')").executeUpdate();
    }
}
