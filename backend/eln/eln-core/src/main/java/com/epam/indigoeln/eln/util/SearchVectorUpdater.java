package com.epam.indigoeln.eln.util;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SearchVectorUpdater {

    @PersistenceContext
    EntityManager em;

    public void update(String table, UUID id, List<@Nullable SearchVectorField> fields) {
        StringBuilder sql = new StringBuilder("UPDATE ").append(table).append(" SET search_vector = ");
        List<Object> parameters = new ArrayList<>();
        for (SearchVectorField field : fields) {
            if (field == null) {
                continue;
            }
            if (!parameters.isEmpty()) {
                sql.append(" || ");
            }
            parameters.add(field.text());
            sql.append("setweight(to_tsvector('english', ?").append(parameters.size()).append("), '").append(field.weight()).append("')");
        }
        em.flush();
        if (parameters.isEmpty()) {
            return;
        }
        parameters.add(id);
        sql.append(" WHERE id = ?").append(parameters.size());
        //noinspection SqlSourceToSinkFlow
        Query q = em.createNativeQuery(sql.toString());
        for (int i = 0; i < parameters.size(); i++) {
            q.setParameter(i + 1, parameters.get(i));
        }
        q.executeUpdate();
    }

}
