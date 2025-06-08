package com.epam.indigoeln.eln.config;

import com.epam.indigoeln.eln.model.ApplicationRole;
import com.epam.indigoeln.eln.service.UserService;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;

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
            em.createNativeQuery("SELECT SET_CONFIG('eln.currentUserId', CAST(? AS VARCHAR), TRUE), SET_CONFIG('eln.isContentEditor', CAST(? AS VARCHAR), TRUE)")
                    .setParameter(1, userService.getCurrentUser().getId())
                    .setParameter(2, userService.getCurrentUser().hasRole(ApplicationRole.CONTENT_EDITOR))
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
