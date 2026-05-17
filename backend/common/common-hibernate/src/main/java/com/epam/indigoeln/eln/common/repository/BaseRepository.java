package com.epam.indigoeln.eln.common.repository;

import com.epam.indigoeln.common.exception.AccessDeniedException;
import com.epam.indigoeln.common.exception.EntityNotFoundException;
import com.epam.indigoeln.common.model.EntityType;
import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.eln.common.entity.IdentifiableEntity;
import com.epam.indigoeln.eln.common.util.Conditions;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkState;

@RequiredArgsConstructor
public abstract class BaseRepository<E extends IdentifiableEntity> implements PanacheRepositoryBase<E, UUID> {

    protected static final Sort DEFAULT_SORT = Sort.descending("modifiedAt");

    protected final EntityType entityType;
    protected final Class<E> entityClass;

    @PersistenceContext
    protected EntityManager em;

    protected <DTO> Page<DTO> doFindWithTotals(Conditions conditions, @Nullable Paging paging, Sort sort, @Nullable EntityGraph<?> entityGraph, Function<E, DTO> mapper) {
        paging = ModelUtil.firstNotNull(paging, Paging.DEFAULT);
        PanacheQuery<E> query = doCreateQuery(conditions, paging, sort, entityGraph);
        List<DTO> list = query.stream().map(mapper).toList();
        long count = query.count();
        return Page.of(paging, count, list);
    }

    @Nullable
    protected <DTO> DTO doFindOne(Conditions conditions, @Nullable EntityGraph<?> entityGraph, Function<E, DTO> mapper) {
        PanacheQuery<E> query = doCreateQuery(conditions, entityGraph);
        List<E> list = query.list();
        checkState(list.size() <= 1);
        return list.isEmpty() ? null : mapper.apply(list.getFirst());
    }

    protected <DTO> List<DTO> doFind(Conditions conditions, Paging paging, Sort sort, @Nullable EntityGraph<?> entityGraph, Function<E, DTO> mapper) {
        PanacheQuery<E> query = doCreateQuery(conditions, paging, sort, entityGraph);
        return query.stream().map(mapper).toList();
    }

    protected <DTO> DTO doLoadDetails(UUID id, EntityGraph<?> entityGraph, Function<E, DTO> mapper) {
        PanacheQuery<E> query = find("id", id);
        E entity = query
                .withHint("jakarta.persistence.loadgraph", entityGraph)
                .singleResultOptional()
                .orElseThrow(() -> new AccessDeniedException(entityType, id));
        return mapper.apply(entity);
    }

    public E get(UUID id) {
        E entity = findById(id);
        if (entity == null) {
            throw new EntityNotFoundException(entityType, id);
        }
        return entity;
    }

    public E getReference(UUID id) {
        return em.getReference(entityClass, id);
    }

    public void flushAndRefresh(E entity) {
        em.flush();
        em.refresh(entity);
    }

    private PanacheQuery<E> doCreateQuery(Conditions conditions, Paging paging, Sort sort, @Nullable EntityGraph<?> entityGraph) {
        PanacheQuery<E> query = conditions.isEmpty() ? findAll(sort) : find(conditions.getQuery(), sort, conditions.getValues());
        query = query.page(paging.getPageNoOrDefault(), paging.getPageSizeOrDefault());
        if (entityGraph != null) {
            query.withHint("jakarta.persistence.loadgraph", entityGraph);
        }
        return query;
    }

    private PanacheQuery<E> doCreateQuery(Conditions conditions, @Nullable EntityGraph<?> entityGraph) {
        PanacheQuery<E> query = find(conditions.getQuery(), conditions.getValues());
        if (entityGraph != null) {
            query.withHint("jakarta.persistence.loadgraph", entityGraph);
        }
        return query;
    }
}
