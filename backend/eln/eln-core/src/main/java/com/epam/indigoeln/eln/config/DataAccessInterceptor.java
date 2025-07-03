package com.epam.indigoeln.eln.config;

import com.epam.indigoeln.eln.model.ApplicationPermission;
import com.epam.indigoeln.eln.service.UserService;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
@DataAccess
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class DataAccessInterceptor {

    @Inject
    UserService userService;
    @PersistenceContext
    EntityManager em;

    private final ThreadLocal<Boolean> invoked = new ThreadLocal<>();

    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception {
        boolean executed = false;
        if (invoked.get() != Boolean.TRUE) {
            Set<ApplicationPermission> permissions = userService.getCurrentUser().collectPermissions();
            em.createNativeQuery("SELECT SET_CONFIG('eln.currentUserId', CAST(? AS VARCHAR), TRUE), SET_CONFIG('eln.viewAllProjects', CAST(? AS VARCHAR), TRUE), SET_CONFIG('eln.viewAllNotebooks', CAST(? AS VARCHAR), TRUE), SET_CONFIG('eln.viewAllExperiments', CAST(? AS VARCHAR), TRUE)")
                    .setParameter(1, userService.getCurrentUser().getId())
                    .setParameter(2, permissions.contains(ApplicationPermission.VIEW_PROJECTS))
                    .setParameter(3, permissions.contains(ApplicationPermission.VIEW_NOTEBOOKS))
                    .setParameter(4, permissions.contains(ApplicationPermission.VIEW_EXPERIMENTS))
                    .getSingleResult();
            invoked.set(true);
            executed = true;
        }
        try {
            return context.proceed();
        } finally {
            if (executed) {
                invoked.remove();
            }
        }
    }
}
