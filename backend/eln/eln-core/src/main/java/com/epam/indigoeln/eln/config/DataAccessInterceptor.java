package com.epam.indigoeln.eln.config;

import com.epam.indigoeln.eln.model.ApplicationPermission;
import com.epam.indigoeln.eln.service.UserService;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnit;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.jpa.AvailableHints;
import org.hibernate.stat.Statistics;

import java.util.List;
import java.util.Set;

@Slf4j
@DataAccess
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class DataAccessInterceptor {

    @Inject
    UserService userService;
    @Inject
    Tracer tracer;
    @PersistenceContext
    EntityManager em;
    @PersistenceUnit
    EntityManagerFactory emf;

    private final ThreadLocal<Boolean> invoked = new ThreadLocal<>();

    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception {
        boolean executed = false;
        Span span = null;
        Scope spanScope = null;
        if (invoked.get() != Boolean.TRUE) {
            span = tracer.spanBuilder("DataAccess").startSpan();
            spanScope = span.makeCurrent();
            Set<ApplicationPermission> permissions = userService.getCurrentUser().getPermissions();
            em.createNativeQuery("SELECT SET_CONFIG('eln.currentUserId', CAST(? AS VARCHAR), TRUE), SET_CONFIG('eln.viewAllProjects', CAST(? AS VARCHAR), TRUE), SET_CONFIG('eln.viewAllNotebooks', CAST(? AS VARCHAR), TRUE), SET_CONFIG('eln.viewAllExperiments', CAST(? AS VARCHAR), TRUE)")
                    .setHint(AvailableHints.HINT_NATIVE_SPACES, List.of("nothing")) // Hibernate assumes empty list as missing, so provide non-existent query space
                    .setParameter(1, userService.getCurrentUser().getId())
                    .setParameter(2, permissions.contains(ApplicationPermission.VIEW_PROJECTS))
                    .setParameter(3, permissions.contains(ApplicationPermission.VIEW_NOTEBOOKS))
                    .setParameter(4, permissions.contains(ApplicationPermission.VIEW_EXPERIMENTS))
                    .getSingleResult();
            invoked.set(true);
            executed = true;
        }

        Statistics stats = emf.unwrap(SessionFactory.class).getStatistics();
        StatSnapshot before = executed && stats.isStatisticsEnabled() ? StatSnapshot.from(stats) : null;

        try {
            return context.proceed();
        } finally {
            if (executed) {
                StatSnapshot after = StatSnapshot.from(stats);
                String statsStr = "queries=%d, entityFetches=%d, collectionFetches=%d, connects=%d".formatted(
                        after.queries() - before.queries(), after.entityFetches() - before.entityFetches(),
                        after.collectionFetches() - before.collectionFetches(), after.connects() - before.connects()
                );
                span.setAttribute("hibernate", statsStr);
                spanScope.close();

                invoked.remove();
            }
        }
    }

    private record StatSnapshot(long queries, long entityFetches, long collectionFetches, long connects) {

        static StatSnapshot from(Statistics s) {
            return new StatSnapshot(
                    s.getQueryExecutionCount(),
                    s.getEntityFetchCount(),
                    s.getCollectionFetchCount(),
                    s.getConnectCount()
            );
        }
    }
}
