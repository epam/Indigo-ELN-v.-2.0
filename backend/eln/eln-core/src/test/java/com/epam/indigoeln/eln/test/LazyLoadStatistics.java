package com.epam.indigoeln.eln.test;

import io.quarkus.arc.Arc;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.jboss.logging.Logger;

import java.util.Arrays;
import java.util.Map;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

public final class LazyLoadStatistics {

    private static final Logger LOG = Logger.getLogger(LazyLoadStatistics.class);

    private LazyLoadStatistics() {
    }

    private static Statistics statistics() {
        return Arc.container().instance(EntityManagerFactory.class).get()
                .unwrap(SessionFactory.class).getStatistics();
    }

    public static void clear() {
        statistics().clear();
    }

    /** Logs one line summarising every association lazily fetched since the last clear(), if any. */
    public static void reportIfAny(String testLabel) {
        Statistics stats = statistics();
        String collections = fetched(stats.getCollectionRoleNames(), role -> stats.getCollectionStatistics(role).getFetchCount());
        String entities = fetched(stats.getEntityNames(), name -> stats.getEntityStatistics(name).getFetchCount());
        if (!collections.isEmpty() || !entities.isEmpty()) {
            LOG.infof("LAZY_FETCH %s | collections=[%s] | entities=[%s]", testLabel, collections, entities);
        }
    }

    private static String fetched(String[] names, ToLongFunction<String> fetchCount) {
        return Arrays.stream(names)
                .collect(Collectors.toMap(n -> n, fetchCount::applyAsLong))
                .entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", "));
    }
}
