package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.UserRef;
import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.entity.CompoundEntity_;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.*;
import com.epam.indigoeln.eln.model.ELNEntityType;
import com.epam.indigoeln.eln.model.ExperimentStatus;
import com.epam.indigoeln.eln.model.GlobalSearchRequest;
import com.epam.indigoeln.eln.model.GlobalSearchResultDTO;
import com.epam.indigoeln.eln.util.CriteriaConditions;
import com.epam.indigoeln.reaction.model.ReactionRole;
import com.google.common.collect.Lists;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Selection;
import jakarta.transaction.Transactional;
import one.util.streamex.EntryStream;
import org.hibernate.query.criteria.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@DataAccess
@Transactional
@ApplicationScoped
public class GlobalSearchService {

    private static final int MAX_COUNT = 1000;

    private static final int TYPE_PROJECT = 1;
    private static final int TYPE_NOTEBOOK = 2;
    private static final int TYPE_EXPERIMENT = 3;

    @Inject
    UserService userService;
    @Inject
    CriteriaConditions.Factory criteriaConditionsFactory;
    @Inject
    HibernateCriteriaBuilder cb;
    @PersistenceContext
    EntityManager em;

    public Page<GlobalSearchResultDTO> search(GlobalSearchRequest request, Paging paging) {
        if (request.isEmpty()) {
            throw new InvalidRequestException("Request is empty");
        }
        boolean onlyExperiments = request.getTherapeuticArea() != null || request.getProjectCode() != null || request.getExperimentStatus() != null
                || request.getMoleculeStructure() != null || request.getReactionStructure() != null
                || request.getBatchPurity() != null || request.getBatchYield() != null;
        boolean hasFullTextSearch = request.getQuery() != null;
        boolean hasRoles = request.getMoleculeStructure() != null;

        int filteredLimit = Math.max(MAX_COUNT, paging.getFirstResult() + paging.getPageSizeOrDefault());

        CriteriaQuery<Tuple> experimentsQuery = cb.createTupleQuery();
        new CriteriaDefinition<>(em, experimentsQuery) {{
            JpaRoot<ExperimentEntity> root = from(ExperimentEntity.class);
            criteriaConditionsFactory.withConditions(this::where, conditions -> {
                Expression<?> rank = root.get(ExperimentEntity_.createdAt);
                Expression<ReactionRole[]> roles = null;
                conditions.user(root.get(ExperimentEntity_.createdBy), request.getAuthor());
                conditions.fullTextSearch(root.get(ExperimentEntity_.searchVector), request.getQuery(), root.get(ExperimentEntity_.name));
                if (request.getQuery() != null) {
                    rank = conditions.fullTextRank(root.get(ExperimentEntity_.searchVector), request.getQuery());
                }
                conditions.dictionary(root.get(ExperimentEntity_.therapeuticArea), request.getTherapeuticArea());
                conditions.dictionary(root.get(ExperimentEntity_.projectCode), request.getProjectCode());
                if (request.getExperimentStatus() != null) {
                    conditions.add(root.get(ExperimentEntity_.status).in(request.getExperimentStatus()));
                }
                // molecule search - use GROUP BY instead of EXISTS to: 1. extract similarity rank; 2. extract roles
                if (request.getMoleculeStructure() != null) {
                    JpaJoin<ExperimentEntity, ExperimentSearchCompound> experimentCompound = root.join(ExperimentEntity_.searchCompounds);
                    JpaJoin<ExperimentSearchCompound, CompoundEntity> compound = experimentCompound.join(ExperimentSearchCompound_.compound);
                    conditions.moleculeSearch(compound.get(CompoundEntity_.molFile), request.getMoleculeStructure());
                    if (request.getReactionRole() != null) {
                        conditions.add(equal(experimentCompound.get(ExperimentSearchCompound_.reactionRole), request.getReactionRole()));
                    }
                    roles = arrayAgg(null, experimentCompound.get(ExperimentSearchCompound_.REACTION_ROLE));
                    // best-matching compound ranks the experiment; the per-compound value is not grouped
                    rank = max(conditions.moleculeSimilarity(compound.get(CompoundEntity_.molFile), request.getMoleculeStructure().query()));
                    groupBy(root.get(ExperimentEntity_.id));
                }
                // reaction search
                if (request.getReactionStructure() != null) {
                    JpaSubQuery<Integer> subquery = subquery(Integer.class);
                    subquery.select(literal(1));
                    JpaRoot<ExperimentEntity> subRoot = subquery.correlate(root);
                    JpaJoin<ExperimentEntity, String> rxnfile = subRoot.join(ExperimentEntity_.searchRxnfiles);
                    criteriaConditionsFactory.withConditions(subquery::where, rxnfilesConditions -> {
                        rxnfilesConditions.reactionSearch(rxnfile, request.getReactionStructure());
                    });
                    conditions.add(exists(subquery));
                }
                // batches search
                if (request.getBatchPurity() != null || request.getBatchYield() != null) {
                    JpaSubQuery<Integer> subquery = subquery(Integer.class);
                    subquery.select(literal(1));
                    JpaRoot<ExperimentEntity> subRoot = subquery.correlate(root);
                    JpaSetJoin<ExperimentEntity, ExperimentSearchBatch> batch = subRoot.join(ExperimentEntity_.searchBatches);
                    criteriaConditionsFactory.withConditions(subquery::where, samplesConditions -> {
                        if (request.getBatchPurity() != null) {
                            samplesConditions.numericSearch(batch.get(ExperimentSearchBatch_.batchPurity), request.getBatchPurity());
                        }
                        if (request.getBatchYield() != null) {
                            samplesConditions.numericSearch(batch.get(ExperimentSearchBatch_.batchYield), request.getBatchYield());
                        }
                    });
                    conditions.add(exists(subquery));
                }
                List<Selection<?>> columns = Lists.newArrayList(literal(TYPE_EXPERIMENT).alias("type"), root.get(ExperimentEntity_.id).alias("id"), rank.alias("rank"));
                if (roles != null) {
                    columns.add(roles.alias("roles"));
                }
                select(tuple(columns));
                orderBy(desc(rank));
                fetch(filteredLimit + 1);
            });
        }};
        CriteriaQuery<Tuple> filteredQuery;
        if (onlyExperiments) {
            filteredQuery = experimentsQuery;
        } else {
            CriteriaQuery<Tuple> projectsQuery = cb.createTupleQuery();
            new CriteriaDefinition<>(em, projectsQuery) {{
                JpaRoot<ProjectEntity> root = from(ProjectEntity.class);
                criteriaConditionsFactory.withConditions(this::where, conditions -> {
                    Expression<?> rank = root.get(ExperimentEntity_.createdAt);
                    conditions.user(root.get(ProjectEntity_.createdBy), request.getAuthor());
                    conditions.fullTextSearch(root.get(ProjectEntity_.searchVector), request.getQuery(), root.get(ProjectEntity_.name));
                    if (request.getQuery() != null) {
                        rank = conditions.fullTextRank(root.get(ProjectEntity_.searchVector), request.getQuery());
                    }
                    select(tuple(literal(TYPE_PROJECT), root.get(ProjectEntity_.id), rank));
                    orderBy(desc(rank));
                    fetch(filteredLimit + 1);
                });
            }};

            CriteriaQuery<Tuple> notebooksQuery = cb.createTupleQuery();
            new CriteriaDefinition<>(em, notebooksQuery) {{
                JpaRoot<NotebookEntity> root = from(NotebookEntity.class);
                criteriaConditionsFactory.withConditions(this::where, conditions -> {
                    Expression<?> rank = root.get(ExperimentEntity_.createdAt);
                    conditions.user(root.get(NotebookEntity_.createdBy), request.getAuthor());
                    conditions.fullTextSearch(root.get(NotebookEntity_.searchVector), request.getQuery(), root.get(NotebookEntity_.name));
                    if (request.getQuery() != null) {
                        rank = conditions.fullTextRank(root.get(NotebookEntity_.searchVector), request.getQuery());
                    }
                    select(tuple(literal(TYPE_NOTEBOOK), root.get(NotebookEntity_.id), rank));
                    orderBy(desc(rank));
                    fetch(filteredLimit + 1);
                });
            }};

            filteredQuery = cb.unionAll(experimentsQuery, projectsQuery, notebooksQuery);
        }

        CriteriaDefinition<Tuple> criteria = new CriteriaDefinition<>(em, Tuple.class) {{
            JpaCteCriteria<Tuple> filtered = with("filtered", filteredQuery);

            JpaSubQuery<Tuple> totalsQuery = subquery(Tuple.class);
            totalsQuery.from(filtered);
            totalsQuery.multiselect(count().alias("total"));

            JpaSubQuery<Tuple> pageQuery = subquery(Tuple.class);
            JpaRoot<Tuple> pageQueryRoot = pageQuery.from(filtered);
            pageQuery.orderBy(desc(pageQueryRoot.get("rank")), desc(pageQueryRoot.get("id")));
            List<Selection<?>> pageColumns = Lists.newArrayList(pageQueryRoot.get("type").alias("type"), pageQueryRoot.get("id").alias("id"));
            if (hasRoles) {
                pageColumns.add(pageQueryRoot.get("roles").alias("roles"));
            }
            pageQuery.multiselect(pageColumns);
            pageQuery.offset(paging.getFirstResult());
            pageQuery.fetch(paging.getPageSizeOrDefault());

            JpaDerivedRoot<Tuple> page = from(pageQuery);
            JpaDerivedRoot<Tuple> totals = from(totalsQuery);

            JpaEntityJoin<Tuple, ExperimentEntity> experiment = page.join(ExperimentEntity.class, JoinType.LEFT);
            experiment.on(equal(experiment.get(ExperimentEntity_.id), page.get("id")));
            JpaEntityJoin<Tuple, ProjectEntity> project = page.join(ProjectEntity.class, JoinType.LEFT);
            project.on(equal(project.get(ProjectEntity_.id), page.get("id")));
            JpaEntityJoin<Tuple, NotebookEntity> notebook = page.join(NotebookEntity.class, JoinType.LEFT);
            notebook.on(equal(notebook.get(NotebookEntity_.id), page.get("id")));

            Expression<UUID> createdById = experiment.get(ExperimentEntity_.createdBy).get(UserEntity_.id);
            Expression<UUID> modifiedById = experiment.get(ExperimentEntity_.modifiedBy).get(UserEntity_.id);
            Expression<Instant> createdAt = experiment.get(ExperimentEntity_.createdAt);
            Expression<Instant> modifiedAt = experiment.get(ExperimentEntity_.modifiedAt);
            Expression<String> name = experiment.get(ExperimentEntity_.name);
            Expression<String> description = experiment.get(ExperimentEntity_.description);

            if (!onlyExperiments) {
                createdById = coalesce(createdById, project.get(ProjectEntity_.createdBy).get(UserEntity_.id)).value(notebook.get(NotebookEntity_.createdBy).get(UserEntity_.id));
                modifiedById = coalesce(modifiedById, project.get(ProjectEntity_.modifiedBy).get(UserEntity_.id)).value(notebook.get(NotebookEntity_.modifiedBy).get(UserEntity_.id));

                createdAt = coalesce(createdAt, project.get(ProjectEntity_.createdAt)).value(notebook.get(NotebookEntity_.createdAt));
                modifiedAt = coalesce(modifiedAt, project.get(ProjectEntity_.modifiedAt)).value(notebook.get(NotebookEntity_.modifiedAt));
                name = coalesce(name, project.get(ProjectEntity_.name)).value(notebook.get(NotebookEntity_.name));
                description = coalesce(description, project.get(ProjectEntity_.description)).value(notebook.get(NotebookEntity_.description));
            }

            JpaEntityJoin<Tuple, UserEntity> createdBy = page.join(UserEntity.class, JoinType.INNER);
            createdBy.on(equal(createdBy.get(UserEntity_.id), createdById));
            JpaEntityJoin<Tuple, UserEntity> modifiedBy = page.join(UserEntity.class, JoinType.INNER);
            modifiedBy.on(equal(modifiedBy.get(UserEntity_.id), modifiedById));

            List<Selection<?>> columns = Lists.newArrayList(
                    totals.get("total"),
                    page.get("type"),
                    page.get("id"),
                    createdBy.get(UserEntity_.username),
                    createdBy.get(UserEntity_.displayName),
                    createdAt,
                    modifiedBy.get(UserEntity_.username),
                    modifiedBy.get(UserEntity_.displayName),
                    modifiedAt,
                    experiment.get(ExperimentEntity_.revision),
                    name,
                    experiment.get(ExperimentEntity_.title),
                    experiment.get(ExperimentEntity_.status)
            );
            if (hasFullTextSearch) {
                columns.add(cb.function("ts_headline", String.class, literal("english"), description, literal("english"), literal(request.getQuery()), literal("StartSel=<mark>,StopSel=</mark>")));
            }
            if (hasRoles) {
                columns.add(page.get("roles"));
            }
            if (!onlyExperiments) {
                columns.addAll(List.of(
                        project.get(ProjectEntity_.notebookCount),
                        project.get(ProjectEntity_.experimentCount),
                        notebook.get(NotebookEntity_.experimentCount)
                ));
            }

            select(tuple(columns));
        }};

        // TODO only rows with access

        TypedQuery<Tuple> query = criteria.createQuery(em);
        List<Tuple> list = query.getResultList();
        List<GlobalSearchResultDTO> page = list.stream()
                .map(tuple -> {
                    int fieldNo = 0; // skip total
                    GlobalSearchResultDTO item = new GlobalSearchResultDTO();
                    item.setType(switch (tuple.get(++fieldNo, Integer.class)) {
                        case TYPE_PROJECT -> ELNEntityType.PROJECT;
                        case TYPE_NOTEBOOK -> ELNEntityType.NOTEBOOK;
                        case TYPE_EXPERIMENT -> ELNEntityType.EXPERIMENT;
                        default -> throw new IllegalStateException();
                    });
                    item.setId(tuple.get(++fieldNo, UUID.class));
                    item.setCreatedBy(new UserRef(tuple.get(++fieldNo, String.class), tuple.get(++fieldNo, String.class)));
                    item.setCreatedAt(tuple.get(++fieldNo, Instant.class));
                    item.setModifiedBy(new UserRef(tuple.get(++fieldNo, String.class), tuple.get(++fieldNo, String.class)));
                    item.setModifiedAt(tuple.get(++fieldNo, Instant.class));
                    item.setRevision(tuple.get(++fieldNo, Integer.class));
                    item.setName(tuple.get(++fieldNo, String.class));
                    item.setTitle(tuple.get(++fieldNo, String.class));
                    item.setExperimentStatus(tuple.get(++fieldNo, ExperimentStatus.class));
                    if (hasFullTextSearch) {
                        item.setFragment(tuple.get(++fieldNo, String.class));
                    }
                    if (hasRoles) {
                        ReactionRole[] roles = tuple.get(++fieldNo, ReactionRole[].class);
                        item.setReactionRoles(roles != null ? Set.of(roles) : null);
                    }
                    if (!onlyExperiments) {
                        item.setNotebookCount(tuple.get(++fieldNo, Integer.class));
                        //noinspection unchecked
                        Map<ExperimentStatus, Integer> experimentCount = (Map<ExperimentStatus, Integer>) ModelUtil.firstNotNull(tuple.get(++fieldNo), tuple.get(++fieldNo));
                        //noinspection ConstantValue
                        if (experimentCount != null) {
                            item.setExperimentCount(EntryStream.of(experimentCount).values().mapToInt(x -> x).sum());
                        }
                    }
                    return item;
                })
                .toList();
        long total = list.isEmpty() ? 0 : list.getFirst().get(0, Long.class);
        return Page.of(paging, total, page);
    }
}
