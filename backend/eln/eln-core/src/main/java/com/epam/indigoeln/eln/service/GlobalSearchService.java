package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.compound.model.NumericSearch;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.util.NamedConditions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

@DataAccess
@Transactional
@ApplicationScoped
public class GlobalSearchService {

    private static final int SLOT_PROJECTS = 0;
    private static final int SLOT_NOTEBOOKS = 1;
    private static final int SLOT_EXPERIMENTS = 2;

    @PersistenceContext
    EntityManager em;

    public Page<GlobalSearchResultDTO> search(GlobalSearchRequest request, Paging paging) {
        if (request.isEmpty()) {
            throw new InvalidRequestException("Request is empty");
        }
        NamedConditions projectConditions = new NamedConditions();
        NamedConditions notebookConditions = new NamedConditions();
        NamedConditions experimentConditions = new NamedConditions();
        boolean hasProjects = true, hasNotebooks = true, hasExperiments = true;
        List<String> experimentJoins = new ArrayList<>();
        if (request.getTherapeuticArea() != null) {
            hasProjects = hasNotebooks = false;
            experimentConditions.add("therapeutic_area_id = :therapeuticArea", "therapeuticArea", request.getTherapeuticArea().getId());
        }
        if (request.getProjectCode() != null) {
            hasProjects = hasNotebooks = false;
            experimentConditions.add("project_code_id = :projectCode", "projectCode", request.getProjectCode().getId());
        }
        if (request.getExperimentStatus() != null) {
            hasProjects = hasNotebooks = false;
            experimentConditions.add("status = any(cast(:experimentStatus as Experiment_Status[]))", "experimentStatus", request.getExperimentStatus().stream().map(Enum::name).toArray(String[]::new));
        }
        if (request.getAuthor() != null) {
            String condition = "created_by_id in :author";
            List<UUID> ids = request.getAuthor().stream().map(UserRef::getId).toList();
            projectConditions.add(condition, "author", ids);
            notebookConditions.add(condition, "author", ids);
            experimentConditions.add(condition, "author", ids);
        }
        if (request.getMoleculeStructure() != null) {
            hasProjects = hasNotebooks = false;
            experimentJoins.add("join Experiment_Referenced_Compound ce on ce.experiment_id = e.id");
            experimentJoins.add("join Compound c on c.id = ce.compound_id");
            switch (request.getMoleculeStructure().type()) {
                case EXACT -> {
                    experimentConditions.add("c.mol_file @ (:molfile, '')::bingo.exact", "molfile", request.getMoleculeStructure().query());
                }
                case SUBSTRUCTURE -> {
                    experimentConditions.add("c.mol_file @ (:molfile, '')::bingo.sub", "molfile", request.getMoleculeStructure().query());
                }
                case SIMILARITY -> {
                    experimentConditions.add("c.mol_file @ (0.8, null, :molfile, 'Tanimoto')::bingo.sim", "molfile", request.getMoleculeStructure().query());
                }
            }
        }
        if (request.getReactionStructure() != null) {
            hasProjects = hasNotebooks = false;
            experimentJoins.add("join Experiment_Rxnfile rxn on rxn.experiment_id = e.id");
            switch (request.getReactionStructure().type()) {
                case EXACT -> {
                    experimentConditions.add("rxn.rxnfile @ (:rxnfile, '')::bingo.rexact", "rxnfile", request.getReactionStructure().query());
                }
                case SUBSTRUCTURE -> {
                    experimentConditions.add("rxn.rxnfile @ (:rxnfile, '')::bingo.rsub", "rxnfile", request.getReactionStructure().query());
                }
                case SIMILARITY -> {
                    throw new InvalidRequestException("Reaction similarity search is not supported");
                }
            }
        }
        if (request.getBatchPurity() != null) {
            hasProjects = hasNotebooks = false;
            experimentConditions.add("jsonb_path_exists(e.model, '$.reactions[*].outputs[*].samples[*].purity.value ? (@ " + generateNumericCondition(request.getBatchPurity()) + ")')");
        }
        if (request.getBatchYield() != null) {
            hasProjects = hasNotebooks = false;
            experimentConditions.add("jsonb_path_exists(e.model, '$.reactions[*].outputs[*].samples[*].yield.value ? (@ " + generateNumericCondition(request.getBatchYield()) + ")')");
        }

        if (request.getQuery() != null) {
            String condition = "search_vector @@ websearch_to_tsquery('english', :query)";
            projectConditions.add(condition, "query", request.getQuery());
            notebookConditions.add(condition, "query", request.getQuery());
            experimentConditions.add(condition, "query", request.getQuery());
        }
        StringBuilder sql = new StringBuilder();
        sql.append("with t as (\n");
        Map<String, @Nullable Object> params = new HashMap<>();
        boolean hasUnionBlocks = false;
        if (hasProjects) {
            String projectsSQL = "SELECT 'PROJECT' AS type, p.name, p.id, p.created_by_id, p.created_at, p.modified_by_id, p.modified_at "
                                 + "FROM project_view p "
                                 + "WHERE " + projectConditions.getQuery();
            sql.append(projectsSQL);
            params.putAll(projectConditions.getValues());
            hasUnionBlocks = true;
        }
        if (hasNotebooks) {
            if (hasUnionBlocks) {
                sql.append("\nUNION ALL\n");
            }
            hasUnionBlocks = true;
            String notebooksSQL = "SELECT 'NOTEBOOK' AS type, n.name, n.id, n.created_by_id, n.created_at, n.modified_by_id, n.modified_at "
                                  + "FROM notebook_view n "
                                  + "WHERE " + notebookConditions.getQuery();
            params.putAll(notebookConditions.getValues());
            sql.append(notebooksSQL);
        }
        if (hasExperiments) {
            if (hasUnionBlocks) {
                sql.append("\nUNION ALL\n");
            }
            hasUnionBlocks = true;
            String experimentsSQL = "SELECT 'EXPERIMENT' AS type, e.name, e.id, e.created_by_id, e.created_at, e.modified_by_id, e.modified_at "
                                    + "FROM experiment_view e "
                                    + String.join(" ", experimentJoins) + " "
                                    + "WHERE " + experimentConditions.getQuery();
            params.putAll(experimentConditions.getValues());
            sql.append(experimentsSQL);
        }
        sql.append(")\n");
        sql.append("""
            select t.type, t.name, t.id
                    , t.created_by_id, c.username, c.display_name, t.created_at
                    , t.modified_by_id, m.username, m.display_name, t.modified_at
                    , count(*) over (partition by 1)
            from t
            join user_account c on c.id = t.created_by_id
            join user_account m on m.id = t.modified_by_id
        """);
        long[] totalCount = new long[] {0};
        Query query = em.createNativeQuery(sql.toString())
                .setFirstResult(paging.getPageNoOrDefault() * paging.getPageSizeOrDefault())
                .setMaxResults(paging.getPageSizeOrDefault());
        params.forEach(query::setParameter);
        List<GlobalSearchResultDTO> list = query
                .getResultStream()
                .map(x -> {
                    Object[] row = (Object[]) x;
                    GlobalSearchResultDTO item = new GlobalSearchResultDTO();
                    item.setType(EntityType.valueOf(row[0].toString()));
                    item.setName((String) row[1]);
                    item.setId((UUID) row[2]);
                    item.setCreatedBy(new UserRef((UUID) row[3], (String) row[4], (String) row[5]));
                    item.setCreatedAt(((Instant) row[6]).atZone(ZoneId.systemDefault()));
                    item.setModifiedBy(new UserRef((UUID) row[7], (String) row[8], (String) row[9]));
                    item.setModifiedAt(((Instant) row[10]).atZone(ZoneId.systemDefault()));
                    totalCount[0] = (Long) row[11];
                    return item;
                })
                .toList();
        return Page.of(paging, totalCount[0], list);
    }

    private static String generateNumericCondition(NumericSearch batchYield) {
        return switch (batchYield) {
            case NumericSearch.Equals eq -> "=" + eq.value();
            case NumericSearch.GreaterThanOrEqual ge -> ">=" + ge.value();
            case NumericSearch.LessThanOrEqual le -> "<=" + le.value();
        };
    }
}
