package com.epam.indigoeln.signature.repository;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.SortOrder;
import com.epam.indigoeln.eln.common.repository.BaseRepository;
import com.epam.indigoeln.eln.common.util.Conditions;
import com.epam.indigoeln.signature.entity.DocumentEntity;
import com.epam.indigoeln.signature.entity.SignatureEntityType;
import com.epam.indigoeln.signature.entity.UserEntity;
import com.epam.indigoeln.signature.mapper.SignatureMapper;
import com.epam.indigoeln.signature.model.DocumentDTO;
import com.google.common.base.MoreObjects;
import io.quarkus.panache.common.Sort;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import static com.epam.indigoeln.common.util.ModelUtil.map;

@ApplicationScoped
public class DocumentRepository extends BaseRepository<DocumentEntity> {

    @Inject
    SignatureMapper signatureMapper;

    public DocumentRepository() {
        super(SignatureEntityType.DOCUMENT, DocumentEntity.class);
    }

    public Page<DocumentDTO> findAll(@Nullable String search, @Nullable SortOrder sort, @Nullable UserEntity waitingForUserSignature, Paging paging) {
        Sort panacheSort = switch (MoreObjects.firstNonNull(sort, SortOrder.LATEST)) {
            case EARLIEST -> Sort.ascending("lastModifiedDate");
            case LATEST -> Sort.descending("lastModifiedDate");
        };

        Conditions conditions = new Conditions()
                .addIfNotNull("element(signatures).user = ?", waitingForUserSignature);
        if (search != null) {
            conditions.add("(name ilike ?)", '%' + search + '%');
        }

        Page<DocumentEntity> page = doFindWithTotals(
                conditions,
                paging,
                panacheSort
        );

        return map(page, signatureMapper::entityToDocument);
    }
}
