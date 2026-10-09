package com.epam.indigoeln.sampleregistration.util;

import com.epam.indigoeln.eln.common.util.CriteriaConditions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.function.Consumer;

@Dependent
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SRSCriteriaConditions extends CriteriaConditions {

    @ApplicationScoped
    public static class Factory {

        @Inject
        Instance<SRSCriteriaConditions> instance;

        public void withConditions(Consumer<List<Predicate>> applier, Consumer<SRSCriteriaConditions> block) {
            doWithConditions(instance, applier, block);
        }
    }
}
