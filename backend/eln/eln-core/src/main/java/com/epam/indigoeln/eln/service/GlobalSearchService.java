package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.compound.model.NumericSearch;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.util.NamedConditions;
import com.epam.indigoeln.reaction.model.ReactionRole;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Stream;

@DataAccess
@Transactional
@ApplicationScoped
public class GlobalSearchService {

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
        String rolesSelector = "null AS reaction_roles ";
        String groupBySQL = null;
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
            experimentJoins.add("join Experiment_Referenced_Compound erc on erc.experiment_id = e.id");
            experimentJoins.add("join Compound c on c.id = erc.compound_id");
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
            if (request.getReactionRole() != null) {
                experimentConditions.add("erc.reaction_role = cast(:role as Reaction_Role)", "role", request.getReactionRole().name());
            }
            rolesSelector = "ARRAY_AGG(DISTINCT erc.reaction_role::varchar) AS reaction_roles";
            groupBySQL = "e.name, e.id, e.description, e.created_by_id, e.created_at, e.modified_by_id, e.modified_at";
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

        String fragmentSelector = "left(t.description, 120)";
        if (request.getQuery() != null) {
            String condition = "search_vector @@ websearch_to_tsquery('english', :query)";
            projectConditions.add(condition, "query", request.getQuery());
            notebookConditions.add(condition, "query", request.getQuery());
            experimentConditions.add(condition, "query", request.getQuery());
            fragmentSelector = "ts_headline('english', t.description, websearch_to_tsquery('english', :query), 'StartSel=<mark>,StopSel=</mark>')";
        }
        StringBuilder sql = new StringBuilder();
        sql.append("WITH t AS (\n");
        Map<String, @Nullable Object> params = new HashMap<>();
        boolean hasUnionBlocks = false;
        if (hasProjects) {
            String projectsSQL = "SELECT 'PROJECT' AS type, p.name, p.id, p.description, p.created_by_id, p.created_at, p.modified_by_id, p.modified_at, NULL AS reaction_roles\n"
                    + "FROM Project p\n"
                    + "JOIN Project_View_2 pv ON pv.id = p.id\n"
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
            String notebooksSQL = "SELECT 'NOTEBOOK' AS type, n.name, n.id, n.description, n.created_by_id, n.created_at, n.modified_by_id, n.modified_at, NULL AS reaction_roles"
                    + "\nFROM Notebook n"
                    + "\nJOIN Notebook_View_2 nv ON nv.id = n.id"
                    + "\nWHERE " + notebookConditions.getQuery();
            params.putAll(notebookConditions.getValues());
            sql.append(notebooksSQL);
        }
        if (hasExperiments) {
            if (hasUnionBlocks) {
                sql.append("\nUNION ALL\n");
            }
            hasUnionBlocks = true;
            String experimentsSQL = "SELECT 'EXPERIMENT' AS type, e.name, e.id, e.description, e.created_by_id, e.created_at, e.modified_by_id, e.modified_at, " + rolesSelector
                    + "\nFROM Experiment e"
                    + "\nJOIN Experiment_View_2 ev ON ev.id = e.id"
                    + "\n" + String.join("\n", experimentJoins)
                    + "\nWHERE " + experimentConditions.getQuery()
                    + (groupBySQL != null ? "\nGROUP BY " + groupBySQL : "");
            params.putAll(experimentConditions.getValues());
            sql.append(experimentsSQL);
        }
        sql.append(")\n");
        sql.append("SELECT t.type, t.name, t.id, ").append(fragmentSelector).append(" fragment");
        sql.append(", t.created_by_id, c.username, c.display_name, t.created_at");
        sql.append(", t.modified_by_id, m.username, m.display_name, t.modified_at");
        sql.append(", t.reaction_roles");
        sql.append(", count(*) over (partition by 1)");
        sql.append("\nFROM t");
        sql.append("\nJOIN User_Account c on c.id = t.created_by_id");
        sql.append("\nJOIN User_Account m on m.id = t.modified_by_id");
        long[] totalCount = new long[] {0};
        Query query = em.createNativeQuery(sql.toString())
                .setFirstResult(paging.getPageNoOrDefault() * paging.getPageSizeOrDefault())
                .setMaxResults(paging.getPageSizeOrDefault());
        params.forEach(query::setParameter);
        //noinspection unchecked
        Stream<Object[]> stream = query.getResultStream();
        List<GlobalSearchResultDTO> list = stream
                .map(row -> {
                    GlobalSearchResultDTO item = new GlobalSearchResultDTO();
                    item.setType(EntityType.valueOf(row[0].toString()));
                    item.setName((String) row[1]);
                    item.setId((UUID) row[2]);
                    item.setFragment((String) row[3]);
                    item.setCreatedBy(new UserRef((UUID) row[4], (String) row[5], (String) row[6]));
                    item.setCreatedAt(((Instant) row[7]).atZone(ZoneId.systemDefault()));
                    item.setModifiedBy(new UserRef((UUID) row[8], (String) row[9], (String) row[10]));
                    item.setModifiedAt(((Instant) row[11]).atZone(ZoneId.systemDefault()));
                    //noinspection ConstantValue
                    if (row[12] != null) {
                        String[] reactionRoles = (String[]) row[12];
                        item.setReactionRoles(StreamEx.of(reactionRoles).map(ReactionRole::valueOf).toCollection(() -> EnumSet.noneOf(ReactionRole.class)));
                    }
                    totalCount[0] = (Long) row[13];
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
