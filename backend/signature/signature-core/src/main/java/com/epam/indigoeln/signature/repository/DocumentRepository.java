package com.epam.indigoeln.signature.repository;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.SortOrder;
import com.epam.indigoeln.eln.common.repository.BaseRepository;
import com.epam.indigoeln.signature.entity.*;
import com.epam.indigoeln.signature.mapper.SignatureMapper;
import com.epam.indigoeln.signature.model.DocumentDTO;
import com.google.common.base.MoreObjects;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.hibernate.query.criteria.CriteriaDefinition;
import org.hibernate.query.criteria.JpaRoot;

import java.util.ArrayList;
import java.util.List;

import static com.epam.indigoeln.common.util.ModelUtil.map;

@ApplicationScoped
public class DocumentRepository extends BaseRepository<DocumentEntity> {

    @Inject
    SignatureMapper signatureMapper;

    public DocumentRepository() {
        super(SignatureEntityType.DOCUMENT, DocumentEntity.class);
    }

    public Page<DocumentDTO> findAll(@Nullable String search, @Nullable SortOrder sort, @Nullable UserEntity waitingForUserSignature, Paging paging) {
        CriteriaDefinition<Tuple> criteria = new CriteriaDefinition<>(em, Tuple.class) {{
            JpaRoot<DocumentEntity> root = from(DocumentEntity.class);
            select(tuple(root.id(), count(literal(1), createWindow())));

            List<Predicate> predicates = new ArrayList<>();
            if (waitingForUserSignature != null) {
                Subquery<Integer> signatureExists = subquery(Integer.class);
                var sigRoot = signatureExists.from(DocumentSignatureEntity.class);
                signatureExists.select(literal(1));
                signatureExists.where(equal(sigRoot.get(DocumentSignatureEntity_.document), root),
                        equal(sigRoot.get(DocumentSignatureEntity_.user), waitingForUserSignature));
                predicates.add(exists(signatureExists));
            }
            if (search != null) {
                predicates.add(ilike(root.get(DocumentEntity_.name), '%' + search + '%'));
            }
            where(predicates);

            orderBy(switch (MoreObjects.firstNonNull(sort, SortOrder.LATEST)) {
                case EARLIEST -> asc(root.get(DocumentEntity_.lastModifiedDate));
                case LATEST -> desc(root.get(DocumentEntity_.lastModifiedDate));
            });
        }};

        Page<DocumentEntity> page = doFindWithTotals(criteria, paging, null);

        return map(page, signatureMapper::entityToDocument);
    }
}
