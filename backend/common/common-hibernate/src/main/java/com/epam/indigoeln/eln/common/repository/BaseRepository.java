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
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkState;

@RequiredArgsConstructor
public abstract class BaseRepository<E extends IdentifiableEntity> implements PanacheRepositoryBase<E, UUID> {

    protected static final Sort DEFAULT_SORT = Sort.descending("modifiedAt");

    protected final EntityType entityType;
    protected final Class<E> entityClass;

    @PersistenceContext
    protected EntityManager em;

    protected Page<E> doFindWithTotals(Conditions conditions, @Nullable Paging paging, Sort sort) {
        paging = ModelUtil.firstNotNull(paging, Paging.DEFAULT);
        PanacheQuery<E> query = doCreateQuery(conditions, paging, sort, null);
        List<E> list = query.list();
        long count = query.count();
        return Page.of(paging, count, list);
    }

    protected Page<E> doFindWithTotals(Conditions conditions, @Nullable Paging paging, Sort sort, EntityGraph<?> entityGraph) {
        paging = ModelUtil.firstNotNull(paging, Paging.DEFAULT);
        PanacheQuery<E> query = doCreateQuery(conditions, paging, sort, entityGraph);
        List<E> list = query.list();
        long count = query.count();
        return Page.of(paging, count, list);
    }

    @Nullable
    protected E doFindOne(Conditions conditions) {
        PanacheQuery<E> query = doCreateQuery(conditions, null, null, null);
        List<E> list = query.list();
        checkState(list.size() <= 1);
        return !list.isEmpty() ? list.getFirst() : null;
    }

    @Nullable
    protected E doFindOne(Conditions conditions, EntityGraph<?> entityGraph) {
        PanacheQuery<E> query = doCreateQuery(conditions, null, null, entityGraph);
        List<E> list = query.list();
        checkState(list.size() <= 1);
        return !list.isEmpty() ? list.getFirst() : null;
    }

    protected List<E> doFind(Conditions conditions) {
        PanacheQuery<E> query = doCreateQuery(conditions, null, null, null);
        return query.list();
    }

    protected List<E> doFind(Sort sort) {
        PanacheQuery<E> query = doCreateQuery(Conditions.EMPTY, null, sort, null);
        return query.list();
    }

    protected List<E> doFind(Conditions conditions, Sort sort) {
        PanacheQuery<E> query = doCreateQuery(conditions, null, sort, null);
        return query.list();
    }

    protected List<E> doFind(Conditions conditions, Paging paging, Sort sort) {
        PanacheQuery<E> query = doCreateQuery(conditions, paging, sort, null);
        return query.list();
    }

    protected List<E> doFind(Conditions conditions, @Nullable Paging paging, @Nullable Sort sort, EntityGraph<?> entityGraph) {
        PanacheQuery<E> query = doCreateQuery(conditions, paging, sort, entityGraph);
        return query.list();
    }

    protected E doLoad(UUID id, EntityGraph<?> entityGraph) {
        PanacheQuery<E> query = find("id", id);
        return query
                .withHint("jakarta.persistence.loadgraph", entityGraph)
                .singleResultOptional()
                .orElseThrow(() -> new AccessDeniedException(entityType, id));
    }

    public E doLoadAndLock(UUID id, LockModeType lockMode, @Nullable EntityGraph<?> entityGraph) {
        Map<String, Object> properties = entityGraph != null ? Map.of("jakarta.persistence.loadgraph", entityGraph) : Map.of();
        E entity = em.find(entityClass, id, lockMode, properties);
        //noinspection ConstantValue
        if (entity == null) {
            throw new EntityNotFoundException(entityType, id);
        }
        return entity;
    }

    public E get(UUID id) {
        E entity = findById(id);
        //noinspection ConstantValue
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

    protected PanacheQuery<E> doCreateQuery(Conditions conditions, @Nullable Paging paging, @Nullable Sort sort, @Nullable EntityGraph<?> entityGraph) {
        PanacheQuery<E> query = !conditions.isEmpty()
                ? (sort != null ? find(conditions.getQuery(), sort, conditions.getValues()) : find(conditions.getQuery(), conditions.getValues()))
                : (sort != null ? findAll(sort) : findAll());
        if (paging != null) {
            query = query.page(paging.getPageNoOrDefault(), paging.getPageSizeOrDefault());
        }
        if (entityGraph != null) {
            query.withHint("jakarta.persistence.loadgraph", entityGraph);
        }
        return query;
    }
}
