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
import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.criteria.CriteriaDefinition;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

import static com.epam.indigoeln.common.util.ModelUtil.map;
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

    protected Page<E> doFindWithTotals(CriteriaDefinition<Tuple> criteria, @Nullable Paging paging, EntityGraph<?> entityGraph) {
        paging = ModelUtil.firstNotNull(paging, Paging.DEFAULT);
        TypedQuery<Tuple> query = em.createQuery(criteria)
                .setFirstResult(paging.getFirstResult())
                .setMaxResults(paging.getPageSizeOrDefault());

        List<Tuple> idsAndTotals = query.getResultList();
        if (idsAndTotals.isEmpty()) {
            return Page.of(paging, 0, List.of());
        }

        long total = idsAndTotals.getFirst().get(1, Number.class).longValue();
        List<UUID> ids = map(idsAndTotals, (Tuple t) -> t.get(0, UUID.class));
        List<E> list = doLoadByIDs(ids, entityGraph);
        return Page.of(paging, total, list);
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

    protected List<E> doFind(Conditions conditions, @Nullable Paging paging, @Nullable Sort sort, @Nullable EntityGraph<?> entityGraph) {
        PanacheQuery<E> query = doCreateQuery(conditions, paging, sort, entityGraph);
        return query.list();
    }

    protected List<E> doFindByIDs(Collection<UUID> ids, EntityGraph<?> entityGraph) {
        return find("id IN ?1", ids)
                .withHint("jakarta.persistence.loadgraph", entityGraph)
                .list();
    }

    protected E doLoad(UUID id, EntityGraph<?> entityGraph) {
        PanacheQuery<E> query = find("id", id);
        return query
                .withHint("jakarta.persistence.loadgraph", entityGraph)
                .singleResultOptional()
                .orElseThrow(() -> new AccessDeniedException(entityType, id));
    }

    protected List<E> doLoadByIDs(List<UUID> ids, @Nullable EntityGraph<?> entityGraph) {
        Map<UUID, @Nullable E> map = new LinkedHashMap<>();
        for (UUID id : ids) {
            map.put(id, null);
        }
        List<E> list = doFind(new Conditions().add("id in ?", ids), null, null, entityGraph);
        checkState(list.size() == ids.size());
        for (E entity : list) {
            checkState(map.get(entity.getId()) == null);
            map.put(entity.getId(), entity);
        }
        return List.copyOf(map.values());
    }

    protected <DTO> List<DTO> doLoadByIDs(List<UUID> ids, @Nullable EntityGraph<?> entityGraph, Function<E, DTO> mapper) {
        return doLoadByIDs(ids, entityGraph).stream().map(mapper).toList();
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
