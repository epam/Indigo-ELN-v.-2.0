package com.epam.indigoeln.eln.service;

import com.arjuna.ats.internal.jdbc.drivers.modifiers.list;
import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.util.Conditions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import one.util.streamex.StreamEx;
import org.hibernate.annotations.processing.SQL;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

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
        Conditions conditions = new Conditions(SLOT_EXPERIMENTS + 1);
        boolean hasProjects = true, hasNotebooks = true, hasExperiments = true;
        if (request.getTherapeuticArea() != null) {
            hasProjects = hasNotebooks = false;
            conditions.add(SLOT_EXPERIMENTS, "therapeutic_area_id = ?", request.getTherapeuticArea().getId());
        }
        if (request.getProjectCode() != null) {
            hasProjects = hasNotebooks = false;
            conditions.add(SLOT_EXPERIMENTS, "project_code_id = ?", request.getProjectCode().getId());
        }
        if (request.getExperimentStatus() != null) {
            hasProjects = hasNotebooks = false;
            conditions.add(SLOT_EXPERIMENTS, "status = cast(? as experiment_status)", request.getExperimentStatus().name());
        }
        if (request.getAuthor() != null) {
            String condition = "created_by_id = ?";
            for (int slotNo = SLOT_PROJECTS; slotNo <= SLOT_EXPERIMENTS; slotNo++) {
                conditions.add(slotNo, condition, request.getAuthor().getId());
            }
        }
        if (request.getStructure() != null) {
            InvalidRequestException.validate(request.getStructureSearchType() != null, "structureSearchType is required when structure is provided");
            switch (request.getStructureSearchType()) {
                case EXACT -> {
//                    SAVE COMPOUNDS FROM EXPERIMENT
//                    ADD TABLE COMPOUND < - > EXPERIMENT
//                    QUERY HERE USING BINGO SQL OPERATOR @
//                    conditions.add("...")
                }
                case SUBSTRUCTURE -> {
                }
                case SIMILARITY -> {
                }
            }
        }
        if (request.getQuery() != null) {
            String condition = "search_vector @@ to_tsquery('english', ?)";
            for (int slotNo = SLOT_PROJECTS; slotNo <= SLOT_EXPERIMENTS; slotNo++) {
                conditions.add(slotNo, condition, request.getQuery());
            }
        }
        StringBuilder sql = new StringBuilder();
        sql.append("with t as (\n");
        boolean addedAnySQL = false;
        if (hasProjects) {
            String projectsSQL = "SELECT 'PROJECT' AS type, name, id, created_by_id, created_at, modified_by_id, modified_at "
                                 + "FROM project_view p "
                                 + "WHERE " + conditions.getQuery(SLOT_PROJECTS);
            sql.append(projectsSQL);
            addedAnySQL = true;
        }
        if (hasNotebooks) {
            if (addedAnySQL) {
                sql.append("\nUNION ALL\n");
            }
            addedAnySQL = true;
            String notebooksSQL = "SELECT 'NOTEBOOK' AS type, name, id, created_by_id, created_at, modified_by_id, modified_at "
                                  + "FROM notebook_view n "
                                  + "WHERE " + conditions.getQuery(SLOT_NOTEBOOKS);
            sql.append(notebooksSQL);
        }
        if (hasExperiments) {
            if (addedAnySQL) {
                sql.append("\nUNION ALL\n");
            }
            addedAnySQL = true;
            String experimentsSQL = "SELECT 'EXPERIMENT' AS type, name, id, created_by_id, created_at, modified_by_id, modified_at "
                                    + "FROM experiment_view e "
                                    + "WHERE " + conditions.getQuery(SLOT_EXPERIMENTS);
            sql.append(experimentsSQL);
        }
        sql.append(")\n");
        sql.append("""
            select t.type, t.name, t.id
                    , t.created_by_id, c.display_name created_by_name, t.created_at
                    , t.modified_by_id, m.display_name modified_by_name, t.modified_at
                    , count(*) over (partition by 1)
            from t
            join user_account c on c.id = t.created_by_id
            join user_account m on m.id = t.modified_by_id
        """);
        long[] totalCount = new long[] {0};
        Query query = em.createNativeQuery(sql.toString())
                .setFirstResult(paging.getPageNoOrDefault() * paging.getPageSizeOrDefault())
                .setMaxResults(paging.getPageSizeOrDefault());
        Object[] values = conditions.getValues();
        for (int i = 0; i < values.length; i++) {
            query.setParameter(i + 1, values[i]);
        }
        List<GlobalSearchResultDTO> list = query
                .getResultStream()
                .map(x -> {
                    Object[] row = (Object[]) x;
                    GlobalSearchResultDTO item = new GlobalSearchResultDTO();
                    item.setType(EntityType.valueOf(row[0].toString()));
                    item.setName((String) row[1]);
                    item.setId((UUID) row[2]);
                    item.setCreatedBy(new UserRef((UUID) row[3], (String) row[4]));
                    item.setCreatedAt(((Instant) row[5]).atZone(ZoneId.systemDefault()));
                    item.setModifiedBy(new UserRef((UUID) row[6], (String) row[7]));
                    item.setModifiedAt(((Instant) row[8]).atZone(ZoneId.systemDefault()));
                    totalCount[0] = (Long) row[9];
                    return item;
                })
                .toList();
        return Page.of(paging, totalCount[0], list);
    };
}
