package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.SortOrder;
import com.epam.indigoeln.eln.common.repository.BaseRepository;
import com.epam.indigoeln.eln.common.util.Conditions;
import com.epam.indigoeln.eln.entity.TemplateEntity;
import com.epam.indigoeln.eln.entity.TemplateEntity_;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.mapper.TemplateMapper;
import com.epam.indigoeln.eln.model.ELNEntityType;
import com.epam.indigoeln.eln.model.TemplateDTO;
import com.epam.indigoeln.eln.model.TemplateDetailsDTO;
import com.epam.indigoeln.eln.util.CriteriaConditions;
import com.google.common.base.MoreObjects;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.Tuple;
import jakarta.ws.rs.NotFoundException;
import org.hibernate.query.criteria.CriteriaDefinition;
import org.hibernate.query.criteria.JpaRoot;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

import static com.epam.indigoeln.common.util.ModelUtil.map;

@ApplicationScoped
public class TemplateRepository extends BaseRepository<TemplateEntity> {

    @Inject
    TemplateMapper templateMapper;
    @Inject
    CriteriaConditions.Factory criteriaConditionsFactory;

    public TemplateRepository() {
        super(ELNEntityType.TEMPLATE, TemplateEntity.class);
    }

    public Page<TemplateDTO> findAll(
            @Nullable String search, @Nullable SortOrder sort,
            @Nullable UserEntity createdByUser, Paging paging, boolean showAll
    ) {
        CriteriaDefinition<Tuple> criteria = new CriteriaDefinition<>(em, Tuple.class) {{
            JpaRoot<TemplateEntity> root = from(TemplateEntity.class);
            select(tuple(root.id(), count(literal(1), createWindow())));
            criteriaConditionsFactory.withConditions(this::where, conditions -> {
                if (search != null) {
                    conditions.add(ilike(root.get(TemplateEntity_.name), '%' + search + '%'));
                }
                if (createdByUser != null) {
                    conditions.add(root.get(TemplateEntity_.createdBy).equalTo(createdByUser));
                }
            });
            orderBy(switch (MoreObjects.firstNonNull(sort, SortOrder.LATEST)) {
                case EARLIEST -> asc(root.get(TemplateEntity_.modifiedAt));
                case LATEST -> desc(root.get(TemplateEntity_.modifiedAt));
            });
        }};


        Page<TemplateEntity> page = doFindWithTotals(
                criteria,
                paging,
                em.getEntityGraph("Template.list")
        );

        return map(page, templateMapper::entityToDTO);
    }

    public TemplateDetailsDTO load(UUID id) {
        TemplateEntity template = doLoad(id, em.getEntityGraph("Template.details"));
        return templateMapper.entityToDetailsDTO(template);
    }

    public TemplateDetailsDTO findByName(String name) {
        TemplateEntity template = doFindOne(
                new Conditions().add("lower(name) = ?", name.toLowerCase()),
                em.getEntityGraph("Template.details")
        );
        if (template == null) {
            throw new NotFoundException("Template not found");
        }
        return templateMapper.entityToDetailsDTO(template);
    }
}
