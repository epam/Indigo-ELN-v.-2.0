package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.reaction.model.mutation.Mutation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class MutationHandlerRegistry {

    @Any
    @Inject
    Instance<MutationHandler<?, ?, ?, ?, ?, ?>> handlers;
    
    @SuppressWarnings("unchecked")
    public <H extends MutationHandler<?, ?, ?, ?, ?, ?>> H findHandler(Mutation mutation) {
        Instance<MutationHandler<?, ?, ?, ?, ?, ?>> selected = handlers.select(new MutationHandlerForLiteral(mutation.getClass()));
        if (selected.isUnsatisfied()) {
            throw new IllegalArgumentException("No handler found for: " + mutation.getClass().getName());
        }
        return (H) selected.get();
    }
}

@RequiredArgsConstructor
@Accessors(fluent = true)
@SuppressWarnings("ClassExplicitlyAnnotation")
class MutationHandlerForLiteral extends AnnotationLiteral<MutationHandlerFor> implements MutationHandlerFor {

    @Getter
    private final Class<? extends Mutation> value;
}
