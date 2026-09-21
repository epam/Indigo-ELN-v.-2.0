package com.epam.indigoeln.eln.util;

import com.epam.indigoeln.common.model.UserRef;
import com.epam.indigoeln.eln.common.util.CriteriaConditions;
import com.epam.indigoeln.eln.entity.DictionaryItemEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.entity.UserEntity_;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.service.DictionaryService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

@Dependent
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class ELNCriteriaConditions extends CriteriaConditions {

    @Inject
    DictionaryService dictionaryService;

    public void dictionary(Expression<DictionaryItemEntity> attribute, @Nullable DictionaryItemRef search) {
        if (search != null) {
            DictionaryItemEntity item = dictionaryService.lookup(search);
            predicates.add(attribute.equalTo(item));
        }
    }

    public void multiDictionary(Expression<? extends Collection<DictionaryItemEntity>> attribute, @Nullable DictionaryItemRef search) {
        if (search != null) {
            DictionaryItemEntity item = dictionaryService.lookup(search);
            predicates.add(cb.isMember(item, attribute));
        }
    }

    public void user(Path<UserEntity> attribute, @Nullable Collection<UserRef> search) {
        if (search != null) {
            predicates.add(cb.in(attribute.get(UserEntity_.username), search.stream().map(UserRef::getUsername).toList()));
        }
    }

    @ApplicationScoped
    public static class Factory {

        @Inject
        Instance<ELNCriteriaConditions> instance;

        public void withConditions(Consumer<List<Predicate>> applier, Consumer<ELNCriteriaConditions> block) {
            doWithConditions(instance, applier, block);
        }
    }
}
