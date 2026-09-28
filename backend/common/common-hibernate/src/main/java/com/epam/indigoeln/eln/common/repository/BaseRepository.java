package com.epam.indigoeln.eln.common.repository;

import com.epam.indigoeln.common.exception.AccessDeniedException;
import com.epam.indigoeln.common.exception.EntityNotFoundException;
import com.epam.indigoeln.common.model.EntityType;
import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.eln.common.entity.IdentifiableEntity;
import com.epam.indigoeln.eln.common.entity.IdentifiableEntity_;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.criteria.CriteriaDefinition;
import org.hibernate.query.criteria.JpaRoot;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

import static com.epam.indigoeln.common.util.ModelUtil.map;
import static com.google.common.base.Preconditions.checkState;

@RequiredArgsConstructor
public abstract class BaseRepository<E extends IdentifiableEntity> implements PanacheRepositoryBase<E, UUID> {

    protected static final Sort DEFAULT_SORT = Sort.descending("modifiedAt");
    public static final String JAKARTA_PERSISTENCE_LOADGRAPH = "jakarta.persistence.loadgraph";

    protected final EntityType entityType;
    protected final Class<E> entityClass;

    @PersistenceContext
    protected EntityManager em;

    protected Page<E> doFindWithTotals(CriteriaDefinition<Tuple> criteria, @Nullable Paging paging, @Nullable EntityGraph<?> entityGraph) {
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

    protected List<E> doFind(CriteriaDefinition<E> criteria, @Nullable Paging paging, @Nullable EntityGraph<?> entityGraph) {
        TypedQuery<E> query = em.createQuery(criteria);
        if (paging != null) {
            query.setFirstResult(paging.getFirstResult()).setMaxResults(paging.getPageSizeOrDefault());
        }
        if (entityGraph != null) {
            query.setHint(JAKARTA_PERSISTENCE_LOADGRAPH, entityGraph);
        }
        return query.getResultList();
    }

    @Nullable
    protected E doFindOne(CriteriaDefinition<E> criteria, @Nullable EntityGraph<?> entityGraph) {
        List<E> list = doFind(criteria, null, entityGraph);
        checkState(list.size() <= 1);
        return !list.isEmpty() ? list.getFirst() : null;
    }

    protected boolean doExists(CriteriaDefinition<Integer> criteria) {
        return !em.createQuery(criteria).setMaxResults(1).getResultList().isEmpty();
    }

    protected List<E> doFind(Sort sort) {
        return findAll(sort).list();
    }

    protected List<E> doFindByIDs(Collection<UUID> ids, EntityGraph<?> entityGraph) {
        return find("id IN ?1", ids)
                .withHint(JAKARTA_PERSISTENCE_LOADGRAPH, entityGraph)
                .list();
    }

    protected E doLoad(UUID id, EntityGraph<?> entityGraph) {
        PanacheQuery<E> query = find("id", id);
        return query
                .withHint(JAKARTA_PERSISTENCE_LOADGRAPH, entityGraph)
                .singleResultOptional()
                .orElseThrow(() -> new AccessDeniedException(entityType, id));
    }

    protected List<E> doLoadByIDs(List<UUID> ids, @Nullable EntityGraph<?> entityGraph) {
        Map<UUID, @Nullable E> map = new LinkedHashMap<>();
        for (UUID id : ids) {
            map.put(id, null);
        }
        CriteriaDefinition<E> criteria = new CriteriaDefinition<>(em, entityClass) {{
            JpaRoot<E> root = from(entityClass);
            select(root);
            where(root.get(IdentifiableEntity_.id).in(ids));
        }};
        List<E> list = doFind(criteria, null, entityGraph);
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
}
