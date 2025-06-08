package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.eln.entity.ProjectKeywordEntity;
import com.epam.indigoeln.eln.model.EntityType;
import com.epam.indigoeln.eln.model.Paging;
import com.epam.indigoeln.eln.util.Conditions;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;

@ApplicationScoped
public class ProjectKeywordsRepository extends BaseRepository<ProjectKeywordEntity> {

    private static final Sort PROJECT_KEYWORDS_SORT = io.quarkus.panache.common.Sort.by("name");

    public ProjectKeywordsRepository() {
        super(EntityType.PROJECT_KEYWORD);
    }

    public List<ProjectKeywordEntity> find(Collection<String> names) {
        return find("name in ?1", names).list();
    }

    public List<String> suggest(@Nullable String search, Paging paging) {
        return doFind(
                new Conditions()
                        .addIfNotNull("name like ?", search != null ? search + '%' : null),
                paging,
                PROJECT_KEYWORDS_SORT,
                null,
                ProjectKeywordEntity::getName
        );
    }
}
